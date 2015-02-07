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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.facebook.Session;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;
import org.lunci.dumbthing.preference.Keys;
import org.lunci.dumbthing.preference.PreferencesTracker;

/**
 * Created by Lunci on 2/6/2015.
 */
public class LinkFacebook extends LinkAccountBase {
    private static final String TAG=LinkFacebook.class.getSimpleName();

    private LinkButtonContainer mButtonContainer;

    public LinkFacebook(Context context, LinkAccoutCallbacks callbacks){
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
        return PreferencesTracker.getInstance().isFacebookLinked();
    }

    @Override
    public void link(Object args) {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "link facebook");
        }
        com.facebook.widget.LoginButton button=new com.facebook.widget.LoginButton(getContext());
        if(args instanceof Fragment)
        button.setFragment((Fragment)args);
        button.setPublishPermissions(getContext().getResources().getStringArray(R.array.facebook_permissions));
        button.performClick();
    }

    @Override
    public void unlink() {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "unlinkFacebook");
        }
        callUnlinkConfirmDialog(R.string.unlink_facebook_warning, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Session.getActiveSession().closeAndClearTokenInformation();

            }
        });
    }

    @Override
    public void onLinkResult(int requestCode, int resultCode, Intent data) {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "onLinkResult, requestCode="+requestCode+"; resultCode="+resultCode);
        }
        if(resultCode== Activity.RESULT_OK){
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(Keys.Preference_Facebook_Linked,true).commit();
            mButtonContainer.updateLinked(true);
            mCallbacks.onLinked(true, R.string.link_facebook_succeed);
        }else{
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(Keys.Preference_Facebook_Linked,false).commit();
            mButtonContainer.updateLinked(false);
            mCallbacks.onLinked(false, R.string.link_facebook_failed);
        }
    }
}
