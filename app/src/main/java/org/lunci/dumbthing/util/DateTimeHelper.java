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

package org.lunci.dumbthing.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeHelper {
	private static final String TAG = "SnapLog.DateTimeHelper";
    public static final String DISPLAYFORMAT="MM/dd/yy hh:mm a";
    public static final String DATABASEFORMAT="yyyy-MM-dd HH:mm:ss";

	public static String getDateTime(String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format,
				Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static String getDateTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
                DATABASEFORMAT, Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static Date getDateTimeFromString(String dateTimeString, String format) throws ParseException{
		SimpleDateFormat dateFormat = new SimpleDateFormat(
                format, Locale.getDefault());
		try {
			return dateFormat.parse(dateTimeString);
		} catch (ParseException e) {
			Log.e(TAG, "Converting datetime from string error");
			e.printStackTrace();
			return null;
		}
	}
    
    public static String changeFormat(String gmtString, String orgFormat, String format) throws ParseException{
        final Date date=getDateTimeFromString(gmtString, orgFormat);
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String getLocalDateTimeFromString(String gmtString, String orgFormat, String format)
    throws ParseException{
        final Date date=getDateTimeFromString(gmtString, orgFormat);
        final Date local=new Date(date.getTime()+TimeZone.getDefault().getOffset(date.getTime()));
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(local);
    }

    public static String getStringFromDate(Date date, String format)
            throws ParseException{
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

	public static String getMonthStringByIndex(int index) {
		switch (index) {
		case 1:
			return "Jan";
		case 2:
			return "Feb";
		case 3:
			return "Mar";
		case 4:
			return "Apr";
		case 5:
			return "May";
		case 6:
			return "Jun";
		case 7:
			return "Jul";
		case 8:
			return "Aug";
		case 9:
			return "Sep";
		case 10:
			return "Oct";
		case 11:
			return "Nov";
		case 12:
			return "Dec";
		default:
			return "";
		}
	}
}
