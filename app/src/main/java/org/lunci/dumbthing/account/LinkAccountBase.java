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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;

/**
 * Created by Lunci on 2/6/2015.
 */
public abstract class LinkAccountBase implements ILinkAccount{
    private static final String TAG=LinkAccountBase.class.getSimpleName();
    private static final LinkAccoutCallbacks DummyCallbacks=new LinkAccoutCallbacks() {
        @Override
        public void onLinked(boolean succ, int toastId) {

        }
    };

    protected LinkAccoutCallbacks mCallbacks=DummyCallbacks;
    private final Activity mActivity;

    public LinkAccountBase(Activity activity, LinkAccoutCallbacks callbacks){
        mActivity=activity;
        mCallbacks=callbacks;
    }

    protected Activity getActivity(){
        return mActivity;
    }

    protected void callUnlinkConfirmDialog(int messageResId, DialogInterface.OnClickListener onConfirmListener){
        if(BuildConfig.DEBUG){
            Log.i(TAG, "callUnlinkConfirmDialog");
        }
        final AlertDialog.Builder builder=new AlertDialog.Builder(mActivity);
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
