package me.yluo.ruisiapp.widget.htmlview;

import android.content.Context;
import android.text.Selection;
import android.text.Spannable;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;


/**
 * 修复 选择报错
 * https://stackoverflow.com/questions/28689871/android-default-textselector-to-copy-text-not-working
 */
public class SelectFixTextView extends TextView {
    public SelectFixTextView(Context context) {
        super(context);
    }

    public SelectFixTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectFixTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SelectFixTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (selStart == -1 || selEnd == -1) {
            // @hack : https://code.google.com/p/android/issues/detail?id=137509
            CharSequence text = getText();
            if (text instanceof Spannable) {
                Selection.setSelection((Spannable) text, 0, 0);
            }
        } else {
            super.onSelectionChanged(selStart, selEnd);
        }
    }
}
