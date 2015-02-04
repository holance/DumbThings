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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout implements
ICheckableExt {
    private static final int[] CheckedStateSet = { android.R.attr.state_checked };
	private boolean mChecked;
	private boolean mCheckable = true;
	private CheckableLayoutCallbacks mCallbacks = null;

	public CheckableRelativeLayout(Context context) {
		super(context);
	}

	public CheckableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CheckableRelativeLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub

	}

	@Override
	public void setCallbacks(CheckableLayoutCallbacks callbacks) {
		mCallbacks = callbacks;
	}

	@Override
	public void setChecked(boolean checked) {
		if (mCheckable) {
			if (mChecked == checked)
				return;
			mChecked = checked;
            refreshDrawableState();
			if (mCallbacks != null) {
				try {
					mCallbacks.onCheckChanged(this, checked);
				} catch (Exception err) {
					err.printStackTrace();
				}
			}
		} else {
			mChecked = false;
		}
	}

	@Override
	public void setCheckable(boolean isCheckable) {
		mCheckable = isCheckable;
		if (!isCheckable) {
			mChecked = false;
		}
	}

    @Override
    public boolean isCheckable(){
        return mCheckable;
    }

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CheckedStateSet);
        }
        return drawableState;
    }
}
