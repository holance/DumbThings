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

package org.lunci.dumbthing.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.lunci.dumbthing.R;
import org.lunci.dumbthing.preference.PreferencesTracker;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Lunci Hua on 2/1/2015.
 */
public class DumbThingsApp extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static String TWITTER_KEY;
    private static String TWITTER_SECRET;
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        TWITTER_KEY=getResources().getString(R.string.twitter_key);
        TWITTER_SECRET=getResources().getString(R.string.twitter_secrete);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        mContext = getApplicationContext();
        final SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(mContext);
        prefs.registerOnSharedPreferenceChangeListener(PreferencesTracker.getInstance(prefs));
    }

    @Override
    public void onTerminate(){
        PreferenceManager.getDefaultSharedPreferences(mContext).unregisterOnSharedPreferenceChangeListener(PreferencesTracker.getInstance());
        super.onTerminate();
    }

    public static Context getContext() {
        return mContext;
    }
}
