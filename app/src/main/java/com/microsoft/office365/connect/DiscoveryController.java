/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */
package com.microsoft.office365.connect;

import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.discoveryservices.ServiceInfo;
import com.microsoft.discoveryservices.odata.DiscoveryClient;
import com.microsoft.services.odata.impl.ADALDependencyResolver;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Handles the discovery of the service endpoints
 * for the capabilities that the user has access to
 * in Office 365.
 */
public class DiscoveryController {

    private static final String TAG = "DiscoveryController";

    private List<ServiceInfo> mServices;

    public static synchronized DiscoveryController getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DiscoveryController();
        }
        return INSTANCE;
    }

    public static synchronized void resetInstance() {
        INSTANCE = null;
    }

    private static DiscoveryController INSTANCE;

    /**
     * Provides information about the service that corresponds to the provided
     * capability.
     * @param capability A string that contains the capability of the service that
     *                   is going to be discovered.
     * @return A signal to wait on before continuing execution. The signal contains the
     * ServiceInfo object with extra information about discovered service.
     */
    public SettableFuture<ServiceInfo> getServiceInfo(final String capability) {

        final SettableFuture<ServiceInfo> result = SettableFuture.create();

        // First, look in the locally cached services.
        if(mServices != null) {
            boolean serviceFound = false;
            for (ServiceInfo service : mServices) {
                if (service.getcapability().equals(capability)) {
                    Log.i(TAG, "getServiceInfo - " + service.getserviceName() + " service for " + capability + " was found in local cached services");
                    result.set(service);
                    serviceFound = true;
                    break;
                }
            }

            if(!serviceFound) {
                NoSuchElementException noSuchElementException = new NoSuchElementException("The " + capability + " capability was not found in the local cached services.");
                Log.e(TAG, "getServiceInfo - " + noSuchElementException.getMessage());
                result.setException(noSuchElementException);
            }
        } else { // The services have not been cached yet. Go ask the discovery service.
            AuthenticationManager.getInstance().setResourceId(Constants.DISCOVERY_RESOURCE_ID);
            ADALDependencyResolver dependencyResolver = (ADALDependencyResolver) AuthenticationManager
                    .getInstance()
                    .getDependencyResolver();

            DiscoveryClient discoveryClient = new DiscoveryClient(Constants.DISCOVERY_RESOURCE_URL, dependencyResolver);

            try {
                ListenableFuture<List<ServiceInfo>> future = discoveryClient.getservices().read();
                Futures.addCallback(future,
                        new FutureCallback<List<ServiceInfo>>() {
                            @Override
                            public void onSuccess(final List<ServiceInfo> services) {
                                Log.i(TAG, "getServiceInfo - Services discovered\n");
                                // Save the discovered services to serve further requests from the local cache.
                                mServices = services;

                                boolean serviceFound = false;
                                for (ServiceInfo service : services) {
                                    if (service.getcapability().equals(capability)) {
                                        Log.i(TAG, "getServiceInfo - " + service.getserviceName() + " service for " + capability + " was found in services retrieved from discovery");
                                        result.set(service);
                                        serviceFound = true;
                                        break;
                                    }
                                }

                                if(!serviceFound) {
                                    NoSuchElementException noSuchElementException = new NoSuchElementException("The " + capability + " capability was not found in the user services.");
                                    Log.e(TAG, "getServiceInfo - " + noSuchElementException.getMessage());
                                    result.setException(noSuchElementException);
                                }
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                Log.e(TAG, "getServiceInfo - " + t.getMessage());
                                result.setException(t);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "getServiceInfo - " + e.getMessage());
                result.setException(e);
            }
        }
        return result;
    }
}

// *********************************************************
//
// O365-Android-Connect, https://github.com/OfficeDev/O365-Android-Connect
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// *********************************************************
