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

public class QueryHelper {
	public static String getIdString(String[] idList) {
		StringBuilder ids = new StringBuilder();
		ids.append("(");
		for (int i = 0; i < idList.length; i++) {
			if (idList[i] == null || idList[i].isEmpty()) {
				continue;
			}
			ids.append(idList[i]);
			ids.append(",");
		}
		ids = ids.deleteCharAt(ids.length() - 1);
		ids.append(")");
		return ids.toString();
	}

	public static String getIdString(long[] idList) {
		StringBuilder ids = new StringBuilder();
		ids.append("(");
		for (int i = 0; i < idList.length; i++) {
			if (idList[i] == -1)
				continue;
			ids.append(idList[i]);
			ids.append(",");
		}
		ids = ids.deleteCharAt(ids.length() - 1);
		ids.append(")");
		return ids.toString();
	}
}
