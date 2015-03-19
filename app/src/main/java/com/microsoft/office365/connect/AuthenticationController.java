/*
* Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
* Portions of this class are adapted from the AuthenticationController.java file from Microsoft Open Technologies, Inc.
* located at https://github.com/OfficeDev/Office-365-SDK-for-Android/blob/master/samples/outlook/app/src/main/java/com/microsoft/services/controllers/AuthenticationController.java
*/
package com.microsoft.office365.connect;

import android.app.Activity;
import android.util.Log;

import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.AuthenticationResult.AuthenticationStatus;
import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.services.odata.impl.ADALDependencyResolver;
import com.microsoft.services.odata.interfaces.DependencyResolver;
import com.microsoft.services.odata.interfaces.LogLevel;

/**
 * Handles setup of ADAL Dependency Resolver for use in API clients.
 */

public class AuthenticationController {
    private static String TAG = "AuthenticationController";

    private AuthenticationContext authContext;
    private ADALDependencyResolver dependencyResolver;
    private Activity contextActivity;
    private String resourceId;

    public static synchronized AuthenticationController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthenticationController();
        }
        return INSTANCE;
    }

    public static synchronized void resetInstance() {
        INSTANCE = null;
    }

    private static AuthenticationController INSTANCE;

    private AuthenticationController() {
        resourceId = Constants.DISCOVERY_RESOURCE_ID;
    }

    private AuthenticationController(final Activity contextActivity){
        this();
        this.contextActivity = contextActivity;
    }

    /**
     * Set the context activity before initializing to the currently active activity.
     *
     * @param contextActivity Currently active activity which can be utilized for interactive
     *                        prompt.
     */
    public void setContextActivity(final Activity contextActivity) {
        this.contextActivity = contextActivity;
    }

    /**
     * Change from the default Resource ID set in ServiceConstants to a different
     * resource ID.
     * This can be called at anytime without requiring another interactive prompt.
     * @param resourceId URL of resource ID to be accessed on behalf of user.
     */
    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
        this.dependencyResolver.setResourceId(resourceId);
    }

    /**
     * Turn on logging.
     * @param level LogLevel to set.
     */
    public void enableLogging(LogLevel level) {
        this.dependencyResolver.getLogger().setEnabled(true);
        this.dependencyResolver.getLogger().setLogLevel(level);
    }

    /**
     * Turn off logging.
     */
    public void disableLogging() {
        this.dependencyResolver.getLogger().setEnabled(false);
    }

    /**
     * Description: Calls AuthenticationContext.acquireToken(...) once to initialize with
     * user's credentials and avoid interactive prompt on later calls.
     * If all tokens expire, app must call initialize() again to prompt user interactively and
     * set up authentication context.
     *
     * @return A signal to wait on before continuing execution.
     */
    public SettableFuture<AuthenticationResult> initialize() {

        final SettableFuture<AuthenticationResult> result = SettableFuture.create();

        if (verifyAuthenticationContext()) {
            getAuthenticationContext().acquireToken(
                    this.contextActivity,
                    this.resourceId,
                    Constants.CLIENT_ID,
                    Constants.REDIRECT_URI,
                    PromptBehavior.Auto,
                    new AuthenticationCallback<AuthenticationResult>() {

                        @Override
                        public void onSuccess(final AuthenticationResult authenticationResult) {

                            if (authenticationResult != null && authenticationResult.getStatus() == AuthenticationStatus.Succeeded) {
                                dependencyResolver = new ADALDependencyResolver(
                                        getAuthenticationContext(),
                                        resourceId,
                                        Constants.CLIENT_ID);
                                result.set(authenticationResult);
                            }
                        }

                        @Override
                        public void onError(Exception t) {
                            result.setException(t);
                        }
                    }
            );
        } else {
            result.setException(new Throwable("Auth context verification failed. Did you set a context activity?"));
        }
        return result;
    }

    /**
     * Gets AuthenticationContext for AAD.
     *
     * @return authenticationContext, if successful
     */
    public AuthenticationContext getAuthenticationContext() {
        if (authContext == null) {
            try {
                authContext = new AuthenticationContext(this.contextActivity, Constants.AUTHORITY_URL, false);
            } catch (Throwable t) {
                Log.e(TAG, t.toString());
            }
        }
        return authContext;
    }

    public DependencyResolver getDependencyResolver() {
        return getInstance().dependencyResolver;
    }

    private boolean verifyAuthenticationContext() {
        if (this.contextActivity == null) {
            Log.e(TAG,"Must set context activity");
            return false;
        }
        return true;
    }
}