package com.martin.pdmaster;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is base on PathView.class in PathView ,
 * If you want know more , The link of PathView: https://github.com/geftimov/android-pathview.
 * <p/>
 * Licence of pathview :
 * <p/>
 * Copyright 2016 Georgi Eftimov
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * <p/>
 * 作者：MartinBZDQSM on 2016/8/28 0028.
 * 博客：http://www.jianshu.com/users/78f0e5f4a403/latest_articles
 * github：https://github.com/MartinBZDQSM
 */
public class PathDrawingView extends View implements PathUtils.AnimationStepListener {

    private static final String TAG = "PathDrawingView";

    //组件的宽高
    private int width;
    private int height;

    //    private Bitmap drawLayer;//需要最后显示的图层，绘制在底部
//    private int drawLayerId;
    private Paint drawerPaint;
    private float nibPrecent = 0.2069f;//笔尖在paintLayer中高的比例
    private PointF nibPointf;

    private Bitmap paintLayer;//画笔的图层

    private Paint pathPaint;
    private int pathId;
    private PathUtils pathLayer = new PathUtils();
    private List<PathUtils.SvgPath> mPaths = new ArrayList<>();

    private Thread mLoader;
    private final Object mSvgLock = new Object();
    public static boolean isDrawing = false;

    private boolean isDrawPaint;

    private int defaultColor = Color.GRAY;
    private int paintColor;

    private boolean isFill = true;
    private RectF drawRect = new RectF();

    private boolean isFillAfter = false;
    public static boolean isDrawingFinished = false;

    /**
     * 去除了并行动画，采用顺序动画
     */
    private AnimatorSetBuilder animatorSetBuilder;

    public PathDrawingView(Context context) {
        this(context, null);
    }

    public PathDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getSrcFromAttrs(context, attrs);
    }

    private void getSrcFromAttrs(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PathDrawingView);
        try {
            if (a != null) {
                pathId = a.getResourceId(R.styleable.PathDrawingView_path, 0);
//                drawLayerId = a.getResourceId(R.styleable.PathDrawingView_drawer, 0);
                isDrawPaint = a.getBoolean(R.styleable.PathDrawingView_draw_paint, true);
                isFillAfter = a.getBoolean(R.styleable.PathDrawingView_fill_after, false);
                isFill = a.getBoolean(R.styleable.PathDrawingView_filling, false);
                paintColor = a.getColor(R.styleable.PathDrawingView_paint_color, defaultColor);

                pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                pathPaint.setStyle(Paint.Style.STROKE);//与这里没有毛关系了
                pathPaint.setColor(paintColor);
                if (isFill) {
                    drawerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    drawerPaint.setStyle(Paint.Style.FILL);
                    drawerPaint.setColor(paintColor);
                }
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
        int sc = canvas.save(Canvas.ALL_SAVE_FLAG);
        synchronized (mSvgLock) {
            int count = mPaths.size();
            for (int i = 0; i < count; i++) {
                int pc = canvas.save(Canvas.ALL_SAVE_FLAG);
                PathUtils.SvgPath svgPath = mPaths.get(i);
                if (isFill) {
                    //需要备用一个完整的path路径，来修复pathPaint的Fill造成绘制过度
                    Path path = pathLayer.mDrawer.get(i);
                    canvas.clipPath(path);
                    //获取svgPath当前矩阵
                    svgPath.path.computeBounds(drawRect, true);
                    canvas.drawRect(drawRect, drawerPaint);
                }
                canvas.drawPath(svgPath.path, pathPaint);
                canvas.restoreToCount(pc);
            }
        }
        canvas.restoreToCount(sc);
        if (isDrawingFinished && isFillAfter)
            pathLayer.drawSvgAfter(canvas, width, height);

        if (isDrawPaint) {
            for (PathUtils.SvgPath svgPath : mPaths) {
                if (isDrawing && svgPath.isMeasure) {
                    canvas.drawBitmap(paintLayer, svgPath.point[0] - nibPointf.x, svgPath.point[1] - nibPointf.y, null);
                }
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
        if (pathId != 0) {
            mLoader = new Thread(new Runnable() {
                @Override
                public void run() {
                    pathLayer.load(getContext(), pathId);
                    Resources resources = getContext().getResources();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inScaled = false;//获取真实宽高的Paintlayer
                    paintLayer = BitmapFactory.decodeResource(resources, R.drawable.mypaint, options);


//                    drawLayer = BitmapFactory.decodeResource(resources, drawLayerId, options);
                    synchronized (mSvgLock) {//同步锁
                        width = w - getPaddingLeft() - getPaddingRight();
                        height = h - getPaddingTop() - getPaddingBottom();
                        //需要测量path
                        mPaths = pathLayer.getPathsForViewport(width, height);
                        //画笔 的笔尖位置
                        float nibX = 0;
                        float nibY = paintLayer.getHeight() * nibPrecent;
                        nibPointf = new PointF(nibX, nibY);
                    }
                }
            }, "SVG Loader");
            mLoader.start();
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
        private int duration;

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
        private PathViewAnimatorListener pathViewAnimatorListener;
        /**
         * The list of paths to be animated.
         */
        private List<PathUtils.SvgPath> paths;

        private int index = 0;
        private float totalLenth;

        /**
         * Default constructor.
         *
         * @param pathView The view that must be animated.
         */
        public AnimatorSetBuilder(final PathDrawingView pathView) {
            paths = pathView.mPaths;
            for (PathUtils.SvgPath path : paths) {
                path.setAnimationStepListener(pathView);
                ObjectAnimator animation = ObjectAnimator.ofFloat(path, "length", 0.0f, path.getLength());
                totalLenth = totalLenth + path.getLength();
                animators.add(animation);
            }
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
            if (pathViewAnimatorListener == null) {
                pathViewAnimatorListener = new PathViewAnimatorListener();
                for (int i = 0; i < paths.size(); i++) {
                    long animationDuration = (long) (paths.get(i).getLength() * duration / totalLenth);
                    Animator animator = animators.get(i);
                    animator.setStartDelay(delay);
                    animator.setDuration(animationDuration);
                    animator.addListener(pathViewAnimatorListener);
                }
            }

            resetAllPaths();
            for (Animator animator : animators) {
                animator.cancel();
            }
            index = 0;
            startAnimatorByIndex();
        }

        public void startAnimatorByIndex() {
            if (index >= paths.size()) {
                return;
            }
            Animator animator = animators.get(index);
            animator.start();
        }

        /**
         * Sets the length of all the paths to 0.
         */
        private void resetAllPaths() {
            for (PathUtils.SvgPath path : paths) {
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
                if (index < paths.size() - 1) {
                    paths.get(index).isMeasure = true;
                    if (animators.get(index).getDuration() < 50) {//过滤动画事件小于100ms的画笔轨迹
                        PathDrawingView.isDrawing = false;
                    } else {
                        PathDrawingView.isDrawing = true;
                    }
                    PathDrawingView.isDrawingFinished = false;
                    if (index == 0 && listenerStart != null)
                        listenerStart.onAnimationStart();
                }

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (index >= paths.size() - 1) {
                    PathDrawingView.isDrawing = false;
                    PathDrawingView.isDrawingFinished = true;
                    if (animationEnd != null) {
                        animationEnd.onAnimationEnd();
                    }
                } else {
                    if (index < paths.size() - 1) {
                        paths.get(index).isMeasure = false;
                        index++;
                        startAnimatorByIndex();
                    }
                }
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
