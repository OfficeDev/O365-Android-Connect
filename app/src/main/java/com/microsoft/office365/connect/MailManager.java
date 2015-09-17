/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */
package com.microsoft.office365.connect;

import android.util.Log;

import com.microsoft.outlookservices.BodyType;
import com.microsoft.outlookservices.EmailAddress;
import com.microsoft.outlookservices.ItemBody;
import com.microsoft.outlookservices.Message;
import com.microsoft.outlookservices.Recipient;
import com.microsoft.outlookservices.odata.OutlookClient;
import com.microsoft.services.odata.impl.ADALDependencyResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.ExecutionException;

/**
 * Handles the creation of the message and contacting the
 * mail service to send the message. The app must have
 * connected to Office 365 and discovered the mail service
 * endpoints before using the sendMail method.
 */
public class MailManager {

    private static final String TAG = "MailManager";

    private String mServiceResourceId;
    private String mServiceEndpointUri;

    public static synchronized MailManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MailManager();
        }
        return INSTANCE;
    }

    private static MailManager INSTANCE;

    /**
     * Store the service resource id from the discovered service.
     * @param serviceResourceId The service resource id obtained from the discovery service.
     */
    public void setServiceResourceId(final String serviceResourceId) {
        this.mServiceResourceId = serviceResourceId;
    }

    /**
     * Store the service endpoint uri from the discovered service.
     * @param serviceEndpointUri The service endpoint uri obtained from the discovery service.
     */
    public void setServiceEndpointUri(final String serviceEndpointUri) {
        this.mServiceEndpointUri = serviceEndpointUri;
    }

    /**
     * Check to see if the service resource id and service endpoint uri values have been set.
     * @return True if service resource id and service endpoint uri have been set, false otherwise.
     */
    public boolean isReady(){
        return mServiceEndpointUri != null && mServiceResourceId != null;
    }

    /**
     * Sends an email message using the Office 365 mail capability from the address of the
     * signed in user. You need to initialize the MailManager by calling
     * - {@link MailManager#setServiceResourceId(String)}
     * - {@link MailManager#setServiceEndpointUri(String)}
     * @param emailAddress The recipient email address.
     * @param subject The subject to use in the mail message.
     * @param body The body of the message.
     * @param operationCallback The callback to which return the result or error.
     */
    public void sendMail(final String emailAddress, final String subject, final String body, final OperationCallback<Integer> operationCallback) {

        if(!isReady()){
            throw new MissingResourceException(
                    "You must set the ServiceResourceId and ServiceEndPointUri before using sendMail",
                    "MailManager",
                    "ServiceResourceId, ServiceEndPointUri"
            );
        }

        // Since we're doing considerable work, let's get out of the main thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AuthenticationManager.getInstance().setResourceId(mServiceResourceId);
                    ADALDependencyResolver dependencyResolver = (ADALDependencyResolver) AuthenticationManager
                            .getInstance()
                            .getDependencyResolver();

                    OutlookClient mailClient = new OutlookClient(mServiceEndpointUri, dependencyResolver);

                    // Prepare the message.
                    List<Recipient> recipientList = new ArrayList<>();

                    Recipient recipient = new Recipient();
                    EmailAddress email = new EmailAddress();
                    email.setAddress(emailAddress);
                    recipient.setEmailAddress(email);
                    recipientList.add(recipient);

                    Message messageToSend = new Message();
                    messageToSend.setToRecipients(recipientList);

                    ItemBody bodyItem = new ItemBody();
                    bodyItem.setContentType(BodyType.HTML);
                    bodyItem.setContent(body);
                    messageToSend.setBody(bodyItem);
                    messageToSend.setSubject(subject);

                    // Contact the Office 365 service and deliver the message.
                    Integer mailId = mailClient
                            .getMe()
                            .getOperations()
                            .sendMail(messageToSend, true).get();

                    Log.i(TAG, "sendMail - Email with ID: " + mailId + "sent");
                    operationCallback.onSuccess(mailId);
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(TAG, "sendMail - " + e.getMessage());
                    operationCallback.onError(e);
                }
            }
        }).start();
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
