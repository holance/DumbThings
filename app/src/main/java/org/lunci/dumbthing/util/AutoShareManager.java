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

package org.lunci.dumbthing.util;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;

import java.util.Collection;
import java.util.List;

/**
 * Created by Lunci on 2/4/2015.
 */
public class AutoShareManager {
    private static final String TAG=AutoShareManager.class.getSimpleName();
    private Activity mActivity;
    private String[] PERMISSIONS;
    
    public AutoShareManager(Activity activity){
        mActivity=activity;
        PERMISSIONS=activity.getResources().getStringArray(R.array.facebook_permissions);
    }

    public void publishStoryOnFacebook(final String content) {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "publishStoryOnFacebook");
        }
        Session.openActiveSession(mActivity, true, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState sessionState, Exception e) {
                if(BuildConfig.DEBUG){
                    Log.i(TAG, "onSessionStateChange");
                }
                if (session!=null && sessionState.isOpened()) {
                    Log.i(TAG, "Facebook Logged in...");
                    // Check for publish permissions
                    List<String> permissions = session.getPermissions();
                    if (!isSubsetOf(PERMISSIONS, permissions)) {
                        Log.w(TAG, "Permission denied. Please relink facebook.");
                        Toast toast=Toast.makeText(mActivity, R.string.send_to_facebook_failed, Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                    final Bundle postParams = new Bundle();
                    postParams.putString("message", content);

                    Request.Callback callback= new Request.Callback() {
                        public void onCompleted(Response response) {
                            if(BuildConfig.DEBUG){
                                Log.i(TAG, "onCompleted");
                                try {
                                    Session.getActiveSession().close();
                                }catch (NullPointerException ex){
                                    ex.printStackTrace();
                                }
                            }
                            final FacebookRequestError error = response.getError();
                            if (error != null) {
                                Log.e(TAG, "post on facebook error:"+error.getErrorMessage());
                            }
                        }
                    };

                    final Request request = new Request(session, "me/feed", postParams,
                            HttpMethod.POST, callback);
                    if(BuildConfig.DEBUG){
                        Log.i(TAG, "start publishing");
                    }
                    final RequestAsyncTask task = new RequestAsyncTask(request);
                    task.execute();

                } else if (sessionState.isClosed()) {
                    Log.i(TAG, "Logged out...");
                }
            }
        });
    }

    public void publishStoryOnTwitter(final String content){
        if(BuildConfig.DEBUG){
            Log.i(TAG, "publishStoryOnTwitter:"+content);
        }
        Twitter.logIn(mActivity, new com.twitter.sdk.android.core.Callback(){

            @Override
            public void success(Result result) {
                if(BuildConfig.DEBUG){
                    Log.i(TAG, "twitter logged in");
                }
                Twitter.getInstance().core.getApiClient().getStatusesService().update(content, null, null, null, null, null, null, null,new com.twitter.sdk.android.core.Callback(){

                    @Override
                    public void success(Result result) {
                        if(BuildConfig.DEBUG){
                            Log.w(TAG, "twitter post succeeded");
                        }
                    }

                    @Override
                    public void failure(TwitterException e) {
                        if(BuildConfig.DEBUG){
                            Log.w(TAG, "twitter post failed");
                        }
                        final Toast toast=Toast.makeText(mActivity, R.string.send_to_twitter_failed, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }

            @Override
            public void failure(TwitterException e) {
                if(BuildConfig.DEBUG){
                    Log.w(TAG, "twitter logged in failed");
                }
                final Toast toast=Toast.makeText(mActivity, R.string.send_to_twitter_failed, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private boolean isSubsetOf(String[] subset, Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }
}
