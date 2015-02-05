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

package org.lunci.dumbthing.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;

import java.util.Arrays;

/**
 * Created by Lunci on 2/5/2015.
 */
public class LinkShareDialog extends DialogFragment {
    private final LinkAccountManager mManager=new LinkAccountManager();
    private static final String TAG=LinkShareDialog.class.getSimpleName();
    private UiLifecycleHelper uiHelper;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState sessionState, Exception e) {
                if(BuildConfig.DEBUG){
                    Log.i(TAG, "StatusCallback call");
                }
            }
        });
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.auto_share_link_list, container);
        mManager.setupButtons(view);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Dialog dialog=super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getResources().getString(R.string.link_accounts));
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            dialog.getWindow().setIcon(R.drawable.ic_link_share);
        }
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
    
    private class LinkAccountManager{
        private View mFacebookButton;
        private View mTwitterButton;
        private View mGooglePlusButton;
        
        public LinkAccountManager(){

            
        }
        
        public void setupButtons(View view){
            mFacebookButton=view.findViewById(R.id.imageView_link_facebook);
            mFacebookButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    linkFacebook();
                }
            });
        }
        
        private void linkFacebook(){
            com.facebook.widget.LoginButton button=new com.facebook.widget.LoginButton(getActivity());
            button.setFragment(LinkShareDialog.this);
            button.setPublishPermissions(Arrays.asList("publish_actions"));
            button.performClick();
        }
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "onSessionStateChange");
        }
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }
}
