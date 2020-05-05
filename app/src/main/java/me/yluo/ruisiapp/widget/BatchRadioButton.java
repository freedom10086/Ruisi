package me.yluo.ruisiapp.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatRadioButton;

import me.yluo.ruisiapp.utils.DimenUtils;


/**
 * @author yang
 */
public class BatchRadioButton extends AppCompatRadioButton {

    private boolean haveBatch = false;
    private int badgeSize = 3;
    private final Paint paint_badge = new Paint();

    public BatchRadioButton(Context context) {
        super(context);
        init(context);
    }

    public BatchRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BatchRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        badgeSize = DimenUtils.dip2px(context, 3);

        paint_badge.setColor(Color.WHITE);
        paint_badge.setStyle(Paint.Style.FILL);
        paint_badge.setAntiAlias(true);
    }

    public void setState(boolean batch) {
        // TODO 有bug暂时不做了
        //if (haveBatch == batch) return;
        //haveBatch = batch;
        //invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (haveBatch) {
            int centx = getWidth() - badgeSize * 2;
            int centy = badgeSize * 2;
            canvas.drawCircle(centx, centy, badgeSize, paint_badge);
        }
    }
}
