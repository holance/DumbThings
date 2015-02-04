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

package org.lunci.dumbthing.preference;

import android.content.SharedPreferences;

import org.lunci.dumbthing.R;
import org.lunci.dumbthing.app.DumbThingsApp;


/**
 * Created by Lunci on 2/2/2015.
 */
public class PreferencesTracker implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static PreferencesTracker mInstance;
    private boolean mEnableAutoPrefix = true;
    private boolean mEnableAutoSuffix = true;
    private String mPrefix;
    private String mSuffix;
    private boolean mEnableAutoShare=true;

    protected PreferencesTracker() {

    }

    public static PreferencesTracker getInstance() {
        if (mInstance == null) {
            mInstance = new PreferencesTracker();
        }
        return mInstance;
    }

    public static PreferencesTracker getInstance(SharedPreferences pref) {
        if (mInstance == null) {
            mInstance = new PreferencesTracker();
        }
        mInstance.init(pref);
        return mInstance;
    }

    public boolean isEnableAutoPrefix() {
        return mEnableAutoPrefix;
    }

    public boolean isEnableAutoSuffix() {
        return mEnableAutoSuffix;
    }

    public boolean isEnableAutoShare(){return mEnableAutoShare;}

    public String getPrefix() {
        return mPrefix;
    }

    public String getSuffix() {
        return mSuffix;
    }

    private void init(SharedPreferences sharedPreferences) {
        if (sharedPreferences.contains(Keys.Preference_Default_Prefix)) {
            mPrefix = sharedPreferences.getString(Keys.Preference_Default_Prefix,
                    DumbThingsApp.getContext().getString(R.string.default_sentence));
        } else {
            mPrefix = DumbThingsApp.getContext().getString(R.string.default_sentence);
            sharedPreferences.edit().putString(Keys.Preference_Default_Prefix, mPrefix).commit();
        }

        if (sharedPreferences.contains(Keys.Preference_Default_Suffix)) {
            mSuffix = sharedPreferences.getString(Keys.Preference_Default_Suffix,
                    DumbThingsApp.getContext().getString(R.string.default_tail));
        } else {
            mSuffix = DumbThingsApp.getContext().getString(R.string.default_tail);
            sharedPreferences.edit().putString(Keys.Preference_Default_Suffix, mSuffix).commit();
        }

        if (sharedPreferences.contains(Keys.Preference_Enable_Prefix)) {
            mEnableAutoPrefix = sharedPreferences.getBoolean(Keys.Preference_Enable_Prefix, true);
        } else {
            sharedPreferences.edit().putBoolean(Keys.Preference_Enable_Prefix, mEnableAutoPrefix).commit();
        }

        if (sharedPreferences.contains(Keys.Preference_Enable_Suffix)) {
            mEnableAutoSuffix = sharedPreferences.getBoolean(Keys.Preference_Enable_Suffix, true);
        } else {
            sharedPreferences.edit().putBoolean(Keys.Preference_Enable_Suffix, mEnableAutoSuffix).commit();
        }

        if (sharedPreferences.contains(Keys.Preference_Enable_Auto_Share)) {
            mEnableAutoShare = sharedPreferences.getBoolean(Keys.Preference_Enable_Auto_Share, true);
        } else {
            sharedPreferences.edit().putBoolean(Keys.Preference_Enable_Auto_Share, mEnableAutoShare).commit();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Keys.Preference_Default_Prefix)) {
            mPrefix = sharedPreferences.getString(Keys.Preference_Default_Prefix,
                    DumbThingsApp.getContext().getString(R.string.default_sentence));
        } else if (key.equals(Keys.Preference_Default_Suffix)) {
            mSuffix = sharedPreferences.getString(Keys.Preference_Default_Suffix,
                    DumbThingsApp.getContext().getString(R.string.default_tail));
        } else if (key.equals(Keys.Preference_Enable_Prefix)) {
            mEnableAutoPrefix = sharedPreferences.getBoolean(Keys.Preference_Enable_Prefix, true);
        } else if (key.equals(Keys.Preference_Enable_Suffix)) {
            mEnableAutoSuffix = sharedPreferences.getBoolean(Keys.Preference_Enable_Suffix, true);
        }else if(key.equals(Keys.Preference_Enable_Auto_Share)){
            mEnableAutoShare=sharedPreferences.getBoolean(Keys.Preference_Enable_Auto_Share, true);
        }
    }
}