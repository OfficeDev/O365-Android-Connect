/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office365.connect;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.services.discovery.ServiceInfo;

import java.text.MessageFormat;

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
    private Button mSendMailButton;
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
     * Locates the service endpoints for the mail service using the DiscoveryManager class.
     */
    public void discoverMailService(){
        resetUIForDiscoverMailService();

        // DiscoveryManager does its job in a worker thread
        // we can just call getServiceInfo
        DiscoveryManager
                .getInstance()
                .getServiceInfo(Constants.MAIL_CAPABILITY,
                        new OperationCallback<ServiceInfo>() {
                            @Override
                            public void onSuccess(final ServiceInfo serviceInfo) {
                                Log.i(TAG, "discoverMailService - Mail service discovered");

                                // Initialize MailManager with ResourceID and ServiceEndpointURI
                                MailManager
                                        .getInstance()
                                        .setServiceResourceId(
                                                serviceInfo.getServiceResourceId()
                                        );
                                MailManager
                                        .getInstance()
                                        .setServiceEndpointUri(
                                                serviceInfo.getServiceEndpointUri()
                                        );

                                showDiscoverSuccessUI();
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "discoverMailService - " + e.getMessage());
                                showDiscoverErrorUI();
                            }
                        }
                );
    }

    /**
     * Handler for the onclick event of the send mail button. It uses the MailManager
     * class to send an email to the address stored in the mEmailEditText view.
     * The subject and body of the message is stored in the strings.xml file.
     * @param v The view that sent the event.
     */
    public void onSendMailButtonClick(View v){
        resetUIForSendMail();

        // MailManager does its job in a worker thread
        // we can just call sendMail
        MailManager.getInstance().sendMail(
                mEmailEditText.getText().toString(),
                getResources().getString(R.string.mail_subject_text),
                MessageFormat.format(
                        getResources().getString(R.string.mail_body_text),
                        getIntent().getStringExtra("givenName")
                ),
                new OperationCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer result) {
                        Log.i(TAG, "onSendMailButtonClick - Mail sent");
                        showSendMailSuccessUI();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onSendMailButtonClick - " + e.getMessage());
                        showSendMailErrorUI();
                    }
                }
        );
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
                case R.id.disconnectMenuItem:
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
        mSendMailButton = (Button)findViewById(R.id.sendMailButton);
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
                mConclusionTextView.setText(R.string.send_mail_text_error);
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