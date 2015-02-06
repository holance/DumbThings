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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;


/**
 * Created by Lunci on 2/5/2015.
 */
public class LinkShareDialog extends DialogFragment {
    private final LinkAccountManager mManager=new LinkAccountManager();
    private static final String TAG=LinkShareDialog.class.getSimpleName();
    private UiLifecycleHelper uiHelper;
    private String mFacebookPublishPermission;

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
        mFacebookPublishPermission=getResources().getString(R.string.facebook_publish_permission);
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
    
    private class LinkButtonLayout{
        public View getRoot() {
            return mRoot;
        }

        public ImageView getButton() {
            return mButton;
        }

        public View getIndicator() {
            return mIndicator;
        }

        private View mRoot;
        private ImageView mButton;
        private View mIndicator;
        
        public void init(View root){
            mRoot=root;
            mButton=(ImageView)root.findViewById(R.id.imageView_link);
            mIndicator=root.findViewById(R.id.imageView_linked_indicator);
        }
        
        public void setIconRes(int resId){
            mButton.setImageResource(resId);

        }
        
        public void setBackgroundRes(int resId){
            mButton.setBackgroundResource(resId);
//            mButton.setPadding(10,10,10,10);
//            mButton.invalidate();
        }
    }
    
    private class LinkAccountManager{
        public LinkButtonLayout getFacebookButton() {
            return mFacebookButton;
        }

        public LinkButtonLayout getTwitterButton() {
            return mTwitterButton;
        }

        public LinkButtonLayout getGooglePlusButton() {
            return mGooglePlusButton;
        }

        public LinkButtonLayout getLinkedInButton() {
            return mLinkedInButton;
        }

        private final LinkButtonLayout mFacebookButton=new LinkButtonLayout();
        private final LinkButtonLayout mTwitterButton=new LinkButtonLayout();
        private final LinkButtonLayout mGooglePlusButton=new LinkButtonLayout();
        private final LinkButtonLayout mLinkedInButton=new LinkButtonLayout();
        
        public LinkAccountManager(){

            
        }
        
        public void setupButtons(View view){
            mFacebookButton.init(view.findViewById(R.id.link_facebook_layout));
            mTwitterButton.init(view.findViewById(R.id.link_twitter_layout));
            mGooglePlusButton.init(view.findViewById(R.id.link_google_layout));
            mLinkedInButton.init(view.findViewById(R.id.link_linkedin_layout));
            
            mFacebookButton.setIconRes(R.drawable.ic_facebook);
            mFacebookButton.setBackgroundRes(R.drawable.circle_facebook);
            
            mTwitterButton.setIconRes(R.drawable.ic_twitter);
            mTwitterButton.setBackgroundRes(R.drawable.circle_twitter);
            
            mGooglePlusButton.setIconRes(R.drawable.ic_google_plus);
            mGooglePlusButton.setBackgroundRes(R.drawable.circle_google_plus);
            
            if(Session.getActiveSession()==null || Session.getActiveSession().isClosed()
                    || !Session.getActiveSession().isPermissionGranted(mFacebookPublishPermission)){
                mFacebookButton.getIndicator().setVisibility(View.INVISIBLE);
            }
            mFacebookButton.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   if(Session.getActiveSession()==null || !Session.getActiveSession().isPermissionGranted(mFacebookPublishPermission)) {
                       linkFacebook();
                   }else{
                       unlinkFacebook();
                   }
                }
            });
            
            mTwitterButton.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    
                }
            });
            
            mGooglePlusButton.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    
                }
            });
            
            mLinkedInButton.getButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    
                }
            });
        }
        
        private void linkFacebook(){
            if(BuildConfig.DEBUG){
                Log.i(TAG, "link facebook");
            }
            com.facebook.widget.LoginButton button=new com.facebook.widget.LoginButton(getActivity());
            button.setFragment(LinkShareDialog.this);
            button.setPublishPermissions(getResources().getStringArray(R.array.facebook_permissions));
            button.performClick();
        }
        
        private void unlinkFacebook(){
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
        
        private void linkTwitter(){
            if(BuildConfig.DEBUG){
                Log.i(TAG, "linkTwitter");
            }

        }
        
        private void unlinkTwitter(){
            if(BuildConfig.DEBUG){
                Log.i(TAG, "unlinkTwitter");
            }
            callUnlinkConfirmDialog(R.string.unlink_twitter_warning, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });           
        }
        
        private void linkGooglePlus(){
            if(BuildConfig.DEBUG){
                Log.i(TAG, "linkGooglePlus");
            }
            
        }
        
        private void unlinkGooglePlus(){
            if(BuildConfig.DEBUG){
                Log.i(TAG, "unlinkGooglePlus");
            }
            
        }
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if(BuildConfig.DEBUG){
            Log.i(TAG, "onSessionStateChange");
        }
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
            mManager.getFacebookButton().getIndicator().setVisibility(View.VISIBLE);
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            mManager.getFacebookButton().getIndicator().setVisibility(View.INVISIBLE);
        }
    }
    
    private void callUnlinkConfirmDialog(int messageResId, DialogInterface.OnClickListener onConfirmListener){
        final AlertDialog.Builder builder=new AlertDialog.Builder(this.getActivity());
        builder.setCancelable(true);
        builder.setIcon(R.drawable.ic_warning);
        builder.setMessage(messageResId);
        builder.setPositiveButton(android.R.string.ok, onConfirmListener);
        
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final Dialog dialog=builder.create();
        dialog.show();
    }
}
