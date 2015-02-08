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

package org.lunci.dumbthing.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import org.lunci.dumbthing.BuildConfig;
import org.lunci.dumbthing.R;
import org.lunci.dumbthing.activity.SettingsActivity;
import org.lunci.dumbthing.dataModel.GlobalMessages;
import org.lunci.dumbthing.preference.PreferencesTracker;

import de.greenrobot.event.EventBus;

/**
 * Created by Lunci on 2/2/2015.
 */
public class Utils {

    private static final String TAG=Utils.class.getSimpleName();

    public static Intent shareText(Context context, String text) {
        try {
            if(BuildConfig.DEBUG){
                Log.i(TAG, "Sharing text:" + text);
            }
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getResources()
                    .getString(R.string.default_sentence));
            intent.putExtra(Intent.EXTRA_TEXT, text);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                final Intent chooserIntent= Intent.createChooser(intent, context
                        .getResources().getString(R.string.share_dumb_thing_with));
                context.startActivity(chooserIntent);
                return chooserIntent;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void autoShareText(final Context context, final String text){
        final AlertDialog.Builder builder= new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_share_auto);
        builder.setCancelable(true);
        builder.setTitle(R.string.share_to_linked_accounts);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EventBus.getDefault().post(new GlobalMessages.PostContent(text));
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        String message="";
        if(PreferencesTracker.getInstance().isFacebookLinked()){
            message+="Facebook;";
        }
        if(PreferencesTracker.getInstance().isTwitterLinked()){
            message+="Twitter;";
        }
        if(PreferencesTracker.getInstance().isGooglePlusLinked()){
            message+="Google Plus;";
        }
        builder.setMessage(message);
        builder.create().show();
        
    }

    public static String buildDumbContent(String orgContent){
        final PreferencesTracker pref=PreferencesTracker.getInstance();
        if(pref.isEnableAutoPrefix()){
            orgContent= pref.getPrefix()+" "+orgContent;
        }
        if(pref.isEnableAutoSuffix()){
           orgContent= orgContent+" "+pref.getSuffix();
        }
        if(BuildConfig.DEBUG){
            Log.i(TAG, "content="+orgContent);
        }
        return orgContent;
    }

    public static void startSettingActivity(Context context){
        final Intent intent=new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }
}
