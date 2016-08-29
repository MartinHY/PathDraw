package com.martin.pdmaster;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

/**
 * 作者：MartinBZDQSM on 2016/8/28 0028.
 * 博客：http://www.jianshu.com/users/78f0e5f4a403/latest_articles
 * github：https://github.com/MartinBZDQSM
 */
public class PathDrawingView extends View {

    private PathLayer svgUtil;

    private Bitmap drawLayer;//需要最后显示的图层，绘制在底部

    private Bitmap paintLayer;//画笔的图层

    private Bitmap coverLayer;

    private static final int defaultCoverColor = Color.GRAY;//默认遮挡层的颜色

    private int coverColor;

    private PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

    private Paint pathPaint;


    public PathDrawingView(Context context) {
        super(context);
    }

    public PathDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}
