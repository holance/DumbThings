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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Lunci on 2/4/2015.
 */
public class AutoShareManager {
    private static interface IAutoShare{
        void post(String content);
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }
    public final String PostFacebookModule;
    public final String PostTwitterModule;
    
    private static final String TAG=AutoShareManager.class.getSimpleName();
    private Activity mActivity;
    private final HashMap<String, IAutoShare> mShareModules=new HashMap<>();
    private class ShareTask extends AsyncTask<String, Integer, Boolean>{
        private final Set<String> mModuleSet;
        public ShareTask(){
            mModuleSet =null;
        }
        
        public ShareTask(Set<String> moduleList){
            mModuleSet =moduleList;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            if(params==null || params.length!=1)
                return false;
            int progress=0;
            int length;
            if(mModuleSet ==null){
                length=mShareModules.size();
                for(IAutoShare module: mShareModules.values()){
                    module.post(params[0]);
                    this.publishProgress(((int) (++progress / (float) length * 100)));
                }
            }else{
                length= mModuleSet.size();
                for(String name: mModuleSet){
                    try {
                        mShareModules.get(name).post(params[0]);
                    }catch (NullPointerException ex){
                        ex.printStackTrace();
                    }
                    this.publishProgress(((int) (++progress / (float) length * 100)));
                }
            }
            return true;
        }
    }
    
    public AutoShareManager(Activity activity){
        mActivity=activity;
        PostFacebookModule=activity.getResources().getString(R.string.facebook);
        PostTwitterModule=activity.getResources().getString(R.string.twitter);
        mShareModules.put(PostFacebookModule, new ShareOnFacebook(activity));
        mShareModules.put(PostTwitterModule, new ShareOnTwitter());
        
    }
    
    public void shareAll(String content){
        final ShareTask task=new ShareTask();
        task.execute(content);
    }
    
    public void shareOn(Set<String> moduleSet, String content){
        final ShareTask task=new ShareTask(moduleSet);
        task.execute(content);
    }

    private class ShareOnFacebook implements IAutoShare{
        private final String TAG=ShareOnFacebook.class.getSimpleName();
        private final String[] PERMISSIONS;
        private String mContent;
        private Session mCurrentSession;
        
        public ShareOnFacebook(Context context){
            PERMISSIONS=context.getResources().getStringArray(R.array.facebook_permissions);
        }
        
        private boolean isSubsetOf(String[] subset, Collection<String> superset) {
            for (String string : subset) {
                if (!superset.contains(string)) {
                    return false;
                }
            }
            return true;
        }
        
        private Session.StatusCallback mCallback= new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState sessionState, Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "onSessionStateChange");
                }
                if (e!=null) {
                    Log.i(TAG, "Exception:"+e.getMessage());
                }
                if (session != null && sessionState.isOpened()) {
                    if (BuildConfig.DEBUG) {
                        Log.i(TAG, "Facebook Logged in...");
                    }
                    // Check for publish permissions
                    List<String> permissions = session.getPermissions();
                    if (!isSubsetOf(PERMISSIONS, permissions)) {
                        Log.w(TAG, "Permission denied. Please relink facebook.");
                        Toast toast = Toast.makeText(mActivity, R.string.send_to_facebook_failed, Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                    postContent(mContent, session);
                } else {
                    Log.i(TAG, "Unable to find facebook active session.");
                }
            }
        };

        @Override
        public void post(final String content) {
            mContent=content;
            if(BuildConfig.DEBUG){
                Log.i(TAG, "publishStoryOnFacebook");
            }
            mCurrentSession=Session.getActiveSession();
            Session.OpenRequest openRequest = new Session.OpenRequest(mActivity).setPermissions(PERMISSIONS).setCallback(mCallback);
            if(mCurrentSession==null) {
                if(BuildConfig.DEBUG){
                    Log.i(TAG, "create new session");
                }
                mCurrentSession = new Session.Builder(mActivity).build();
                if(BuildConfig.DEBUG){
                    Log.i(TAG, "session state="+mCurrentSession.getState());
                }
                if (SessionState.CREATED.equals(mCurrentSession.getState())||SessionState.CREATED_TOKEN_LOADED.equals(mCurrentSession.getState())){
                    Session.setActiveSession(mCurrentSession);
                    if(BuildConfig.DEBUG){
                        Log.i(TAG, "open session for publish");
                    }
                    mCurrentSession.openForPublish(openRequest);
                }
            }else{
                if(BuildConfig.DEBUG){
                    Log.i(TAG, "open session for publish");
                }
                mCurrentSession.openForPublish(openRequest);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if(BuildConfig.DEBUG){
                Log.i(TAG, "onActivityResult, requestCode="+requestCode+"; resultCode="+resultCode);
            }
            if(mCurrentSession!=null)
                mCurrentSession.onActivityResult(mActivity, requestCode, resultCode, data);
            else{
                Log.e(TAG, "mCurrentSession is null");
            }
        }
    }
    
    private void postContent(String content, Session session){
        final Bundle postParams = new Bundle();
        postParams.putString("message", content);

        Request.Callback callback = new Request.Callback() {
            public void onCompleted(Response response) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "post on facebook completed");
                }
                final FacebookRequestError error = response.getError();
                if (error != null) {
                    Log.e(TAG, "post on facebook error:" + error.getErrorMessage());
                    final Toast toast = Toast.makeText(mActivity, R.string.send_to_facebook_failed, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };

        final Request request = new Request(session, "me/feed", postParams,
                HttpMethod.POST, callback);
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "start publishing");
        }
        final RequestAsyncTask task = new RequestAsyncTask(request);
        task.execute();
        
    }
    
    
    private class ShareOnTwitter implements IAutoShare{
        private final String TAG=ShareOnTwitter.class.getSimpleName();
        private TwitterSession mCurrentSession;
        private String mContent;
        
        @Override
        public void post(final String content) {
            mContent=content;
            if(BuildConfig.DEBUG){
                Log.i(TAG, "publishStoryOnTwitter:"+content);
            }
            mCurrentSession=Twitter.getSessionManager().getActiveSession();
            if(mCurrentSession!=null){
                if(BuildConfig.DEBUG){
                    Log.i(TAG, "get session succeeded");
                }
                postContent();
            }else {
                Log.w(TAG, "get twitter session failed");
             //   Twitter.logIn(mActivity, mCallback);
            }
        }
//
//        private com.twitter.sdk.android.core.Callback<TwitterSession> mCallback=new com.twitter.sdk.android.core.Callback<TwitterSession>(){
//
//            @Override
//            public void success(Result result) {
//                mCurrentSession=(TwitterSession)result.data;
//                postContent();
//            }
//
//            @Override
//            public void failure(TwitterException e) {
//                Log.w(TAG, "Log into twitter failed:"+e.getMessage());
//                final Toast toast=Toast.makeText(mActivity, R.string.send_to_twitter_failed, Toast.LENGTH_SHORT);
//                toast.show();
//            }
//        };
        
        private void postContent(){
            Twitter.getInstance().core.getApiClient().getStatusesService()
                    .update(mContent, null, null, null, null, null, null, null,new com.twitter.sdk.android.core.Callback<Tweet>(){

                        @Override
                        public void success(Result result) {
                            if(BuildConfig.DEBUG){
                                Log.w(TAG, "twitter post succeeded");
                            }
                        }

                        @Override
                        public void failure(TwitterException e) {
                            if(BuildConfig.DEBUG){
                                Log.w(TAG, "twitter post failed:"+e.getMessage());
                                e.printStackTrace();
                            }
                            if(e.getMessage().startsWith("403")){//post duplicated tweet is forbidden by twitter.
                                return;
                            }else {
                                final Toast toast = Toast.makeText(mActivity, R.string.send_to_twitter_failed, Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if(BuildConfig.DEBUG){
                Log.i(TAG, "onActivityResult, requestCode="+requestCode+"; resultCode="+resultCode);
            }

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "onActivityResult, requestCode="+requestCode+"; resultCode="+resultCode);
        }
        for(IAutoShare module:mShareModules.values()){
            module.onActivityResult(requestCode, resultCode, data);
        }
    }
}
