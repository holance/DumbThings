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
import org.lunci.dumbthing.preference.PreferencesTracker;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lunci on 2/4/2015.
 */
public class AutoShareManager {
    private static interface AutoShareCallback{
        void onResult(String moduleName, boolean success);
    }
    private static interface IAutoShare{
        void post(String content, AutoShareCallback callback);
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }
    public final String PostFacebookModule;
    public final String PostTwitterModule;
    
    private static final String TAG=AutoShareManager.class.getSimpleName();
    private Activity mActivity;
    private final HashMap<String, IAutoShare> mShareModules=new HashMap<>();
    
    private class ShareTask extends AsyncTask<String, Integer, Boolean>{
        private final Set<String> mModuleSet;

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
                return false;
            }else{
                length= mModuleSet.size();
                for(String name: mModuleSet){
                    try {
                        mShareModules.get(name).post(params[0],mShareCallback);
                    }catch (NullPointerException ex){
                        ex.printStackTrace();
                    }
                    this.publishProgress(((int) (++progress / (float) length * 100)));
                }
            }
            return true;
        }
    }
    
    private HashMap<String, Boolean> mPostSuccessSet=new HashMap<>();
    private Set<String> mPendingPostSet=new HashSet<>();

    private AutoShareCallback mShareCallback=new AutoShareCallback(){
        @Override
        public void onResult(String moduleName, boolean success) {
            mPostSuccessSet.put(moduleName, success);
            mPendingPostSet.remove(moduleName);
            if(mPendingPostSet.size()==0){
                for(boolean succ:mPostSuccessSet.values()){
                    if(!succ){
                        return;
                    }
                }
                Toast.makeText(mActivity, R.string.share_to_linked_accounts_succeeded, Toast.LENGTH_SHORT).show();
            }
        }
    };
    
    public AutoShareManager(Activity activity){
        mActivity=activity;
        PostFacebookModule=activity.getResources().getString(R.string.facebook);
        PostTwitterModule=activity.getResources().getString(R.string.twitter);
        mShareModules.put(PostFacebookModule, new ShareOnFacebook(activity,PostFacebookModule));
        mShareModules.put(PostTwitterModule, new ShareOnTwitter(PostTwitterModule));
        
    }
    
    public void shareAll(String content){
        final Set<String> moduleSet= PreferencesTracker.getInstance().getAutoSharingAccounts();
        shareOn(moduleSet, content);
    }
    
    public void shareOn(Set<String> moduleSet, String content){
        mPendingPostSet.clear();
        for(String s:moduleSet) {
            mPendingPostSet.add(s);
        }
        mPostSuccessSet.clear();
        final ShareTask task=new ShareTask(moduleSet);
        task.execute(content);
    }

    private class ShareOnFacebook implements IAutoShare{
        private final String TAG=ShareOnFacebook.class.getSimpleName();
        private final String[] PERMISSIONS;
        private final String mName;
        private String mContent;
        private Session mCurrentSession;
        private AutoShareCallback mCallback;
        
        public ShareOnFacebook(Context context, String name){
            mName=name;
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
        
        private Session.StatusCallback mSessionCallback= new Session.StatusCallback() {
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
                        Toast toast = Toast.makeText(mActivity, R.string.send_to_facebook_permssion_failed, Toast.LENGTH_SHORT);
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
        public void post(final String content, AutoShareCallback callback) {
            mCallback=callback;
            mContent=content;
            if(BuildConfig.DEBUG){
                Log.i(TAG, "publishStoryOnFacebook");
            }
            mCurrentSession=Session.getActiveSession();
            Session.OpenRequest openRequest = new Session.OpenRequest(mActivity).setPermissions(PERMISSIONS).setCallback(mSessionCallback);
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

        private void postContent(String content, Session session){
            final Bundle postParams = new Bundle();
            postParams.putString("message", content);

            Request.Callback callback = new Request.Callback() {
                public void onCompleted(Response response) {
                    final FacebookRequestError error = response.getError();
                    if (error != null) {
                        Log.e(TAG, "post on facebook error:" + error.getErrorMessage());
                        final Toast toast = Toast.makeText(mActivity, R.string.send_to_facebook_failed, Toast.LENGTH_SHORT);
                        toast.show();
                        mCallback.onResult(mName, false);
                    }else{
                        if (BuildConfig.DEBUG) {
                            Log.i(TAG, "post on facebook completed");
                        }
                        mCallback.onResult(mName, true);
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
    }

    private class ShareOnTwitter implements IAutoShare{
        private final String TAG=ShareOnTwitter.class.getSimpleName();
        private TwitterSession mCurrentSession;
        private String mContent;
        private AutoShareCallback mCallback;
        private final String mName;
        
        public ShareOnTwitter(String name){
            mName=name;
        }
        
        @Override
        public void post(final String content, AutoShareCallback callback) {
            mCallback=callback;
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

        private void postContent(){
            Twitter.getInstance().core.getApiClient().getStatusesService()
                    .update(mContent, null, null, null, null, null, null, null,new com.twitter.sdk.android.core.Callback<Tweet>(){

                        @Override
                        public void success(Result result) {
                            if(BuildConfig.DEBUG){
                                Log.i(TAG, "twitter post succeeded");
                            }
                            mCallback.onResult(mName, true);
                        }

                        @Override
                        public void failure(TwitterException e) {
                            if(BuildConfig.DEBUG){
                                Log.w(TAG, "twitter post failed:"+e.getMessage());
                                e.printStackTrace();
                            }
                            if(e.getMessage().startsWith("403")){//post duplicated tweet is forbidden by twitter.
                                mCallback.onResult(mName, true);
                                return;
                            }else {
                                final Toast toast = Toast.makeText(mActivity, R.string.send_to_twitter_failed, Toast.LENGTH_SHORT);
                                toast.show();
                                mCallback.onResult(mName, false);
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
    
    public void onRestoreInstanceState(Bundle inState){
        Session.restoreSession(mActivity, null, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState sessionState, Exception e) {
                if (e!=null) {
                    Log.i(TAG, "Exception:"+e.getMessage());
                }
            }
        }, inState);
    }
    
    public void onSaveInstanceState(Bundle outState){
        if(Session.getActiveSession()!=null){
            Session session = Session.getActiveSession();
            Session.saveSession(session, outState);
        }
    }
}
