/*
* Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
* Portions of this class are adapted from the AuthenticationController.java file from Microsoft Open Technologies, Inc.
* located at https://github.com/OfficeDev/Office-365-SDK-for-Android/blob/master/samples/outlook/app/src/main/java/com/microsoft/services/controllers/AuthenticationController.java
*/
package com.microsoft.office365.connect;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.microsoft.aad.adal.ADALError;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationException;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.AuthenticationResult.AuthenticationStatus;
import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.services.odata.impl.ADALDependencyResolver;
import com.microsoft.services.odata.interfaces.DependencyResolver;
import com.microsoft.services.odata.interfaces.LogLevel;

/**
 * Handles setup of ADAL Dependency Resolver for use in API clients.
 */

public class AuthenticationManager {
    private static String TAG = "AuthenticationManager";

    private AuthenticationContext mAuthenticationContext;
    private ADALDependencyResolver mDependencyResolver;
    private Activity mContextActivity;
    private String mResourceId;

    public static synchronized AuthenticationManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthenticationManager();
        }
        return INSTANCE;
    }

    public static synchronized void resetInstance() {
        INSTANCE = null;
    }

    private static AuthenticationManager INSTANCE;

    private AuthenticationManager() {
        mResourceId = Constants.DISCOVERY_RESOURCE_ID;
    }

    /**
     * Set the context activity before connecting to the currently active activity.
     *
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

    /**
     * Description: Calls AuthenticationContext.acquireToken(...) once to connect with
     * user's credentials and avoid interactive prompt on later calls.
     * If all tokens expire, app must call connect() again to prompt user interactively and
     * set up authentication context.
     *
     * @return A signal to wait on before continuing execution.
     */
    public void connect(final AuthenticationCallback authenticationCallback) {
        if (verifyAuthenticationContext()) {
            getAuthenticationContext().acquireToken(
                    this.mContextActivity,
                    this.mResourceId,
                    Constants.CLIENT_ID,
                    Constants.REDIRECT_URI,
                    PromptBehavior.Auto,
                    new AuthenticationCallback<AuthenticationResult>() {
                        @Override
                        public void onSuccess(final AuthenticationResult authenticationResult) {

                            if (authenticationResult != null && authenticationResult.getStatus() == AuthenticationStatus.Succeeded) {
                                mDependencyResolver = new ADALDependencyResolver(
                                        getAuthenticationContext(),
                                        mResourceId,
                                        Constants.CLIENT_ID);
                                authenticationCallback.onSuccess(authenticationResult);
                            } else if (authenticationResult != null){
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
                            authenticationCallback.onError(e);
                        }
                    }
            );
        } else {
            Log.e(TAG, "connect - Auth context verification failed. Did you set a context activity?");
            AuthenticationException ae = new AuthenticationException(
                    ADALError.ACTIVITY_REQUEST_INTENT_DATA_IS_NULL,
                    "Auth context verification failed. Did you set a context activity?");
            throw ae;
        }
    }

    /**
     * Gets AuthenticationContext for AAD.
     *
     * @return authenticationContext, if successful
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

    public DependencyResolver getDependencyResolver() {
        return getInstance().mDependencyResolver;
    }

    private boolean verifyAuthenticationContext() {
        if (this.mContextActivity == null) {
            Log.e(TAG,"Must set context activity");
            return false;
        }
        return true;
    }

    /**
     * Disconnects the app from Office 365 by clearing the token cache, setting the client objects
     * to null, and clearing the app cookies from the device.
     */
    public void disconnect(){
        //Clear tokens.
        if(getAuthenticationContext().getCache() != null) {
            getAuthenticationContext().getCache().removeAll();
        }

        //Reset controller objects.
        MailController.resetInstance();
        DiscoveryController.resetInstance();
        AuthenticationManager.resetInstance();

        //Clear cookies.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            CookieManager.getInstance().removeSessionCookies(null);
            CookieManager.getInstance().flush();
        }else{
            CookieManager.getInstance().removeSessionCookie();
            CookieSyncManager.getInstance().sync();
        }
    }
}