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
import android.os.Environment;

import org.lunci.dumbthing.io.IOHelper;

import java.io.File;

public class Tools {

    public static void BackupDatabase(Context mContext, String databaseName,
                                      String backupFilePath) {
        try {
            final File data = Environment.getDataDirectory();
            String currentDBPath = "//data//" + mContext.getPackageName()
                    + "//databases//" + databaseName;
            final File currentDB = new File(data, currentDBPath);
            final File backupDB = new File(backupFilePath);
            IOHelper.copyFile(currentDB, backupDB);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean RestoreDatabase(Context mContext,
                                          String databaseName, String backupFilePath) {
        try {
            final File data = Environment.getDataDirectory();
            String currentDBPath = "//data//" + mContext.getPackageName()
                    + "//databases//" + databaseName;
            final File currentDB = new File(data, currentDBPath);
            final File backupDB = new File(backupFilePath);
            return IOHelper.copyFile(backupDB, currentDB);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
