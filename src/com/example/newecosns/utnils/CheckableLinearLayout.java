package com.example.newecosns.utnils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {

	private static final String TAG = "CheckableLinearLayout";
    private static final int[] CHECKED_STATE_SET = {
        android.R.attr.state_checked
    };
	boolean mChecked;

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean isChecked() {

		return mChecked;
	}

	@Override
	public void setChecked(boolean checked) {

            if (mChecked != checked) {
                 mChecked = checked;
                 refreshDrawableState();
             }
	}

	@Override
	public void toggle() {

		mChecked = !mChecked;

	}

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}