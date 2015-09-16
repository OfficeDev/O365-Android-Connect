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
import java.util.concurrent.ExecutionException;

/**
 * Handles the discovery of the service endpoints
 * for the capabilities that the user has access to
 * in Office 365.
 */
public class DiscoveryManager {

    private static final String TAG = "DiscoveryManager";

    private List<ServiceInfo> mServices;

    public static synchronized DiscoveryManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DiscoveryManager();
        }
        return INSTANCE;
    }

    private static DiscoveryManager INSTANCE;

    /**
     * Provides information about the service that corresponds to the provided capability.
     * @param capability A string that contains the capability of the service that
     *                   is going to be discovered.
     * @return The ServiceInfo object with extra information about discovered service.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public ServiceInfo getServiceInfo(final String capability) throws InterruptedException, ExecutionException {
        // First, look in the locally cached services.
        if(mServices != null) {
            for (ServiceInfo serviceInfo : mServices) {
                if (serviceInfo.getcapability().equals(capability)) {
                    Log.i(TAG, "getServiceInfo - " + serviceInfo.getserviceName() + " service for " + capability + " was found in local cached services");
                    return serviceInfo;
                }
            }

            // We already cached the services but couldn't find the requested service in local cache
            Log.e(TAG, "getServiceInfo - The " + capability + " capability was not found in the local cached services. "
                    + "Falling back to the discovery service");
            return getServiceInfoFromDiscoveryService(capability);
        } else {
            // The services have not been cached yet. Go ask the discovery service.
            return getServiceInfoFromDiscoveryService(capability);
        }
    }

    /**
     * Provides information about the service that corresponds to the provided capability.
     * @param capability A string that contains the capability of the service that
     *                   is going to be discovered.
     * @return The ServiceInfo object with extra information about discovered service.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public ServiceInfo getServiceInfoFromDiscoveryService(final String capability) throws InterruptedException, ExecutionException {
        AuthenticationManager.getInstance().setResourceId(Constants.DISCOVERY_RESOURCE_ID);
        ADALDependencyResolver dependencyResolver = (ADALDependencyResolver) AuthenticationManager
                .getInstance()
                .getDependencyResolver();

        DiscoveryClient discoveryClient = new DiscoveryClient(Constants.DISCOVERY_RESOURCE_URL, dependencyResolver);

        List<ServiceInfo> services =
                discoveryClient
                        .getservices()
                        .select("serviceResourceId,serviceEndpointUri,capability")
                        .read().get();

        Log.i(TAG, "getServiceInfoFromDiscoveryService - Services discovered\n");
        // Save the discovered services to serve further requests from the local cache.
        mServices = services;

        for (ServiceInfo service : services) {
            if (service.getcapability().equals(capability)) {
                Log.i(TAG, "getServiceInfoFromDiscoveryService - " + service.getserviceName() + " service for " + capability + " was found in services retrieved from discovery");
                return service;
            }
        }

        // We haven't cached the services but couldn't find the requested service in discovery service
        NoSuchElementException noSuchElementException = new NoSuchElementException("The " + capability + " capability was not found in the user services.");
        Log.e(TAG, "getServiceInfoFromDiscoveryService - " + noSuchElementException.getMessage());
        throw noSuchElementException;
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
