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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

public class SQLiteCursorLoader extends AbstractCursorLoader {
	protected SQLiteOpenHelper db = null;
	protected String rawQuery = null;
	protected String[] args = null;
	private static final String TAG = "SnapLog.SQLiteCursorLoader";

	/**
	 * Creates a fully-specified SQLiteCursorLoader. See
	 * {@link android.database.sqlite.SQLiteDatabase#rawQuery(android.database.sqlite.SQLiteDatabase, String, String[])
	 * SQLiteDatabase.rawQuery()} for documentation on the meaning of the
	 * parameters. These will be passed as-is to that call.
	 */
	public SQLiteCursorLoader(Context context, SQLiteOpenHelper db,
			String rawQuery, String[] args) {
		super(context);
		this.db = db;
		this.rawQuery = rawQuery;
		this.args = args;
	}

	/**
	 * Runs on a worker thread and performs the actual database query to
	 * retrieve the Cursor.
	 */
	@Override
	protected Cursor buildCursor() {
		if (args != null && args.length > 0)
			Log.d(TAG, "executing: " + rawQuery + args[0]);
		try {
			return (db.getReadableDatabase().rawQuery(rawQuery, args));
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Query failed..");
			return null;
		}
	}

	/**
	 * Writes a semi-user-readable roster of contents to supplied output.
	 */
	@Override
	public void dump(String prefix, FileDescriptor fd, PrintWriter writer,
			String[] args) {
		super.dump(prefix, fd, writer, args);
		writer.print(prefix);
		writer.print("rawQuery=");
		writer.println(rawQuery);
		writer.print(prefix);
		writer.print("args=");
		writer.println(Arrays.toString(args));
	}

	public void insert(String table, String nullColumnHack, ContentValues values) {
		buildInsertTask(this).execute(db, table, nullColumnHack, values);
	}

	public void update(String table, ContentValues values, String whereClause,
			String[] whereArgs) {
		buildUpdateTask(this)
				.execute(db, table, values, whereClause, whereArgs);
	}

	public void replace(String table, String nullColumnHack,
			ContentValues values) {
		buildReplaceTask(this).execute(db, table, nullColumnHack, values);
	}

	public void delete(String table, String whereClause, String[] whereArgs) {
		buildDeleteTask(this).execute(db, table, whereClause, whereArgs);
	}

	public void execSQL(String sql, Object[] bindArgs) {
		buildExecSQLTask(this).execute(db, sql, bindArgs);
	}

	protected ContentChangingTask buildInsertTask(SQLiteCursorLoader loader) {
		return (new InsertTask(loader));
	}

	protected ContentChangingTask buildUpdateTask(SQLiteCursorLoader loader) {
		return (new UpdateTask(loader));
	}

	protected ContentChangingTask buildReplaceTask(SQLiteCursorLoader loader) {
		return (new ReplaceTask(loader));
	}

	protected ContentChangingTask buildDeleteTask(SQLiteCursorLoader loader) {
		return (new DeleteTask(loader));
	}

	protected ContentChangingTask buildExecSQLTask(SQLiteCursorLoader loader) {
		return (new ExecSQLTask(loader));
	}

	protected static class InsertTask extends ContentChangingTask {
		InsertTask(SQLiteCursorLoader loader) {
			super(loader);
		}

		@Override
		protected Void doInBackground(Object... params) {
			SQLiteOpenHelper db = (SQLiteOpenHelper) params[0];
			String table = (String) params[1];
			String nullColumnHack = (String) params[2];
			ContentValues values = (ContentValues) params[3];
			SQLiteDatabase w = db.getWritableDatabase();
			try {
				w.beginTransaction();
				w.insert(table, nullColumnHack, values);
				w.setTransactionSuccessful();
			} catch (Exception err) {
				err.printStackTrace();
			} finally {
				w.endTransaction();
			}
			return (null);
		}
	}

	protected static class UpdateTask extends ContentChangingTask {
		UpdateTask(SQLiteCursorLoader loader) {
			super(loader);
		}

		@Override
		protected Void doInBackground(Object... params) {
			SQLiteOpenHelper db = (SQLiteOpenHelper) params[0];
			String table = (String) params[1];
			ContentValues values = (ContentValues) params[2];
			String where = (String) params[3];
			String[] whereParams = (String[]) params[4];
			SQLiteDatabase w = db.getWritableDatabase();
			try {
				w.beginTransaction();
				w.update(table, values, where, whereParams);
				w.setTransactionSuccessful();
			} catch (Exception err) {
				err.printStackTrace();
			} finally {
				w.endTransaction();
			}
			return (null);
		}
	}

	protected static class ReplaceTask extends ContentChangingTask {
		ReplaceTask(SQLiteCursorLoader loader) {
			super(loader);
		}

		@Override
		protected Void doInBackground(Object... params) {
			SQLiteOpenHelper db = (SQLiteOpenHelper) params[0];
			String table = (String) params[1];
			String nullColumnHack = (String) params[2];
			ContentValues values = (ContentValues) params[3];
			SQLiteDatabase w = db.getWritableDatabase();
			try {
				w.beginTransaction();
				w.replace(table, nullColumnHack, values);
				w.setTransactionSuccessful();
			} catch (Exception err) {
				err.printStackTrace();
			} finally {
				w.endTransaction();
			}
			return (null);
		}
	}

	protected static class DeleteTask extends ContentChangingTask {
		DeleteTask(SQLiteCursorLoader loader) {
			super(loader);
		}

		@Override
		protected Void doInBackground(Object... params) {
			SQLiteOpenHelper db = (SQLiteOpenHelper) params[0];
			String table = (String) params[1];
			String where = (String) params[2];
			String[] whereParams = (String[]) params[3];
			SQLiteDatabase w = db.getWritableDatabase();
			try {
				w.beginTransaction();
				w.delete(table, where, whereParams);
				w.setTransactionSuccessful();
			} catch (Exception err) {
				err.printStackTrace();
			} finally {
				w.endTransaction();
			}
			return (null);
		}
	}

	protected static class ExecSQLTask extends ContentChangingTask {
		ExecSQLTask(SQLiteCursorLoader loader) {
			super(loader);
		}

		@Override
		protected Void doInBackground(Object... params) {
			SQLiteOpenHelper db = (SQLiteOpenHelper) params[0];
			String sql = (String) params[1];
			Object[] bindParams = (Object[]) params[2];
			SQLiteDatabase w = db.getWritableDatabase();
			try {
				w.beginTransaction();
				w.execSQL(sql, bindParams);
				w.setTransactionSuccessful();
			} catch (Exception err) {
				err.printStackTrace();
			} finally {
				w.endTransaction();
			}
			return (null);
		}
	}
}