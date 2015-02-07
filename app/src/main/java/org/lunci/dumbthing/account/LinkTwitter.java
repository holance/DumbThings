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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

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

    public LinkTwitter(Context context, LinkAccoutCallbacks callbacks){
        super(context, callbacks);
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
        final TwitterLoginButton loginButton=new TwitterLoginButton(getContext());
        mButtonContainer.setLoginButton(loginButton);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(Keys.Preference_Twitter_Linked,true).commit();
                mButtonContainer.updateLinked(true);
                mCallbacks.onLinked(true, R.string.link_twitter_succeed);
            }

            @Override
            public void failure(TwitterException e) {
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(Keys.Preference_Twitter_Linked,false).commit();
                mButtonContainer.updateLinked(false);
                mCallbacks.onLinked(false, R.string.link_twitter_failed);
            }
        });
        loginButton.performClick();
    }

    @Override
    public void unlink() {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "unlinkFacebook");
        }
        callUnlinkConfirmDialog(R.string.unlink_twitter_warning, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Twitter.getSessionManager().clearActiveSession();
                Twitter.logOut();
            }
        });
    }

    @Override
    public void onLinkResult(int requestCode, int resultCode, Intent data) {
        try {
            ((TwitterLoginButton) mButtonContainer.getLoginButton()).onActivityResult(requestCode, resultCode, data);
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
    }
}
