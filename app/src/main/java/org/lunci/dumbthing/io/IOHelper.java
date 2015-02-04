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

package org.lunci.dumbthing.io;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class IOHelper {
	private static final String TAG = "SnapLog.FileIO.IOHelper";
	public static int Error_Storage_Unavailable = 0;
	public static int Error_Storage_ReadOnly = 1;
	public static int Error_Unknown = 2;
	public static int Error_File_Not_Found = 3;
	public static int Error_IO = 4;

	public static interface IOHelperCallbacks {
		void onError(int errorType, String message);
	}

	public static boolean copyFile(File orgFile, File destFile) {
		boolean succ = true;
		if (!orgFile.exists()) {
			Log.w(TAG,
					"copyFile: orgFile not found:" + orgFile.getAbsolutePath());
			return false;
		} else if (orgFile.canRead()) {
			try {
				destFile.createNewFile();
				final FileInputStream inStream = new FileInputStream(orgFile);
				final FileChannel src = inStream.getChannel();
				final FileOutputStream outStream = new FileOutputStream(
						destFile);
				final FileChannel dst = outStream.getChannel();
				try {
					dst.transferFrom(src, 0, src.size());
				} catch (Exception ex) {
					ex.printStackTrace();
					succ = false;
				} finally {
					src.close();
					dst.close();
					inStream.close();
					outStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				succ = false;
			}
		} else {
			Log.e(TAG,
					"copyFile: unable to read orgFile:"
							+ orgFile.getAbsolutePath());
			succ = false;
		}
		return succ;
	}

	public static boolean deleteFile(File file) {
		if (file.isFile() && file.exists() && file.canWrite()) {
			Log.d(TAG, "Deleting file:" + file.getName());
			return file.delete();
		} else {
			return false;
		}
	}

	public static File getInternalCacheDir(Context context) {
		// Get the directory for the app's private pictures directory.
		File file = context.getCacheDir();
		return file;
	}

	/* Checks if external storage is available to at least read */
	public static boolean isExternalStorageReadable() {
		try {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)
					|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				return true;
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
		return false;
	}

	/* Checks if external storage is available for read and write */
	public static boolean isExternalStorageWritable() {
		try {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				return true;
			}
			return false;
		} catch (Exception err) {
			err.printStackTrace();
		}
		return false;
	}

	public static String readAssetText(Context context, String filename) {
		try {
			InputStream stream = context.getAssets().open(filename);
			final int size = stream.available();
			final byte[] buffer = new byte[size];
			stream.read(buffer);
			stream.close();
			return new String(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

	}

	public static byte[] readFile(File file_in) {
		if (file_in == null || !file_in.exists()) {
			if (file_in != null) {
				Log.e(TAG, "Unable to read file:" + file_in.getAbsolutePath());
			} else {
				Log.e(TAG, "Unable to read file: file_in is null.");
			}
			return null;
		}

		FileInputStream finput = null;
		byte result[] = null;
		try {
			// Log.d(TAG, "Reading file");
			result = new byte[(int) file_in.length()];
			finput = new FileInputStream(file_in);
			finput.read(result);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (finput != null) {
				try {
					finput.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static boolean writeFile(File file_in, byte[] content_in) {
		if (file_in == null) {
			Log.e(TAG, "file_in is null.");
			return false;
		}
		boolean succ = false;
		FileOutputStream fos = null;
		try {
			file_in.createNewFile();
			fos = new FileOutputStream(file_in);
			fos.write(content_in);
			succ = true;
			Log.d(TAG, "File saved:" + file_in.getAbsolutePath());
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File unable to create.");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, "File writing error.");
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return succ;
	}

	public static File getPrivateExternalStorageDir(String folderName,
			Context context, IOHelperCallbacks callbacks) {
		String state = null;
		try {
			state = Environment.getExternalStorageState();
		} catch (RuntimeException e) {
			Log.e(TAG, "Is the SD card visible?", e);
			callbacks
					.onError(Error_Storage_Unavailable,
							"Required external storage (such as an SD card) is unavailable.");
		}
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			// We can read and write the media
			// if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) > 7) {
			// For Android 2.2 and above
			try {
				File file = context
						.getExternalFilesDir(Environment.MEDIA_MOUNTED);
				file = new File(file.getAbsolutePath() + File.separator
						+ folderName);
				if (!file.exists())
					file.mkdirs();
				return file;
			} catch (NullPointerException e) {
				// We get an error here if the SD card is visible, but full
				Log.e(TAG, "External storage is unavailable");
				callbacks
						.onError(Error_Storage_Unavailable,
								"Required external storage (such as an SD card) is unavailable.");
			}

		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			Log.e(TAG, "External storage is read-only");
			callbacks
					.onError(Error_Storage_ReadOnly,
							"Required external storage (such as an SD card) is read-only.");
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			Log.e(TAG, "External storage is unavailable");
			callbacks
					.onError(Error_Unknown,
							"Required external storage (such as an SD card) is unavailable or corrupted.");
		}
		return null;
	}

	public static File getPublicExternalStorageDir(String folderName,
			IOHelperCallbacks callbacks) {
		// Get the directory for the app's private pictures directory.
		if (!isExternalStorageReadable() || !isExternalStorageWritable()) {
			callbacks
					.onError(Error_Storage_Unavailable,
							"Unable to read/write external storage (such as an SD card)");
			return null;
		}
		try {
			File file = Environment
					.getExternalStoragePublicDirectory(folderName);
			// Log.d(TAG, "publicExternalStorageDir:" + file);
			if (!file.mkdirs()) {
				// Log.d(TAG, "Directory is not created");
			}
			return file;
		} catch (Exception err) {
			err.printStackTrace();
			callbacks
					.onError(Error_Unknown,
							"Unable to read/write external storage (such as an SD card)");
		}
		return null;
	}
}
