/*
 * Copyright 2015 Lunci Hua
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lunci.dumbthing.account;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.preference.PreferencesTracker;

/**
 * Created by Lunci on 2/6/2015.
 */
public class LinkGooglePlus extends LinkAccountBase implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG=LinkGooglePlus.class.getSimpleName();

    private LinkButtonContainer mButtonContainer;
    /* Track whether the sign-in button has been clicked so that we know to resolve
 * all issues preventing sign-in without waiting.
 */
    private boolean mSignInClicked;
    private static final int RC_SIGN_IN = 0;
    /* Store the connection result from onConnectionFailed callbacks so that we can
     * resolve them when the user clicks sign-in.
     */
    private ConnectionResult mConnectionResult;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;

    public LinkGooglePlus(Activity activity, LinkAccoutCallbacks callbacks){
        super(activity, callbacks);
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    @Override
    public void setButtonContainer(LinkButtonContainer container) {
        mButtonContainer=container;
    }

    @Override
    public LinkButtonContainer getButtonContainer() {
        return mButtonContainer;
    }

    @Override
    public boolean isLinked() {
        return PreferencesTracker.getInstance().isTwitterLinked();
    }

    @Override
    public void link(Object args) {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "link google plus");
        }
        mGoogleApiClient.connect();
        SignInButton button=new SignInButton(getActivity());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mGoogleApiClient.isConnecting()) {
                    mSignInClicked = true;
                    resolveSignInError();
                }
            }
        });
    }

    @Override
    public void unlink() {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "unlinkGooglePlus");
        }
        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {

                    @Override
                    public void onResult(Status status) {

                    }
                });
    }

    @Override
    public void onLinkResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
//            try {
//                mIntentInProgress = true;
//                getContext().startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
//                        RC_SIGN_IN, null, 0, 0, 0);
//            } catch (IntentSender.SendIntentException e) {
//                // The intent was canceled before it was sent.  Return to the default
//                // state and attempt to connect to get an updated ConnectionResult.
//                mIntentInProgress = false;
//                mGoogleApiClient.connect();
//            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInClicked = false;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
