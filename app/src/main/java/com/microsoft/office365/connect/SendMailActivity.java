/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */
package com.microsoft.office365.connect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.discoveryservices.ServiceInfo;

import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;

/**
 * This activity handles the send mail operation of the app.
 * The app must be connected to Office 365 before this activity can send an email.
 * The activity uses the DiscoveryManager class to get the service endpoint. It also
 * uses the MailManager to send the message.
 */
public class SendMailActivity extends AppCompatActivity {

    private static final String TAG = "SendMailActivity";

    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private EditText mEmailEditText;
    private ImageButton mSendMailButton;
    private ProgressBar mSendMailProgressBar;
    private TextView mConclusionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);

        initializeViews();

        // Extract the givenName and displayableId and use it in the UI.
        mTitleTextView.append(getIntent()
                .getStringExtra("givenName") + "!");
        mEmailEditText.setText(getIntent()
                .getStringExtra("displayableId"));

        // We don't need to wait for user input to discover the mail service,
        // so we just do it
        discoverMailService();
    }

    /**
     * It locates the service endpoints for the mail service using the DiscoveryManager class.
     */
    public void discoverMailService(){
        resetUIForDiscoverMailService();

        new Thread(new Runnable() {
            public void run() {
                try {
                    // Since we are no longer on the UI thread,
                    // we can call this method synchronously without blocking the UI
                    ServiceInfo serviceInfo = DiscoveryManager
                            .getInstance()
                            .getServiceInfo(Constants.MAIL_CAPABILITY);
                    Log.i(TAG, "discoverMailService - Mail service discovered");

                    // Initialize MailManager with ResourceID and ServiceEndpointURI
                    MailManager
                            .getInstance()
                            .setServiceResourceId(
                                    serviceInfo.getserviceResourceId()
                            );
                    MailManager
                            .getInstance()
                            .setServiceEndpointUri(
                                    serviceInfo.getserviceEndpointUri()
                            );

                    showDiscoverSuccessUI();
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(TAG, "discoverMailService - " + e.getMessage());
                    showDiscoverErrorUI();
                }
            }
        }).start();
    }

    /**
     * Handler for the onclick event of the send mail button. It uses the MailManager
     * class to send an email to the address stored in the mEmailEditText view.
     * The subject and body of the message is stored in the strings.xml file.
     * @param v
     */
    public void onSendMailButtonClick(View v){
        resetUIForSendMail();

        new Thread(new Runnable() {
            public void run() {
                try {
                    // Since we are no longer on the UI thread,
                    // we can call this method synchronously without blocking the UI
                    Integer mailId = MailManager.getInstance().sendMail(
                            mEmailEditText.getText().toString(),
                            getResources().getString(R.string.mail_subject_text),
                            MessageFormat.format(
                                    getResources().getString(R.string.mail_body_text),
                                    getIntent().getStringExtra("givenName")
                            )
                    );
                    Log.i(TAG, "onSendMailButtonClick - Mail sent");
                    showSendMailSuccessUI();
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(TAG, "onSendMailButtonClick - " + e.getMessage());
                    showSendMailErrorUI();
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_mail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.disconnectMenuitem:
                    AuthenticationManager.getInstance().disconnect();
                    showDisconnectSuccessUI();
                    Intent connectIntent = new Intent(this, ConnectActivity.class);
                    startActivity(connectIntent);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }

        } catch (Throwable t) {
            if (t.getMessage() == null)
                Log.e(TAG, " ");
            else
                Log.e(TAG, t.getMessage());
        }
        return true;
    }

    private void initializeViews(){
        mTitleTextView = (TextView)findViewById(R.id.titleTextView);
        mDescriptionTextView = (TextView)findViewById(R.id.descriptionTextView);
        mEmailEditText = (EditText)findViewById(R.id.emailEditText);
        mSendMailButton = (ImageButton)findViewById(R.id.sendMailButton);
        mSendMailProgressBar = (ProgressBar)findViewById(R.id.sendMailProgressBar);
        mConclusionTextView = (TextView)findViewById(R.id.conclusionTextView);
    }

    private void resetUIForDiscoverMailService(){
        mSendMailButton.setVisibility(View.GONE);
        mConclusionTextView.setVisibility(View.GONE);
        mSendMailProgressBar.setVisibility(View.VISIBLE);
    }

    private void resetUIForSendMail(){
        mSendMailButton.setVisibility(View.GONE);
        mConclusionTextView.setVisibility(View.GONE);
        mSendMailProgressBar.setVisibility(View.VISIBLE);
    }

    private void showDiscoverSuccessUI(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Now that we have discovered the mail service, show the send mail button
                mSendMailButton.setVisibility(View.VISIBLE);
                mSendMailProgressBar.setVisibility(View.GONE);

                Toast.makeText(
                        SendMailActivity.this,
                        R.string.discover_toast_text,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDiscoverErrorUI(){
        runOnUiThread(new Runnable() {
            @Override
            public void run(){
                mSendMailProgressBar.setVisibility(View.GONE);
                mSendMailButton.setVisibility(View.VISIBLE);
                mConclusionTextView.setText(R.string.discover_text_error);
                mConclusionTextView.setVisibility(View.VISIBLE);
                Toast.makeText(
                        SendMailActivity.this,
                        R.string.discover_toast_text_error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSendMailSuccessUI(){
        runOnUiThread(new Runnable() {
            @Override
            public void run(){
                mSendMailProgressBar.setVisibility(View.GONE);
                mSendMailButton.setVisibility(View.VISIBLE);
                mConclusionTextView.setText(R.string.conclusion_text);
                mConclusionTextView.setVisibility(View.VISIBLE);
                Toast.makeText(
                        SendMailActivity.this,
                        R.string.send_mail_toast_text,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSendMailErrorUI(){
        runOnUiThread(new Runnable() {
            @Override
            public void run(){
                mSendMailProgressBar.setVisibility(View.GONE);
                mSendMailButton.setVisibility(View.VISIBLE);
                mConclusionTextView.setText(R.string.sendmail_text_error);
                mConclusionTextView.setVisibility(View.VISIBLE);
                Toast.makeText(
                        SendMailActivity.this,
                        R.string.send_mail_toast_text_error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDisconnectSuccessUI(){
        mTitleTextView.setVisibility(View.GONE);
        mDescriptionTextView.setVisibility(View.GONE);
        mEmailEditText.setVisibility(View.GONE);
        mSendMailButton.setVisibility(View.GONE);
        mConclusionTextView.setVisibility(View.GONE);

        Toast.makeText(
                SendMailActivity.this,
                R.string.disconnect_toast_text,
                Toast.LENGTH_SHORT).show();
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
