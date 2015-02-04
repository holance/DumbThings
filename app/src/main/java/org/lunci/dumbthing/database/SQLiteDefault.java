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

import org.lunci.dumbthing.dataModel.DumbModel;

public class SQLiteDefault {
	public static final String DATABASE_NAME = "DumbThings.db";
	public static final int DATABASE_VERSION = 1;
	public static final String[] CreateCommand = {
			"CREATE TABLE DumbThings ( "
					+ "	    id         INTEGER PRIMARY KEY ASC ON CONFLICT FAIL NOT NULL UNIQUE,"
					+ "Content         TEXT,"
					+ "CreatedAt       DATETIME NOT NULL ON CONFLICT FAIL"
					+ "                         DEFAULT ( datetime( 'now' )  ),"
					+ "ModifiedAt      DATETIME NOT NULL ON CONFLICT FAIL"
					+ "                         DEFAULT ( datetime( 'now' )  ),"
					+ "MediaId        Long);",
            "CREATE TRIGGER UpdateModified" + "  AFTER UPDATE ON DumbThings "
                    + "	BEGIN" + "    UPDATE DumbThings"
                    + "       SET ModifiedAt = CURRENT_TIMESTAMP"
                    + "     WHERE id = OLD.id;" + "END;",
			"CREATE INDEX idx_CreatedAt ON DumbThings ( CreatedAt );"
    };

	public static final String DatabaseExportFileName = "DumbThings.db.backup";
	public static final String DatabaseBackupFileName = "DumbThings.db.backup";

    public static final String GetAllDumbThingsQuery = "select * FROM "
            + DumbModel.Table + " order by datetime(" + DumbModel.ModifiedAt_Field + ") DESC";
    
    public static final String GetDumbThingsCount="";
}
