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

package org.lunci.dumbthing.contentProvider;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.Gravity;
import android.widget.Toast;

import org.lunci.dumbthing.app.DumbThingsApp;
import org.lunci.dumbthing.database.SQLiteDefault;
import org.lunci.dumbthing.database.SQLiteHelper;
import org.lunci.dumbthing.database.Tools;
import org.lunci.dumbthing.io.DefaultFileValues;
import org.lunci.dumbthing.io.IOHelper;

import java.io.File;

/**
 * Created by Lunci on 2/1/2015.
 */
public class ContentProvider {
    protected static SQLiteOpenHelper mDatabase;
    protected final File mBackupPath;
    protected static Context mContext;
    protected static SQLiteDatabase mReadableDatabase;
    protected static SQLiteDatabase mWritableDatabase;
    
    private static ContentProvider mObject;
    
    public static ContentProvider getInstance(){
        if(mObject==null){
            mObject=new ContentProvider();
        }
        return mObject;
    }

    protected ContentProvider(){
        mContext=DumbThingsApp.getContext();
        mDatabase = SQLiteHelper.getInstance(mContext,
                new MainDatabaseErrorHandler());
        mBackupPath = IOHelper.getPrivateExternalStorageDir(
                DefaultFileValues.GetDefaultInternalDatabaseBackupPath(),
                mContext, new MyIOHelperCallbacks());
        mReadableDatabase = mDatabase.getReadableDatabase();
        mWritableDatabase = mDatabase.getWritableDatabase();
    }

    private final class MainDatabaseErrorHandler implements
            DatabaseErrorHandler {
        @Override
        public void onCorruption(SQLiteDatabase dbObj) {
            // TODO Auto-generated method stub
            dbObj.close();
            getContext().deleteDatabase(SQLiteDefault.DATABASE_NAME);
            if (Tools.RestoreDatabase(
                    getContext(), SQLiteDefault.DATABASE_NAME,
                    mBackupPath.getAbsolutePath())) {
                Toast toast = Toast.makeText(getContext(),
                        "Database has been restored successfully.",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getContext(),
                        "Database restore failed. Rebuilding database.",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            mDatabase = SQLiteHelper.getInstance(getContext(), this);
        }
    }
    
    protected Context getContext(){
        return mContext;
    }
    
    public SQLiteDatabase getReadableDatabase(){
        return mReadableDatabase;
    }

    public SQLiteDatabase getWritableDatabase(){
        return mWritableDatabase;
    }

    private final class MyIOHelperCallbacks implements IOHelper.IOHelperCallbacks {
        @Override
        public void onError(int errorType, String message) {
            Toast toast = Toast.makeText(getContext(), message,
                    Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
