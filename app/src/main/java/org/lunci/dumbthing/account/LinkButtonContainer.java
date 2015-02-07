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

import android.view.View;
import android.widget.ImageView;

import org.lunci.dumbthing.R;
import org.lunci.dumbthing.preference.PreferencesTracker;

/**
 * Created by Lunci on 2/6/2015.
 */
public class LinkButtonContainer{
    public View getRoot() {
        return mRoot;
    }

    public ImageView getButton() {
        return mButton;
    }

    public View getIndicator() {
        return mIndicator;
    }

    public void setLoginButton(View button){
        mLoginButton=button;
    }

    public View getLoginButton(){
        return mLoginButton;
    }

    private View mRoot;
    private ImageView mButton;
    private View mIndicator;
    private View mLoginButton;
    private PreferencesTracker mPrefTracker=PreferencesTracker.getInstance();

    public void init(View root){
        mRoot=root;
        mButton=(ImageView)root.findViewById(R.id.imageView_link);
        mIndicator=root.findViewById(R.id.imageView_linked_indicator);
        mButton.setTag(root.getId());
        mIndicator.setTag(root.getId());
    }

    public void setIconRes(int resId){
        mButton.setImageResource(resId);
    }

    public void updateLinked(boolean linked){
        try {
            if (linked) {
                mIndicator.setVisibility(View.VISIBLE);
            } else {
                mIndicator.setVisibility(View.INVISIBLE);
            }
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
    }

    public void setBackgroundRes(int resId){
        mButton.setBackgroundResource(resId);
    }
}
