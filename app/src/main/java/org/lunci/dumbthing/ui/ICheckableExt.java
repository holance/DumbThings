/*
 *  Copyright (C) 2015 Lunci Hua
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 */

package org.lunci.dumbthing.ui;

import android.view.View;
import android.widget.Checkable;

public interface ICheckableExt extends Checkable {
	public static interface CheckableLayoutCallbacks {
		void onCheckChanged(View view, boolean checked);
	}

	public void setCallbacks(CheckableLayoutCallbacks callbacks);

	public void setCheckable(boolean isCheckable);

    public boolean isCheckable();
}
