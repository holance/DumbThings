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
import android.widget.Toast;

import org.lunci.dumbthing.R;
import org.lunci.dumbthing.account.ILinkAccount;
import org.lunci.dumbthing.account.LinkButtonContainer;
import org.lunci.dumbthing.account.LinkFacebook;
import org.lunci.dumbthing.account.LinkTwitter;
import org.lunci.dumbthing.preference.PreferencesTracker;
import org.lunci.dumbthing.util.Utils;


/**
 * Created by Lunci on 2/5/2015.
 */
public class LinkShareDialog extends DialogFragment {
    private static final String TAG = LinkShareDialog.class.getSimpleName();
    private final LinkAccountManager mManager = new LinkAccountManager();
    private String mFacebookPublishPermission;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFacebookPublishPermission = getResources().getString(R.string.facebook_publish_permission);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.auto_share_link_list, container);
        mManager.setupButtons(view);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getResources().getString(R.string.link_accounts));
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            dialog.getWindow().setIcon(R.drawable.ic_link_share);
        }
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private enum CurrentMode {None, Facebook, Twitter, GooglePlus}

    private class LinkAccountManager implements ILinkAccount.LinkAccoutCallbacks {
        private final LinkButtonContainer mFacebookButton = new LinkButtonContainer();
        private final LinkButtonContainer mTwitterButton = new LinkButtonContainer();
        private final LinkButtonContainer mGooglePlusButton = new LinkButtonContainer();
        private final LinkButtonContainer mLinkedInButton = new LinkButtonContainer();
        private CurrentMode mCurrentMode = CurrentMode.None;
        private ILinkAccount mAccountLinker;

        public LinkAccountManager() {


        }

        public LinkButtonContainer getFacebookButton() {
            return mFacebookButton;
        }

        public LinkButtonContainer getTwitterButton() {
            return mTwitterButton;
        }

        public LinkButtonContainer getGooglePlusButton() {
            return mGooglePlusButton;
        }

        public LinkButtonContainer getLinkedInButton() {
            return mLinkedInButton;
        }

        public ILinkAccount getAccountLinker() {
            return mAccountLinker;
        }

        public void setupButtons(View view) {
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

            final PreferencesTracker mPref = PreferencesTracker.getInstance();

            mFacebookButton.updateLinked(mPref.isFacebookLinked());

            mTwitterButton.updateLinked(mPref.isTwitterLinked());

            mGooglePlusButton.updateLinked(mPref.isGooglePlusLinked());

            mFacebookButton.getButton().setOnClickListener(mClickListener);

            mTwitterButton.getButton().setOnClickListener(mClickListener);

            mGooglePlusButton.getButton().setOnClickListener(mClickListener);

            mLinkedInButton.getButton().setOnClickListener(mClickListener);
        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            mAccountLinker.onLinkResult(requestCode, resultCode, data);
            mCurrentMode = CurrentMode.None;
        }

        @Override
        public void onLinked(boolean succ, int toastId) {
            final Toast toast = Toast.makeText(getActivity(), toastId, Toast.LENGTH_SHORT);
            toast.show();
        }

        private View.OnClickListener mClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.getTag() == null) {
                    Log.e(TAG, "Link button tag is null");
                    return;
                }else if(!Utils.checkNetworkConnection()){
                    Toast.makeText(getActivity(), R.string.network_not_available, Toast.LENGTH_SHORT).show();
                    return;
                }
                final int tag = (int) v.getTag();
                switch (tag) {
                    case R.id.link_facebook_layout:
                        mCurrentMode = CurrentMode.Facebook;
                        mAccountLinker = new LinkFacebook(getActivity(), LinkAccountManager.this);
                        mAccountLinker.setButtonContainer(mFacebookButton);
                        break;
                    case R.id.link_twitter_layout:
                        mCurrentMode = CurrentMode.Twitter;
                        mAccountLinker = new LinkTwitter(getActivity(), LinkAccountManager.this);
                        mAccountLinker.setButtonContainer(mTwitterButton);
                        break;
                    default:
                        mAccountLinker = null;
                        Log.w(TAG, "account linker not implemented yet.");
                        break;
                }
                if (mAccountLinker == null)
                    return;
                else if (mAccountLinker.isLinked()) {
                    mAccountLinker.unlink();
                } else {
                    switch (mCurrentMode) {
                        case Facebook:
                            mAccountLinker.link(LinkShareDialog.this);
                            break;
                        default:
                            mAccountLinker.link(null);
                            break;
                    }
                }
            }
        };
    }
}
