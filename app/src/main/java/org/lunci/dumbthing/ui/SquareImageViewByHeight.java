/*
 * Restricted Copyright (c) Siemens AG, 2015. All Rights Reserved.
 */

package org.lunci.dumbthing.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImageViewByHeight extends ImageView {
	public boolean OpaqueIfDisabled = true;

	public SquareImageViewByHeight(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SquareImageViewByHeight(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SquareImageViewByHeight(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (OpaqueIfDisabled) {
			if (enabled) {
				this.animate().alpha(1f);
			} else {
				this.animate().alpha(0.6f);
			}
		} else {
			this.animate().alpha(1f);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//		if (widthMode == MeasureSpec.EXACTLY
//				&& heightMode != MeasureSpec.EXACTLY) {
//			int width = MeasureSpec.getSize(widthMeasureSpec);
//			int height = width;
//			if (heightMode == MeasureSpec.AT_MOST) {
//				height = Math.min(height,
//						MeasureSpec.getSize(heightMeasureSpec));
//			}
//			setMeasuredDimension(width, height);
//		} else {
//			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		}
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY
                && heightMode == MeasureSpec.EXACTLY) {
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int width = height;
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width,
                        MeasureSpec.getSize(widthMeasureSpec));
            }
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
	}
}
