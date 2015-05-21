/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */
package com.microsoft.office365.connect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;

import java.net.URI;
import java.util.UUID;

/**
 * Starting activity of the app. Handles the connection to Office 365.
 * When it first starts it only displays a button to Connect to Office 365.
 * If there are no cached tokens, the user is required to sign in to Office 365.
 * If there are cached tokens, the app tries to reuse them.
 * The activity redirects the user to the SendMailActivity upon successful connection.
 */
public class ConnectActivity extends ActionBarActivity {

    private static final String TAG = "ConnectActivity";

    private Button mConnectButton;
    private TextView mTitleTextView;
    private ProgressBar mConnectProgressBar;
    private TextView mDescriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        initializeViews();
    }

    /**
     * Event handler for the onclick event of the button.
     * @param v
     */
    public void onConnectButtonClick(View v) {
        showConnectingInProgressUI();

        //check that client id and redirect have been set correctly
        try {
            UUID.fromString(Constants.CLIENT_ID);
            URI.create(Constants.REDIRECT_URI);
        }
        catch (IllegalArgumentException e) {
            Toast.makeText(
                    this
                    , getString(R.string.warning_clientid_redirecturi_incorrect)
                    , Toast.LENGTH_LONG).show();

            resetUIForConnect();
            return;
        }

        final Intent sendMailIntent = new Intent(this, SendMailActivity.class);
        AuthenticationManager.getInstance().setContextActivity(this);

        AuthenticationManager.getInstance().connect(
                new AuthenticationCallback<AuthenticationResult>() {
                    /**
                     * If the connection is successful, the activity extracts the username and
                     * displayableId values from the authentication result object and sends them
                     * to the SendMail activity.
                     * @param result The authentication result object that contains information about
                     *               the user and the tokens.
                     */
                    @Override
                    public void onSuccess(AuthenticationResult result) {
                        Log.i(TAG, "onConnectButtonClick - Successfully connected to Office 365");

                        sendMailIntent.putExtra("givenName", result
                                .getUserInfo()
                                .getGivenName());
                        sendMailIntent.putExtra("displayableId", result
                                .getUserInfo()
                                .getDisplayableId());
                        startActivity(sendMailIntent);

                        resetUIForConnect();
                    }

                    @Override
                    public void onError(final Exception e) {
                        Log.e(TAG, "onCreate - " + e.getMessage());
                        showConnectErrorUI();
                    }
                });
    }

    /**
     * This activity gets notified about the completion of the ADAL activity through this method.
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its
     *                   setResult().
     * @param data An Intent, which can return result data to the caller (various data
     *             can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult - AuthenticationActivity has come back with results");
        super.onActivityResult(requestCode, resultCode, data);
        AuthenticationManager
                .getInstance()
                .getAuthenticationContext()
                .onActivityResult(requestCode, resultCode, data);
    }

    private void initializeViews(){
        mConnectButton = (Button)findViewById(R.id.connectButton);
        mConnectProgressBar = (ProgressBar)findViewById(R.id.connectProgressBar);
        mTitleTextView = (TextView)findViewById(R.id.titleTextView);
        mDescriptionTextView = (TextView)findViewById(R.id.descriptionTextView);
    }

    private void resetUIForConnect(){
        mConnectButton.setVisibility(View.VISIBLE);
        mTitleTextView.setVisibility(View.GONE);
        mDescriptionTextView.setVisibility(View.GONE);
        mConnectProgressBar.setVisibility(View.GONE);
    }

    private void showConnectingInProgressUI(){
        mConnectButton.setVisibility(View.GONE);
        mTitleTextView.setVisibility(View.GONE);
        mDescriptionTextView.setVisibility(View.GONE);
        mConnectProgressBar.setVisibility(View.VISIBLE);
    }

    private void showConnectErrorUI(){
        mConnectButton.setVisibility(View.VISIBLE);
        mConnectProgressBar.setVisibility(View.GONE);
        mTitleTextView.setText(R.string.title_text_error);
        mTitleTextView.setVisibility(View.VISIBLE);
        mDescriptionTextView.setText(R.string.connect_text_error);
        mDescriptionTextView.setVisibility(View.VISIBLE);
        Toast.makeText(
                ConnectActivity.this,
                R.string.connect_toast_text_error,
                Toast.LENGTH_LONG).show();
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
