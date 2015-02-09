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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenSource;
import com.facebook.Session;
import com.facebook.TokenCachingStrategy;
import com.facebook.internal.NativeProtocol;
import com.facebook.widget.FacebookDialog;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;
import org.lunci.dumbthing.preference.Keys;
import org.lunci.dumbthing.preference.PreferencesTracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Lunci on 2/6/2015.
 */
public class LinkFacebook extends LinkAccountBase {
    private static final String TAG=LinkFacebook.class.getSimpleName();
    private static final String DIALOG_CALL_BUNDLE_SAVE_KEY =
            "com.facebook.UiLifecycleHelper.pendingFacebookDialogCallKey";
    private FacebookDialog.PendingCall pendingFacebookDialogCall;
    private LinkButtonContainer mButtonContainer;

    public LinkFacebook(Activity activity, LinkAccoutCallbacks callbacks){
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
        return PreferencesTracker.getInstance().isFacebookLinked();
    }

    @Override
    public void link(Object args) {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "link facebook");
        }
        if(Session.getActiveSession()!=null){
            Session.getActiveSession().closeAndClearTokenInformation();
        }
        com.facebook.widget.LoginButton button=new com.facebook.widget.LoginButton(getActivity());
        if(args instanceof Fragment)
        button.setFragment((Fragment)args);
        button.setPublishPermissions(getActivity().getResources().getStringArray(R.array.facebook_permissions));
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
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                try {
                    final String token = pref.getString(Keys.Preference_Facebook_AccessToken, "");
                    final long expire = pref.getLong(Keys.Preference_Facebook_AccessToken_Expire, 0);
                    final Date expDate = new Date();
                    expDate.setTime(expire);
                    final long lastRefresh = pref.getLong(Keys.Preference_Facebook_Last_Refresh_Date, 0);
                    final Date lastDate = new Date();
                    lastDate.setTime(lastRefresh);
                    final Set<String> permissions = pref.getStringSet(Keys.Preference_Facebook_Permissions, null);
                    final List<String> permissionList = new ArrayList<>();
                    permissionList.addAll(permissions);
                    //  final Set<String> declinedPermissions=pref.getStringSet(Keys.Preference_Facebook_Declined_Permissions, null);
                    final String source = pref.getString(Keys.Preference_Facebook_AccessToken_Source, "");
                    final AccessTokenSource tokenSource = AccessTokenSource.valueOf(source);
                    final AccessToken accessToken = AccessToken.createFromExistingAccessToken(token, expDate, lastDate, tokenSource, permissionList);
                    final Session session = Session.openActiveSessionWithAccessToken(getActivity(), accessToken, null);
                    if (session != null)
                        session.closeAndClearTokenInformation();
                }catch (NullPointerException ex){
                    ex.printStackTrace();
                }finally {
                    final SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(Keys.Preference_Facebook_Linked, false).commit();
                    mButtonContainer.updateLinked(false);
                }
            }
        });
    }

    @Override
    public void onLinkResult(int requestCode, int resultCode, Intent data) {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "onLinkResult, requestCode="+requestCode+"; resultCode="+resultCode);
        }
        if(resultCode== Activity.RESULT_OK){
            Session session=Session.getActiveSession();
            if(session!=null){
                session.onActivityResult(getActivity(), requestCode, resultCode, data);
            }
            handleFacebookDialogActivityResult(requestCode, resultCode, data, null);
            final SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(getActivity());
            final SharedPreferences.Editor editor=pref.edit();
            editor.putBoolean(Keys.Preference_Facebook_Linked, true);
            final Bundle bundle=session.getAuthorizationBundle();
            if(bundle!=null) {
                Log.w(TAG, "using auth bundle");
                editor.putString(Keys.Preference_Facebook_AccessToken, TokenCachingStrategy.getToken(bundle));
                editor.putLong(Keys.Preference_Facebook_AccessToken_Expire, TokenCachingStrategy.getExpirationMilliseconds(bundle));
                editor.putLong(Keys.Preference_Facebook_Last_Refresh_Date, TokenCachingStrategy.getLastRefreshMilliseconds(bundle));
                editor.putString(Keys.Preference_Facebook_AccessToken_Source, TokenCachingStrategy.getSource(bundle).name());
                final Set<String> permissions = new HashSet<>();
                for (String s : TokenCachingStrategy.getPermissions(bundle)) {
                    permissions.add(s);
                }
                editor.putStringSet(Keys.Preference_Facebook_Permissions, permissions);
                final Set<String> declined_permissions = new HashSet<>();
                for (String s : session.getDeclinedPermissions()) {
                    declined_permissions.add(s);
                }
                editor.putStringSet(Keys.Preference_Facebook_Declined_Permissions, declined_permissions);
            }else{
                Log.w(TAG, "bundle is null");
                editor.putString(Keys.Preference_Facebook_AccessToken, session.getAccessToken());
                editor.putLong(Keys.Preference_Facebook_AccessToken_Expire, session.getExpirationDate().getTime());
                final Set<String> permissions = new HashSet<>();
                for (String s : session.getPermissions()) {
                    permissions.add(s);
                }
                editor.putStringSet(Keys.Preference_Facebook_Permissions, permissions);
                final Set<String> declined_permissions = new HashSet<>();
                for (String s : session.getDeclinedPermissions()) {
                    declined_permissions.add(s);
                }
                editor.putStringSet(Keys.Preference_Facebook_Declined_Permissions, declined_permissions);
            }
            editor.commit();
            mButtonContainer.updateLinked(true);
            mCallbacks.onLinked(true, R.string.link_facebook_succeed);
        }else{
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(Keys.Preference_Facebook_Linked,false).commit();
            mButtonContainer.updateLinked(false);
            mCallbacks.onLinked(false, R.string.link_facebook_failed);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            pendingFacebookDialogCall = savedInstanceState.getParcelable(DIALOG_CALL_BUNDLE_SAVE_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Session.saveSession(Session.getActiveSession(), outState);
        outState.putParcelable(DIALOG_CALL_BUNDLE_SAVE_KEY, pendingFacebookDialogCall);
    }

    private boolean handleFacebookDialogActivityResult(int requestCode, int resultCode, Intent data,
                                                       FacebookDialog.Callback facebookDialogCallback) {
        if (pendingFacebookDialogCall == null || pendingFacebookDialogCall.getRequestCode() != requestCode) {
            return false;
        }

        if (data == null) {
            // We understand the request code, but have no Intent. This can happen if the called Activity crashes
            // before it can be started; we treat this as a cancellation because we have no other information.
            cancelPendingAppCall(facebookDialogCallback);
            return true;
        }

        String callIdString = data.getStringExtra(NativeProtocol.EXTRA_PROTOCOL_CALL_ID);
        UUID callId = null;
        if (callIdString != null) {
            try {
                callId = UUID.fromString(callIdString);
            } catch (IllegalArgumentException exception) {
            }
        }

        // Was this result for the call we are waiting on?
        if (callId != null && pendingFacebookDialogCall.getCallId().equals(callId)) {
            // Yes, we can handle it normally.
            FacebookDialog.handleActivityResult(getActivity(), pendingFacebookDialogCall, requestCode, data,
                    facebookDialogCallback);
        } else {
            // No, send a cancellation error to the pending call and ignore the result, because we
            // don't know what to do with it.
            cancelPendingAppCall(facebookDialogCallback);
        }

        pendingFacebookDialogCall = null;
        return true;
    }

    private void cancelPendingAppCall(FacebookDialog.Callback facebookDialogCallback) {
        if (facebookDialogCallback != null) {
            Intent pendingIntent = pendingFacebookDialogCall.getRequestIntent();

            Intent cancelIntent = new Intent();
            cancelIntent.putExtra(NativeProtocol.EXTRA_PROTOCOL_CALL_ID,
                    pendingIntent.getStringExtra(NativeProtocol.EXTRA_PROTOCOL_CALL_ID));
            cancelIntent.putExtra(NativeProtocol.EXTRA_PROTOCOL_ACTION,
                    pendingIntent.getStringExtra(NativeProtocol.EXTRA_PROTOCOL_ACTION));
            cancelIntent.putExtra(NativeProtocol.EXTRA_PROTOCOL_VERSION,
                    pendingIntent.getIntExtra(NativeProtocol.EXTRA_PROTOCOL_VERSION, 0));
            cancelIntent.putExtra(NativeProtocol.STATUS_ERROR_TYPE, NativeProtocol.ERROR_UNKNOWN_ERROR);

            FacebookDialog.handleActivityResult(getActivity(), pendingFacebookDialogCall,
                    pendingFacebookDialogCall.getRequestCode(), cancelIntent, facebookDialogCallback);
        }
        pendingFacebookDialogCall = null;
    }
}
