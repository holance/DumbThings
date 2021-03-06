/*
 * Copyright (c) 2015 Lunci Hua
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package org.lunci.dumbthing.account;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;
import org.lunci.dumbthing.preference.Keys;
import org.lunci.dumbthing.preference.PreferencesTracker;

/**
 * Created by Lunci on 2/6/2015.
 */
public class LinkTwitter extends LinkAccountBase {
    private static final String TAG=LinkTwitter.class.getSimpleName();

    private LinkButtonContainer mButtonContainer;
    private TwitterAuthClient mClient;
    public LinkTwitter(Activity activity, LinkAccoutCallbacks callbacks){
        super(activity, callbacks);
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
            Log.i(TAG, "link twitter");
        }
        mClient = new TwitterAuthClient();
        mClient.authorize(getActivity(),mCallback);
    }
    
    private Callback<TwitterSession> mCallback = new Callback<TwitterSession>(){

        @Override
        public void success(Result<TwitterSession> twitterSessionResult) {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(Keys.Preference_Twitter_Linked,true).commit();
            mButtonContainer.updateLinked(true);
            mCallbacks.onLinked(true, R.string.link_twitter_succeed);
        }

        @Override
        public void failure(TwitterException e) {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(Keys.Preference_Twitter_Linked,false).commit();
            mButtonContainer.updateLinked(false);
            mCallbacks.onLinked(false, R.string.link_twitter_failed);
        }
    };

    @Override
    public void unlink() {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "unlinkTwitter");
        }
        callUnlinkConfirmDialog(R.string.unlink_twitter_warning, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Twitter.getSessionManager().clearActiveSession();
                Twitter.logOut();
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(Keys.Preference_Twitter_Linked,false).commit();
                mButtonContainer.updateLinked(false);
            }
        });
    }

    @Override
    public void onLinkResult(int requestCode, int resultCode, Intent data) {
        try {
            mClient.onActivityResult(requestCode, resultCode, data);
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }
}
