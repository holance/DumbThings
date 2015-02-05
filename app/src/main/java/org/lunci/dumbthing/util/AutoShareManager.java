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
import android.view.View;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

/**
 * Created by Lunci on 2/4/2015.
 */
public class AutoShareManager {
    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;
    private String mAppId="";
    private Activity mActivity;
    
    public AutoShareManager(Activity activity){
        mActivity=activity;
        mFacebook = new Facebook(mAppId);
        mAsyncRunner = new AsyncFacebookRunner(mFacebook);
    }

    public void shareOnFacebook(View v, String text) {
        mFacebook.dialog(mActivity, "feed", new Facebook.DialogListener() {

            @Override
            public void onFacebookError(FacebookError error) {
                Toast.makeText(mActivity, "Post fail " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(DialogError error) {
                Toast.makeText(mActivity, "Post fail due to " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete(Bundle values) {
                Toast.makeText(mActivity, "Post success.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(mActivity, "Cancle by user!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
