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

import org.lunci.dumbthing.io.IOHelper;

import java.io.File;


public class BackupRestore {
	public static boolean Backup(Context context) {
		final File backupDB = new File(org.lunci.dumbthing.io.IOHelper.getPrivateExternalStorageDir(
                org.lunci.dumbthing.io.DefaultFileValues.GetDefaultInternalDatabaseBackupPath(),
				context, new MyIOHelperCallbacks()),
				SQLiteDefault.DatabaseBackupFileName);
		Tools.BackupDatabase(context,
                SQLiteDefault.DATABASE_NAME, backupDB.getAbsolutePath());
		return true;
	}

	private static final class MyIOHelperCallbacks implements IOHelper.IOHelperCallbacks {

		@Override
		public void onError(int errorType, String message) {

		}

	}
}
