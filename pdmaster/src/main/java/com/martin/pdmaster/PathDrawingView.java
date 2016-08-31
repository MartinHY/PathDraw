package com.martin.pdmaster;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：MartinBZDQSM on 2016/8/28 0028.
 * 博客：http://www.jianshu.com/users/78f0e5f4a403/latest_articles
 * github：https://github.com/MartinBZDQSM
 */
public class PathDrawingView extends View implements PathLayer.AnimationStepListener {

    private static final String TAG = "PathDrawingView";

    //组件的宽高
    private int width;
    private int height;

    private Bitmap drawLayer;//需要最后显示的图层，绘制在底部
    private float nibPrecent = 0.2069f;//笔尖在paintLayer中高的比例

    private Bitmap paintLayer;//画笔的图层

    private PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

    private Paint pathPaint;

    private int path1Id;

    private PathLayer pathLayer = new PathLayer();

    private List<PathLayer.SvgPath> mPaths = new ArrayList<>();

    private static final int defaultPathColor = Color.BLACK;//默认遮挡层的颜色

    private int pathColor;

    private Thread mLoader;

    private final Object mSvgLock = new Object();


    /**
     * 去除了并行动画，采用顺序动画
     */
    private AnimatorSetBuilder animatorSetBuilder;
    /**
     * 绘制进度
     */
    private float progress = 0f;

    private int totalLenth;

    private Bitmap mTempBitmap;
    private Canvas mTempCanvas;

    public PathDrawingView(Context context) {
        super(context, null);
    }

    public PathDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getSrcFromAttrs(context, attrs);
    }

    private void getSrcFromAttrs(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PathDrawingView);
        try {
            if (a != null) {
                path1Id = a.getResourceId(R.styleable.PathDrawingView_path1, 0);
                pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                pathPaint.setColor(defaultPathColor);
                pathPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                pathPaint.setStrokeWidth(5);
            }
        } finally {
            if (a != null) {
                a.recycle();
            }
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mTempBitmap == null || (mTempBitmap.getWidth() != canvas.getWidth() || mTempBitmap.getHeight() != canvas.getHeight())) {
            mTempBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            mTempCanvas = new Canvas(mTempBitmap);
        }

        mTempBitmap.eraseColor(0);
        synchronized (mSvgLock) {
            mTempCanvas.save();
            mTempCanvas.translate(getPaddingLeft(), getPaddingTop());
            final int count = mPaths.size();
            for (int i = 0; i < count; i++) {
                final PathLayer.SvgPath svgPath = mPaths.get(i);
                final Path path = svgPath.path;
                mTempCanvas.drawPath(path, pathPaint);
            }
            mTempCanvas.restore();
            canvas.drawBitmap(mTempBitmap, 0, 0, null);
            if (paintLayer != null) {
                canvas.drawBitmap(paintLayer, 0, 0, null);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mLoader != null) {
            try {
                mLoader.join();
            } catch (InterruptedException e) {
            }
        }
        if (path1Id != 0) {
            mLoader = new Thread(new Runnable() {
                @Override
                public void run() {
                    pathLayer.load(getContext(), path1Id);
                    Resources resources = getContext().getResources();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = false;//获取真实宽高的Paintlayer
                    paintLayer = BitmapFactory.decodeResource(resources, R.drawable.paint1, options);
                    synchronized (mSvgLock) {//同步锁
                        width = w - getPaddingLeft() - getPaddingRight();
                        height = h - getPaddingTop() - getPaddingBottom();

                        //需要测量path
                        mPaths = pathLayer.getPathsForViewport(width, height, 5);

//                        PathDrawingView.this.postInvalidate();
                    }
                }
            }, "SVG Loader");
            mLoader.start();
        }
    }

    /**
     * This refreshes the paths before draw and resize.
     */
    private void updatePathsPhaseLocked() {
        final int count = mPaths.size();
        for (int i = 0; i < count; i++) {
            PathLayer.SvgPath svgPath = mPaths.get(i);
            totalLenth += svgPath.getLength();
            svgPath.path.reset();
            svgPath.measure.getSegment(0.0f, svgPath.length * 0, svgPath.path, true);
            // Required only for Android 4.4 and earlier
            svgPath.path.rLineTo(0.0f, 0.0f);
        }
    }


    /**
     * AnimatorSet for the paths of the view to be animated one after the other.
     *
     * @return The AnimatorBuilder to build the animation.
     */
    public AnimatorSetBuilder getSequentialPathAnimator() {
        if (animatorSetBuilder == null) {
            animatorSetBuilder = new AnimatorSetBuilder(this);
        }
        return animatorSetBuilder;
    }


    @Override
    public void onAnimationStep() {
        invalidate();
    }

    /**
     * Object for building the sequential animation of the paths of this view.
     */
    public static class AnimatorSetBuilder {
        /**
         * Duration of the animation.
         */
        private int duration = 1000;

        private final List<Long> durations = new ArrayList<>();

        /**
         * Interpolator for the time of the animation.
         */
        private Interpolator interpolator;
        /**
         * The delay before the animation.
         */
        private int delay = 0;
        /**
         * List of ObjectAnimator that constructs the animations of each path.
         */
        private final List<Animator> animators = new ArrayList<>();
        /**
         * Listener called before the animation.
         */
        private ListenerStart listenerStart;
        /**
         * Listener after the animation.
         */
        private ListenerEnd animationEnd;
        /**
         * Animation listener.
         */
        private AnimatorSetBuilder.PathViewAnimatorListener pathViewAnimatorListener;
        /**
         * The list of paths to be animated.
         */
        private List<PathLayer.SvgPath> paths;
        /**
         * The animator that can animate paths sequentially
         */
        private AnimatorSet animatorSet = new AnimatorSet();

        /**
         * Default constructor.
         *
         * @param pathView The view that must be animated.
         */
        public AnimatorSetBuilder(final PathDrawingView pathView) {
            paths = pathView.mPaths;
            if (pathViewAnimatorListener == null) {
                pathViewAnimatorListener = new PathViewAnimatorListener();
            }
            for (PathLayer.SvgPath path : paths) {
                path.setAnimationStepListener(pathView);
                ObjectAnimator animation = ObjectAnimator.ofFloat(path, "length", 0.0f, path.getLength());
                long animationDuration = (long) ((path.getLength() * 1.0f / pathView.totalLenth) * duration);
                durations.add(animationDuration);
                Log.i("PathDrawingView", "animationDuration  :" + animationDuration);
                animation.setDuration(animationDuration);
                animation.addListener(pathViewAnimatorListener);
                animators.add(animation);
            }
            animatorSet.addListener(pathViewAnimatorListener);
        }

        /**
         * Sets the duration of the animation. Since the AnimatorSet sets the duration for each
         * Animator, we have to divide it by the number of paths.
         *
         * @param duration - The duration of the animation.
         * @return AnimatorSetBuilder.
         */
        public AnimatorSetBuilder duration(final int duration) {
            this.duration = duration;
            return this;
        }

        /**
         * Set the Interpolator.
         *
         * @param interpolator - Interpolator.
         * @return AnimatorSetBuilder.
         */
        public AnimatorSetBuilder interpolator(final Interpolator interpolator) {
            this.interpolator = interpolator;
            return this;
        }

        /**
         * The delay before the animation.
         *
         * @param delay - int the delay
         * @return AnimatorSetBuilder.
         */
        public AnimatorSetBuilder delay(final int delay) {
            this.delay = delay;
            return this;
        }

        /**
         * Set a listener before the start of the animation.
         *
         * @param listenerStart an interface called before the animation
         * @return AnimatorSetBuilder.
         */
        public AnimatorSetBuilder listenerStart(final ListenerStart listenerStart) {
            this.listenerStart = listenerStart;
            return this;
        }

        /**
         * Set a listener after of the animation.
         *
         * @param animationEnd an interface called after the animation
         * @return AnimatorSetBuilder.
         */
        public AnimatorSetBuilder listenerEnd(final ListenerEnd animationEnd) {
            this.animationEnd = animationEnd;
            return this;
        }

        /**
         * Starts the animation.
         */
        public void start() {
            resetAllPaths();
            animatorSet.cancel();
            animatorSet.playSequentially(animators);
            animatorSet.start();
        }

        /**
         * Sets the length of all the paths to 0.
         */
        private void resetAllPaths() {
            for (PathLayer.SvgPath path : paths) {
                path.setLength(0);
            }
        }

        /**
         * Called when the animation start.
         */
        public interface ListenerStart {
            /**
             * Called when the path animation start.
             */
            void onAnimationStart();
        }

        /**
         * Called when the animation end.
         */
        public interface ListenerEnd {
            /**
             * Called when the path animation end.
             */
            void onAnimationEnd();
        }

        /**
         * Animation listener to be able to provide callbacks for the caller.
         */
        private class PathViewAnimatorListener implements Animator.AnimatorListener {

            @Override
            public void onAnimationStart(Animator animation) {
                if (listenerStart != null)
                    listenerStart.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animationEnd != null)
                    animationEnd.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }
    }
}
