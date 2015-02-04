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

package org.lunci.dumbthing.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {
	private static final String TAG = "SnapLog.SQLiteHelper";

	private final Context mContext;
	private static SQLiteOpenHelper helper;

	public static SQLiteOpenHelper getInstance(Context context,
			DatabaseErrorHandler errorHandler) {
		if (helper == null)
			helper = new SQLiteHelper(context, errorHandler);
		return helper;
	}

	private SQLiteHelper(Context context, DatabaseErrorHandler errorHandler) {
		super(context, SQLiteDefault.DATABASE_NAME, null,
				SQLiteDefault.DATABASE_VERSION, errorHandler);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate");
		for (int i = 0; i < SQLiteDefault.CreateCommand.length; ++i)
			db.execSQL(SQLiteDefault.CreateCommand[i]);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade");
		mContext.deleteDatabase(SQLiteDefault.DATABASE_NAME);
		onCreate(db);
	}
}
