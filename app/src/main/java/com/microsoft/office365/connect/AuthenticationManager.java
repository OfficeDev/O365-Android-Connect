/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office365.connect;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.microsoft.aad.adal.ADALError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.AuthenticationResult.AuthenticationStatus;
import com.microsoft.aad.adal.AuthenticationSettings;
import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.services.orc.core.DependencyResolver;
import com.microsoft.services.orc.log.LogLevel;
import com.microsoft.services.orc.resolvers.ADALDependencyResolver;

import java.io.UnsupportedEncodingException;

/**
 * Handles setup of ADAL Dependency Resolver for use in API clients.
 */

public class AuthenticationManager {
    private static final String TAG = "AuthenticationManager";
    private static final String PREFERENCES_FILENAME = "ConnectFile";
    private static final String USER_ID_VAR_NAME = "userId";
    private AuthenticationContext mAuthenticationContext;
    private ADALDependencyResolver mDependencyResolver;
    private Activity mContextActivity;
    private String mResourceId;

    static{
        // Devices with API level lower than 18 must setup an encryption key.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                AuthenticationSettings.INSTANCE.getSecretKeyData() == null) {
            AuthenticationSettings.INSTANCE.setSecretKey(generateSecretKey());
        }

        // We're not using Microsoft Intune Company portal app,
        // skip the broker check so we don't get warnings about the following permissions
        // in manifest:
        // GET_ACCOUNTS
        // USE_CREDENTIALS
        // MANAGE_ACCOUNTS
        AuthenticationSettings.INSTANCE.setSkipBroker(true);
    }

    /**
     * Calls {@link AuthenticationManager#authenticatePrompt(AuthenticationCallback)} if no user id is stored in the shared preferences.
     * Calls {@link AuthenticationManager#authenticateSilent(AuthenticationCallback)} otherwise.
     * @param authenticationCallback The callback to notify when the processing is finished.
     */
    public void connect(final AuthenticationCallback<AuthenticationResult> authenticationCallback) {
        // Since we're doing considerable work, let's get out of the main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (verifyAuthenticationContext()) {
                    if (isConnected()) {
                        authenticateSilent(authenticationCallback);
                    } else {
                        authenticatePrompt(authenticationCallback);
                    }
                } else {
                    Log.e(TAG, "connect - Auth context verification failed. Did you set a context activity?");
                    throw new AuthenticationException(
                            ADALError.ACTIVITY_REQUEST_INTENT_DATA_IS_NULL,
                            "Auth context verification failed. Did you set a context activity?");
                }
            }
        }).start();
    }

    /**
     * Calls acquireTokenSilent with the user id stored in shared preferences.
     * In case of an error, it falls back to {@link AuthenticationManager#authenticatePrompt(AuthenticationCallback)}.
     * @param authenticationCallback The callback to notify when the processing is finished.
     */
    private void authenticateSilent(final AuthenticationCallback<AuthenticationResult> authenticationCallback) {
        getAuthenticationContext().acquireTokenSilent(
                this.mResourceId,
                Constants.CLIENT_ID,
                getUserId(),
                new AuthenticationCallback<AuthenticationResult>() {
                    @Override
                    public void onSuccess(final AuthenticationResult authenticationResult) {
                        if (authenticationResult != null && authenticationResult.getStatus() == AuthenticationStatus.Succeeded) {
                            mDependencyResolver = new ADALDependencyResolver(
                                    getAuthenticationContext(),
                                    mResourceId,
                                    Constants.CLIENT_ID);
                            authenticationCallback.onSuccess(authenticationResult);
                        } else if (authenticationResult != null) {
                            // I could not authenticate the user silently,
                            // falling back to prompt the user for credentials.
                            authenticatePrompt(authenticationCallback);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        // I could not authenticate the user silently,
                        // falling back to prompt the user for credentials.
                        authenticatePrompt(authenticationCallback);
                    }
                }
        );
    }

    /**
     * Calls acquireToken to prompt the user for credentials.
     * @param authenticationCallback The callback to notify when the processing is finished.
     */
    private void authenticatePrompt(final AuthenticationCallback<AuthenticationResult> authenticationCallback) {
        getAuthenticationContext().acquireToken(
                this.mContextActivity,
                this.mResourceId,
                Constants.CLIENT_ID,
                Constants.REDIRECT_URI,
                PromptBehavior.Always,
                new AuthenticationCallback<AuthenticationResult>() {
                    @Override
                    public void onSuccess(final AuthenticationResult authenticationResult) {
                        if (authenticationResult != null && authenticationResult.getStatus() == AuthenticationStatus.Succeeded) {
                            setUserId(authenticationResult.getUserInfo().getUserId());
                            mDependencyResolver = new ADALDependencyResolver(
                                    getAuthenticationContext(),
                                    mResourceId,
                                    Constants.CLIENT_ID);
                            authenticationCallback.onSuccess(authenticationResult);
                        } else if (authenticationResult != null) {
                            // We need to make sure that there is no data stored with the failed auth
                            AuthenticationManager.getInstance().disconnect();
                            // This condition can happen if user signs in with an MSA account
                            // instead of an Office 365 account
                            authenticationCallback.onError(
                                    new AuthenticationException(
                                            ADALError.AUTH_FAILED,
                                            authenticationResult.getErrorDescription()
                                    )
                            );
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        // We need to make sure that there is no data stored with the failed auth
                        AuthenticationManager.getInstance().disconnect();
                        authenticationCallback.onError(e);
                    }
                }
        );
    }

    /**
     * Disconnects the app from Office 365 by clearing the token cache, setting the client objects
     * to null, and removing the user id from shred preferences.
     */
    public void disconnect(){
        // Clear tokens.
        if(getAuthenticationContext().getCache() != null) {
            getAuthenticationContext().getCache().removeAll();
        }

        // Reset the AuthenticationManager object
        AuthenticationManager.resetInstance();

        // Forget the user
        removeUserId();
    }

    public static synchronized AuthenticationManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthenticationManager();
        }
        return INSTANCE;
    }

    private static synchronized void resetInstance() {
        INSTANCE = null;
    }

    private static AuthenticationManager INSTANCE;

    private AuthenticationManager() {
        mResourceId = Constants.DISCOVERY_RESOURCE_ID;
    }

    /**
     * Set the context activity before connecting to the currently active activity.
     * @param contextActivity Currently active activity which can be utilized for interactive
     *                        prompt.
     */
    public void setContextActivity(final Activity contextActivity) {
        this.mContextActivity = contextActivity;
    }

    /**
     * Change from the default Resource ID set in ServiceConstants to a different
     * resource ID.
     * This can be called at anytime without requiring another interactive prompt.
     * @param resourceId URL of resource ID to be accessed on behalf of user.
     */
    public void setResourceId(final String resourceId) {
        this.mResourceId = resourceId;
        this.mDependencyResolver.setResourceId(resourceId);
    }

    /**
     * Gets authentication context for Azure Active Directory.
     * @return an authentication context, if successful.
     */
    public AuthenticationContext getAuthenticationContext() {
        if (mAuthenticationContext == null) {
            try {
                mAuthenticationContext = new AuthenticationContext(this.mContextActivity, Constants.AUTHORITY_URL, false);
            } catch (Throwable t) {
                Log.e(TAG, t.toString());
            }
        }
        return mAuthenticationContext;
    }

    /**
     * Dependency resolver that can be used to create client objects.
     * The {@link DiscoveryManager#getServiceInfo} method uses it to create a DiscoveryClient object.
     * The {@link MailManager#sendMail(String, String, String, OperationCallback)} uses it to create an OutlookClient object.
     * @return The dependency resolver object.
     */
    public DependencyResolver getDependencyResolver() {
        return getInstance().mDependencyResolver;
    }

    private boolean verifyAuthenticationContext() {
        if (this.mContextActivity == null) {
            Log.e(TAG, "Must set context activity");
            return false;
        }
        return true;
    }

    private boolean isConnected(){
        SharedPreferences settings = this
                .mContextActivity
                .getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        return settings.contains(USER_ID_VAR_NAME);
    }

    private String getUserId(){
        SharedPreferences settings = this
                .mContextActivity
                .getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        return settings.getString(USER_ID_VAR_NAME, "");
    }

    private void setUserId(String value){
        SharedPreferences settings = this
                .mContextActivity
                .getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(USER_ID_VAR_NAME, value);
        editor.apply();
    }

    private void removeUserId(){
        SharedPreferences settings = this
                .mContextActivity
                .getSharedPreferences(PREFERENCES_FILENAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = settings.edit();
        editor.remove(USER_ID_VAR_NAME);
        editor.apply();
    }

    /**
     * Generates an encryption key for devices with API level lower than 18 using the
     * ANDROID_ID value as a seed.
     * In production scenarios, you should come up with your own implementation of this method.
     * Consider that your algorithm must return the same key so it can encrypt/decrypt values
     * successfully.
     * @return The encryption key in a 32 byte long array.
     */
    private static byte[] generateSecretKey() {
        byte[] key = new byte[32];
        byte[] android_id;

        try{
            android_id = Settings.Secure.ANDROID_ID.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e){
            Log.e(TAG, "generateSecretKey - " + e.getMessage());
            throw new RuntimeException(e);
        }

        for(int i = 0; i < key.length; i++){
            key[i] = android_id[i % android_id.length];
        }

        return key;
    }

    /**
     * Turn logging on.
     * @param level LogLevel to set.
     */
    public void enableLogging(LogLevel level) {
        this.mDependencyResolver.getLogger().setEnabled(true);
        this.mDependencyResolver.getLogger().setLogLevel(level);
    }

    /**
     * Turn logging off.
     */
    public void disableLogging() {
        this.mDependencyResolver.getLogger().setEnabled(false);
    }
}