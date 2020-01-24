/*
 * Copyright (C) 2015 Basil Miller
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.requirements.navigationTabBar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;

import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.viewpager.widget.ViewPager;

import com.example.requirements.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;




/**
 * Created by GIGAMOLE on 24.03.2016.
 */
@SuppressWarnings({"unused", "DefaultFileTemplate"})
public class NavigationTabBar extends View implements ViewPager.OnPageChangeListener {

    // NTB constants
    protected final static int FLAGS =
            Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG;


    protected final static String PREVIEW_TITLE = "Title";

    protected final static int INVALID_INDEX = -1;
    public final static int AUTO_SIZE = -2;
    public final static int AUTO_COLOR = -3;
    public final static int AUTO_SCALE = -4;


    protected final static int DEFAULT_ANIMATION_DURATION = 300;

    protected final static int DEFAULT_INACTIVE_COLOR = Color.parseColor("#9f90af");
    protected final static int DEFAULT_ACTIVE_COLOR = Color.WHITE;
    protected final static int DEFAULT_BG_COLOR = Color.parseColor("#605271");

    protected final static float MIN_FRACTION = 0.0F;
    protected final static float MAX_FRACTION = 1.0F;

    protected final static int MIN_ALPHA = 0;
    protected final static int MAX_ALPHA = 255;

    protected final static float SCALED_FRACTION = 0.3F;
    protected final static float TITLE_ACTIVE_ICON_SCALE_BY = 0F;
    protected final static float TITLE_ACTIVE_SCALE_BY = 0.0F;
    protected final static float TITLE_SIZE_FRACTION = 1F;
    protected final static float TITLE_MARGIN_FRACTION = 1F;
    protected final static float TITLE_MARGIN_SCALE_FRACTION = 0.5F;

    protected final static float BADGE_HORIZONTAL_FRACTION = 0.5F;
    protected final static float BADGE_VERTICAL_FRACTION = 0.75F;
    protected final static float BADGE_TITLE_SIZE_FRACTION = 0.9F;

    protected final static float LEFT_FRACTION = 0.25F;
    protected final static float CENTER_FRACTION = 0.5F;
    protected final static float RIGHT_FRACTION = 0.75F;

    protected final static Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    protected final static Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    protected final static Interpolator OUT_SLOW_IN_INTERPOLATOR = new LinearOutSlowInInterpolator();

    // NTB and pointer bounds
    protected final RectF mBounds = new RectF();
    protected final RectF mBgBounds = new RectF();
    protected final RectF mPointerBounds = new RectF();
    // Badge bounds and bg badge bounds
    protected final Rect mBadgeBounds = new Rect();
    protected final RectF mBgBadgeBounds = new RectF();

    // Canvas, where all of other canvas will be merged
    protected Bitmap mBitmap;
    protected final Canvas mCanvas = new Canvas();



    // Canvas with titles
    protected Bitmap mTitlesBitmap;
    protected final Canvas mTitlesCanvas = new Canvas();

    // Canvas for our rect pointer
    protected Bitmap mPointerBitmap;
    protected final Canvas mPointerCanvas = new Canvas();

    // External background view for the NTB
    protected NavigationTabBarBehavior mBehavior;

    // Detect if behavior already set
    protected boolean mIsBehaviorSet;
    // Detect if behavior enabled
    protected boolean mBehaviorEnabled;
    // Detect if need to hide NTB
    protected boolean mNeedHide;
    // Detect if need animate animate or force hide
    protected boolean mAnimateHide;

    // Main paint
    protected final Paint mPaint = new Paint(FLAGS) {
        {
            setStyle(Style.FILL);
        }
    };
    // Background color paint
    protected final Paint mBgPaint = new Paint(FLAGS) {
        {
            setStyle(Style.FILL);
        }
    };
    // Pointer paint
    protected final Paint mPointerPaint = new Paint(FLAGS) {
        {
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }
    };


    // Paint for icon mask pointer
    protected final Paint mIconPointerPaint = new Paint(FLAGS) {
        {
            setStyle(Style.FILL);
            setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        }
    };

    // Paint for model title
    protected final Paint mModelTitlePaint = new TextPaint(FLAGS) {
        {
            setColor(Color.parseColor("#000000"));
            setTextAlign(Align.CENTER);
        }
    };


    // Variables for animator
    protected final ValueAnimator mAnimator = new ValueAnimator();
    protected final ResizeInterpolator mResizeInterpolator = new ResizeInterpolator();
    protected int mAnimationDuration;

    // NTB models
    protected final List<Model> mModels = new ArrayList<>();

    // Variables for ViewPager
    protected ViewPager mViewPager;
    protected ViewPager.OnPageChangeListener mOnPageChangeListener;
    protected int mScrollState;

    // Tab listener
    protected OnTabBarSelectedIndexListener mOnTabBarSelectedIndexListener;
    protected ValueAnimator.AnimatorListener mAnimatorListener;

    // Variables for sizes
    protected float mModelSize;


    // Corners radius for rect mode
    protected float mCornersRadius;

    // Model title size and margin
    protected float mModelTitleSize = 50f;
    protected float mTitleMargin;

    // Model badge title size and margin
    protected float mBadgeMargin;
    protected float mBadgeTitleSize = AUTO_SIZE;

    // Model title mode: active ar all
    protected TitleMode mTitleMode;





    // Indexes
    protected int mLastIndex = INVALID_INDEX;
    protected int mIndex = INVALID_INDEX;
    // General fraction value
    protected float mFraction;

    // Coordinates of pointer
    protected float mStartPointerX;
    protected float mEndPointerX;
    protected float mPointerLeftTop;
    protected float mPointerRightBottom;

    // Detect if model has title
    protected boolean mIsTitled;
    // Detect if model has badge
    protected boolean mIsBadged;
    // Detect if model icon scaled
    protected boolean mIsScaled;
    // Detect if model icon tinted
    protected boolean mIsTinted;
    // Detect if model can swiped
    protected boolean mIsSwiped;
    // Detect if model badge have custom typeface
    protected boolean mIsBadgeUseTypeface;
    // Detect if is bar mode or indicator pager mode
    protected boolean mIsViewPagerMode;
    // Detect whether the horizontal orientation
    protected boolean mIsHorizontalOrientation;
    // Detect if we move from left to right
    protected boolean mIsResizeIn;
    // Detect if we get action down event
    protected boolean mIsActionDown;
    // Detect if we get action down event on pointer
    protected boolean mIsPointerActionDown;
    // Detect when we set index from tab bar nor from ViewPager
    protected boolean mIsSetIndexFromTabBar;

    // Color variables
    protected int mInactiveColor;
    protected int mActiveColor;
    protected int mBgColor;

    // Custom typeface
    protected Typeface mTypeface;

    public NavigationTabBar(final Context context) {
        this(context, null);
    }

    public NavigationTabBar(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings({"ResourceAsColor", "ResourceType"})
    public NavigationTabBar(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //Init NTB

        // Always draw
        setWillNotDraw(false);
        // Speed and fix for pre 17 API
//        ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, null);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        final TypedArray typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.NavigationTabBar);
        try {
            setIsTitled(typedArray.getBoolean(R.styleable.NavigationTabBar_ntb_titled, true));

            setIsScaled(typedArray.getBoolean(R.styleable.NavigationTabBar_ntb_scaled, true));
            setIsTinted(typedArray.getBoolean(R.styleable.NavigationTabBar_ntb_tinted, true));
            setIsSwiped(typedArray.getBoolean(R.styleable.NavigationTabBar_ntb_swiped, true));
//            setTitleSize(
//                    typedArray.getDimension(R.styleable.NavigationTabBar_ntb_title_size, AUTO_SIZE)
//            );


            setTitleMode(
                    typedArray.getInt(
                           R.styleable.NavigationTabBar_ntb_title_mode, TitleMode.ALL_INDEX
                    )
            );



            setInactiveColor(
                    typedArray.getColor(
                           R.styleable.NavigationTabBar_ntb_inactive_color, DEFAULT_INACTIVE_COLOR
                    )
            );



            setActiveColor(
                    typedArray.getColor(
                           R.styleable.NavigationTabBar_ntb_active_color, DEFAULT_ACTIVE_COLOR
                    )
            );
            setBgColor(
                    typedArray.getColor(
                          R.styleable.NavigationTabBar_ntb_bg_color, DEFAULT_BG_COLOR
                    )
            );
            setAnimationDuration(
                    typedArray.getInteger(
                         R.styleable.NavigationTabBar_ntb_animation_duration,
                            DEFAULT_ANIMATION_DURATION
                    )
            );
            setCornersRadius(
                    typedArray.getDimension(R.styleable.NavigationTabBar_ntb_corners_radius, 0.0F)
            );


            // Init animator
            mAnimator.setFloatValues(MIN_FRACTION, MAX_FRACTION);
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final ValueAnimator animation) {
                    updateIndicatorPosition((Float) animation.getAnimatedValue());
                }
            });

            // Set preview models
            if (isInEditMode()) {
                // Get preview colors
                String[] previewColors = null;
                try {
                    final int previewColorsId = typedArray.getResourceId(
                           R.styleable.NavigationTabBar_ntb_preview_colors, 0
                    );
                    previewColors = previewColorsId == 0 ? null :
                            typedArray.getResources().getStringArray(previewColorsId);
                } catch (Exception exception) {
                    previewColors = null;
                    exception.printStackTrace();
                } finally {
                    if (previewColors == null)
                        previewColors =
                                typedArray.getResources().getStringArray(R.array.default_preview);


                    requestLayout();
                }
            }
        } finally {
            typedArray.recycle();
        }
    }

    public int getAnimationDuration() {
        return mAnimationDuration;
    }

    public void setAnimationDuration(final int animationDuration) {
        mAnimationDuration = animationDuration;
        mAnimator.setDuration(mAnimationDuration);
        resetScroller();
    }

    public List<Model> getModels() {
        return mModels;
    }

    public void setModels(final List<Model> models) {


        mModels.addAll(models);
        requestLayout();
    }

    public boolean isTitled() {
        return true;
    }

    public void setIsTitled(final boolean isTitled) {
        mIsTitled = isTitled;
        requestLayout();
    }

    public boolean isBadged() {
        return mIsBadged;
    }



    public boolean isScaled() {
        return mIsScaled;
    }

    public void setIsScaled(final boolean isScaled) {
        mIsScaled = isScaled;
        requestLayout();
    }



    public void setIsTinted(final boolean isTinted) {
        mIsTinted = isTinted;
//        updateTint();
    }

    public boolean isSwiped() {
        return mIsSwiped;
    }

    public void setIsSwiped(final boolean swiped) {
        mIsSwiped = swiped;
    }

    public float getTitleSize() {
        return mModelTitleSize;
    }


    public TitleMode getTitleMode() {
        return mTitleMode;
    }

    protected void setTitleMode(final int index) {
        switch (index) {
            case TitleMode.ACTIVE_INDEX:
                setTitleMode(TitleMode.ACTIVE);
                break;
            case TitleMode.ALL_INDEX:
            default:
                setTitleMode(TitleMode.ALL);
                break;
        }
    }

    public void setTitleMode(final TitleMode titleMode) {
        mTitleMode = titleMode;
        postInvalidate();
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(final String typeface) {
        if (TextUtils.isEmpty(typeface)) return;

        Typeface tempTypeface;
        try {
            tempTypeface = Typeface.createFromAsset(getContext().getAssets(), typeface);
        } catch (Exception e) {
            tempTypeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
            e.printStackTrace();
        }

        setTypeface(tempTypeface);
    }

    public void setTypeface(final Typeface typeface) {
        mTypeface = typeface;
        mModelTitlePaint.setTypeface(typeface);

        postInvalidate();
    }


    public int getActiveColor() {
        return mActiveColor;
    }

    public void setActiveColor(final int activeColor) {
        mActiveColor = activeColor;

        // Set icon pointer active color
        mIconPointerPaint.setColor(mActiveColor);
//        updateTint();
    }

    public int getInactiveColor() {
        return mInactiveColor;
    }

    public void setInactiveColor(final int inactiveColor) {
        mInactiveColor = inactiveColor;

        // Set inactive color to title
        mModelTitlePaint.setColor(mInactiveColor);
//        updateTint();
    }

    public int getBgColor() {
        return mBgColor;
    }

    public void setBgColor(final int bgColor) {
        mBgColor = bgColor;
        mBgPaint.setColor(mBgColor);
        postInvalidate();
    }

    public float getCornersRadius() {
        return mCornersRadius;
    }

    public void setCornersRadius(final float cornersRadius) {
        mCornersRadius = cornersRadius;
        postInvalidate();
    }





    public float getBarHeight() {
        return mBounds.height();
    }

    public OnTabBarSelectedIndexListener getOnTabBarSelectedIndexListener() {
        return mOnTabBarSelectedIndexListener;
    }

    // Set on tab bar selected index listener where you can trigger action onStart or onEnd
    public void setOnTabBarSelectedIndexListener(
            final OnTabBarSelectedIndexListener onTabBarSelectedIndexListener
    ) {
        mOnTabBarSelectedIndexListener = onTabBarSelectedIndexListener;

        if (mAnimatorListener == null)
            mAnimatorListener = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(final Animator animation) {
                    if (mOnTabBarSelectedIndexListener != null)
                        mOnTabBarSelectedIndexListener.onStartTabSelected(
                                mModels.get(mIndex), mIndex
                        );

                    animation.removeListener(this);
                    animation.addListener(this);
                }

                @Override
                public void onAnimationEnd(final Animator animation) {
                    if (mIsViewPagerMode) return;

                    animation.removeListener(this);
                    animation.addListener(this);

                    if (mOnTabBarSelectedIndexListener != null)
                        mOnTabBarSelectedIndexListener.onEndTabSelected(
                                mModels.get(mIndex), mIndex
                        );
                }
            };
        mAnimator.removeListener(mAnimatorListener);
        mAnimator.addListener(mAnimatorListener);
    }

    public void setViewPager(final ViewPager viewPager) {
        // Detect whether ViewPager mode
        if (viewPager == null) {
            mIsViewPagerMode = false;
            return;
        }

        if (viewPager.equals(mViewPager)) return;
        if (mViewPager != null) //noinspection deprecation
            mViewPager.setOnPageChangeListener(null);
        if (viewPager.getAdapter() == null)
            throw new IllegalStateException("ViewPager does not provide adapter instance.");

        mIsViewPagerMode = true;
        mViewPager = viewPager;
        mViewPager.removeOnPageChangeListener(this);
        mViewPager.addOnPageChangeListener(this);

        resetScroller();
        postInvalidate();
    }

    public void setViewPager(final ViewPager viewPager, int index) {
        setViewPager(viewPager);

        mIndex = index;
        if (mIsViewPagerMode) mViewPager.setCurrentItem(index, true);
        postInvalidate();
    }

    // Reset scroller and reset scroll duration equals to animation duration
    protected void resetScroller() {
        if (mViewPager == null) return;
        try {
            final Field scrollerField = ViewPager.class.getDeclaredField("mScroller");
            scrollerField.setAccessible(true);
            final ResizeViewPagerScroller scroller = new ResizeViewPagerScroller(getContext());
            scrollerField.set(mViewPager, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnPageChangeListener(final ViewPager.OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    // Return if the behavior translation is enabled
    public boolean isBehaviorEnabled() {
        return mBehaviorEnabled;
    }

    // Set the behavior translation value
    public void setBehaviorEnabled(final boolean enabled) {
        mBehaviorEnabled = enabled;

        if (getParent() != null && getParent() instanceof CoordinatorLayout) {
            final ViewGroup.LayoutParams params = getLayoutParams();
            if (mBehavior == null) mBehavior = new NavigationTabBarBehavior(enabled);
            else mBehavior.setBehaviorTranslationEnabled(enabled);

            ((CoordinatorLayout.LayoutParams) params).setBehavior(mBehavior);
            if (mNeedHide) {
                mNeedHide = false;
                mBehavior.hideView(this, (int) getBarHeight(), mAnimateHide);
            }
        }
    }

    public int getModelIndex() {
        return mIndex;
    }

    public void setModelIndex(int index) {
        setModelIndex(index, false);
    }

    // Set model index from touch or programmatically
    public void setModelIndex(final int modelIndex, final boolean isForce) {
        if (mAnimator.isRunning()) return;
        if (mModels.isEmpty()) return;

        int index = modelIndex;
        boolean force = isForce;

        // This check gives us opportunity to have an non selected model
        if (mIndex == INVALID_INDEX) force = true;
        // Detect if last is the same
        if (index == mIndex) force = true;
        // Snap index to models size
        index = Math.max(0, Math.min(index, mModels.size() - 1));

        mIsResizeIn = index < mIndex;
        mLastIndex = mIndex;
        mIndex = index;

        mIsSetIndexFromTabBar = true;
        if (mIsViewPagerMode) {
            if (mViewPager == null) throw new IllegalStateException("ViewPager is null.");
            mViewPager.setCurrentItem(index, !force);
        }

        // Set startX and endX for animation,
        // where we animate two sides of rect with different interpolation
        if (force) {
            mStartPointerX = mIndex * mModelSize;
            mEndPointerX = mStartPointerX;
        } else {
            mStartPointerX = mPointerLeftTop;
            mEndPointerX = mIndex * mModelSize;
        }

        // If it force, so update immediately, else animate
        // This happens if we set index onCreate or something like this
        // You can use force param or call this method in some post()
        if (force) {
            updateIndicatorPosition(MAX_FRACTION);

            if (mOnTabBarSelectedIndexListener != null)
                mOnTabBarSelectedIndexListener.onStartTabSelected(mModels.get(mIndex), mIndex);

            // Force onPageScrolled listener and refresh VP
            if (mIsViewPagerMode) {
                if (!mViewPager.isFakeDragging()) mViewPager.beginFakeDrag();
                if (mViewPager.isFakeDragging()) mViewPager.fakeDragBy(0.0F);
                if (mViewPager.isFakeDragging()) mViewPager.endFakeDrag();
            } else {
                if (mOnTabBarSelectedIndexListener != null)
                    mOnTabBarSelectedIndexListener.onEndTabSelected(mModels.get(mIndex), mIndex);
            }
        } else mAnimator.start();
    }

    // Deselect active index and reset pointer
    public void deselect() {
        mLastIndex = INVALID_INDEX;
        mIndex = INVALID_INDEX;
        mStartPointerX = INVALID_INDEX * mModelSize;
        mEndPointerX = mStartPointerX;
        updateIndicatorPosition(MIN_FRACTION);
    }

    protected void updateIndicatorPosition(final float fraction) {
        // Update general fraction
        mFraction = fraction;

        // Set the pointer left top side coordinate
        mPointerLeftTop = mStartPointerX +
                (mResizeInterpolator.getResizeInterpolation(fraction, mIsResizeIn) *
                        (mEndPointerX - mStartPointerX));
        // Set the pointer right bottom side coordinate
        mPointerRightBottom = (mStartPointerX + mModelSize) +
                (mResizeInterpolator.getResizeInterpolation(fraction, !mIsResizeIn) *
                        (mEndPointerX - mStartPointerX));

        // Update pointer
        postInvalidate();
    }

    // Update NTB
    protected void notifyDataSetChanged() {
        requestLayout();
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        // Return if animation is running
        if (mAnimator.isRunning()) return true;
        // If is not idle state, return
        if (mScrollState != ViewPager.SCROLL_STATE_IDLE) return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Action down touch
                mIsActionDown = true;
                if (!mIsViewPagerMode) break;
                if (!mIsSwiped) break;
                // Detect if we touch down on pointer, later to move
                if (mIsHorizontalOrientation)
                    mIsPointerActionDown = (int) (event.getX() / mModelSize) == mIndex;
                else
                    mIsPointerActionDown = (int) (event.getY() / mModelSize) == mIndex;
                break;
            case MotionEvent.ACTION_MOVE:
                // If pointer touched, so move
                if (mIsPointerActionDown) {
                    if (mIsHorizontalOrientation)
                        mViewPager.setCurrentItem((int) (event.getX() / mModelSize), true);
                    else
                        mViewPager.setCurrentItem((int) (event.getY() / mModelSize), true);
                    break;
                }
                if (mIsActionDown) break;
            case MotionEvent.ACTION_UP:
                // Press up and set model index relative to current coordinate
                if (mIsActionDown) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (mIsHorizontalOrientation) setModelIndex((int) (event.getX() / mModelSize));
                    else setModelIndex((int) (event.getY() / mModelSize));
                }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            default:
                // Reset action touch variables
                mIsPointerActionDown = false;
                mIsActionDown = false;
                break;
        }

        return true;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get measure size
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        if (mModels.isEmpty() || width == 0 || height == 0) return;

        // Detect orientation and calculate icon size
        if (width > height) {
            mIsHorizontalOrientation = true;

            // Get model size
            mModelSize = (float) width / (float) mModels.size();

            // Get smaller side
            float side = mModelSize > height ? height : mModelSize;



            if (mModelTitleSize == AUTO_SIZE) mModelTitleSize = side * TITLE_SIZE_FRACTION;
            mTitleMargin = side * TITLE_MARGIN_FRACTION;

            // If is badged mode, so get vars and set paint with default bounds
            if (mIsBadged) {

            }
        } else {
            // Disable vertical translation in coordinator layout
            mBehaviorEnabled = false;
            // Disable other features
            mIsHorizontalOrientation = false;



            mModelSize = (float) height / (float) mModels.size();
            // Get smaller side
            float side = mModelSize > width ? width : mModelSize;



            if (mModelTitleSize == AUTO_SIZE) mModelTitleSize = side * TITLE_SIZE_FRACTION;
            mTitleMargin = side * TITLE_MARGIN_FRACTION;
        }

        // Set bounds for NTB
        mBounds.set(0.0F, 0.0F, width, height - mBadgeMargin);


        mBgBounds.set(0.0F, 0, mBounds.width(), mBounds.height() );



        // Reset bitmap to init it onDraw()
        mBitmap = null;
        mPointerBitmap = null;
//        mIconsBitmap = null;
        if (mIsTitled) mTitlesBitmap = null;

        // Set start position of pointer for preview or on start
        if (isInEditMode() || !mIsViewPagerMode) {
            mIsSetIndexFromTabBar = true;

            // Set random in preview mode
            if (isInEditMode()) {
                mIndex = new Random().nextInt(mModels.size());


            }

            mStartPointerX = mIndex * mModelSize;
            mEndPointerX = mStartPointerX;
            updateIndicatorPosition(MAX_FRACTION);
        }

        //The translation behavior has to be set up after the super.onMeasure has been called
        if (!mIsBehaviorSet) {
            setBehaviorEnabled(mBehaviorEnabled);
            mIsBehaviorSet = true;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onDraw(final Canvas canvas) {
        // Get height of NTB with badge on nor
        final int mBadgedHeight = (int) (mBounds.height() + mBadgeMargin);

        // Set main canvas
        if (mBitmap == null || mBitmap.isRecycled()) {
            mBitmap = Bitmap.createBitmap(
                    (int) mBounds.width(), mBadgedHeight, Bitmap.Config.ARGB_8888
            );
            mCanvas.setBitmap(mBitmap);
        }
        // Set pointer canvas
        if (mPointerBitmap == null || mPointerBitmap.isRecycled()) {
            mPointerBitmap = Bitmap.createBitmap(
                    (int) mBounds.width(), mBadgedHeight, Bitmap.Config.ARGB_8888
            );
            mPointerCanvas.setBitmap(mPointerBitmap);
        }

        // Set titles canvas
        if (mIsTitled) {
            if (mTitlesBitmap == null || mTitlesBitmap.isRecycled()) {
                mTitlesBitmap = Bitmap.createBitmap(
                        (int) mBounds.width(), mBadgedHeight, Bitmap.Config.ARGB_8888
                );
                mTitlesCanvas.setBitmap(mTitlesBitmap);
            }
        } else mTitlesBitmap = null;

        // Reset and clear canvases
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        mPointerCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        if (mIsTitled) mTitlesCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        if (mCornersRadius == 0) canvas.drawRect(mBgBounds, mBgPaint);
        else canvas.drawRoundRect(mBgBounds, mCornersRadius, mCornersRadius, mBgPaint);



        // Draw our model colors
        for (int i = 0; i < mModels.size(); i++) {
            mPaint.setColor(mModels.get(i).getColor());

            if (mIsHorizontalOrientation) {
                final float left = mModelSize * i;
                final float right = left + mModelSize;
                mCanvas.drawRect(
                        left, 0, right, mBounds.height() , mPaint
                );
            } else {
                final float top = mModelSize * i;
                final float bottom = top + mModelSize;
                mCanvas.drawRect(0.0F, top, mBounds.width(), bottom, mPaint);
            }
        }

        // Set bound of pointer
        if (mIsHorizontalOrientation) mPointerBounds.set(
                mPointerLeftTop,
                0,
                mPointerRightBottom,
                mBounds.height()
        );
        else mPointerBounds.set(0.0F, mPointerLeftTop, mBounds.width(), mPointerRightBottom);

        // Draw pointer for model colors
        if (mCornersRadius == 0) mPointerCanvas.drawRect(mPointerBounds, mPaint);
        else mPointerCanvas.drawRoundRect(mPointerBounds, mCornersRadius, mCornersRadius, mPaint);

        // Draw pointer into main canvas
        mCanvas.drawBitmap(mPointerBitmap, 0.0F, 0.0F, mPointerPaint);


        // Draw model icons
        for (int i = 0; i < mModels.size(); i++) {
            final Model model = mModels.get(i);

            // Variables to center our icons
            final float leftOffset;
            final float topOffset;
            final float matrixCenterX;
            final float matrixCenterY;

            // Set offset to titles
            final float leftTitleOffset;
            final float topTitleOffset;
            if (mIsHorizontalOrientation) {
                leftOffset = (mModelSize * i) + (mModelSize ) * 0.5F;
                topOffset = (mBounds.height()) * 0.5F;

                // Set offset to titles
                leftTitleOffset = (mModelSize * i) + (mModelSize * 0.5F);
                topTitleOffset =
                        mBounds.height() -(mBounds.height() - mModelTitleSize)*0.57F;

            } else {
                leftOffset = (mBounds.width() )* 0.5F;
                topOffset = (mModelSize * i)* 0.5F;

                // Set offset to titles
                leftTitleOffset = leftOffset  * 0.5F;
                topTitleOffset = topOffset * 0.5f;
            }

            matrixCenterX = leftOffset  * 0.5F;
            matrixCenterY = topOffset* 0.5F;

            // Title translate position
            final float titleTranslate =
                    topOffset* TITLE_MARGIN_SCALE_FRACTION;

            // Translate icon to model center
            model.mIconMatrix.setTranslate(
                    leftOffset,
                    (mIsTitled && mTitleMode == TitleMode.ALL) ? titleTranslate : topOffset
            );

            // Get interpolated fraction for left last and current models
            final float interpolation =
                    mResizeInterpolator.getResizeInterpolation(mFraction, true);
            final float lastInterpolation =
                    mResizeInterpolator.getResizeInterpolation(mFraction, false);

            // Scale value relative to interpolation
            final float matrixScale =0f * interpolation;
            final float matrixLastScale = 0f* lastInterpolation;

            // Get title alpha relative to interpolation
            final int titleAlpha = (int) (MAX_ALPHA * interpolation);
            final int titleLastAlpha = MAX_ALPHA - (int) (MAX_ALPHA * lastInterpolation);
            // Get title scale relative to interpolation
            final float titleScale = mIsScaled ?
                    MAX_FRACTION + interpolation * TITLE_ACTIVE_SCALE_BY : MAX_FRACTION;
            final float titleLastScale = mIsScaled ? (MAX_FRACTION + TITLE_ACTIVE_SCALE_BY) -
                    (lastInterpolation * TITLE_ACTIVE_SCALE_BY) : titleScale;



            // Check if we handle models from touch on NTB or from ViewPager
            // There is a strange logic
            // of ViewPager onPageScrolled method, so it is
            if (mIsSetIndexFromTabBar) {
                if (mIndex == i)
                    updateCurrentModel(

                            titleScale,
                            titleAlpha
                    );
                else if (mLastIndex == i)
                    updateLastModel(
                            model,
                            leftOffset,
                            topOffset,
                            titleTranslate,
                            lastInterpolation,
                            titleLastScale,
                            titleLastAlpha
                    );
                else
                    updateInactiveModel( model,
                            leftOffset,
                            topOffset,
                            titleScale,
                            matrixScale,
                            matrixCenterX,
                            matrixCenterY);
            } else {
                if (i == mIndex + 1)
                    updateCurrentModel(

                            titleScale,
                            titleAlpha
                    );
                else if (i == mIndex)
                    updateLastModel(
                            model,
                            leftOffset,
                            topOffset,
                            titleTranslate,
                            lastInterpolation,
                            titleLastScale,
                            titleLastAlpha
                    );
                else updateInactiveModel(  model,
                            leftOffset,
                            topOffset,
                            titleScale,
                            matrixScale,
                            matrixCenterX,
                            matrixCenterY);

            }

            if (mIsTitled) mTitlesCanvas.drawText(
                    isInEditMode() ? PREVIEW_TITLE : model.getTitle(),
                    leftTitleOffset,
                    topTitleOffset,
                    mModelTitlePaint
            );
        }

        // Reset pointer bounds for icons and titles
        if (mIsHorizontalOrientation)
            mPointerBounds.set(mPointerLeftTop, 0.5F, mPointerRightBottom, mBounds.height());
        if (mCornersRadius == 0) {
//            if (mIsTinted) mIconsCanvas.drawRect(mPointerBounds, mIconPointerPaint);
            if (mIsTitled) mTitlesCanvas.drawRect(mPointerBounds, mIconPointerPaint);
        } else {

            if (mIsTitled) mTitlesCanvas.drawRoundRect(
                    mPointerBounds, mCornersRadius, mCornersRadius, mIconPointerPaint
            );
        }

        // Draw general bitmap
        canvas.drawBitmap(mBitmap, 0.5F, 0.5F, null);

        // Draw titles bitmap on top
        if (mIsTitled) canvas.drawBitmap(mTitlesBitmap, 0.5F, 0, null);


    }

    // Method to transform current fraction of NTB and position
    protected void updateCurrentModel(

            final float titleScale,
            final int titleAlpha
    ) {

        mModelTitlePaint.setTextSize(mModelTitleSize * titleScale);
        if (mTitleMode == TitleMode.ACTIVE) mModelTitlePaint.setAlpha(titleAlpha);

    }

    // Method to transform last fraction of NTB and position
    protected void updateLastModel(
            final Model model,
            final float leftOffset,
            final float topOffset,
            final float titleTranslate,
            final float lastInterpolation,
            final float titleLastScale,
            final int titleLastAlpha
    ) {
        if (mIsTitled && mTitleMode == TitleMode.ACTIVE) model.mIconMatrix.setTranslate(
                leftOffset, titleTranslate + (lastInterpolation * (topOffset - titleTranslate))
        );



        mModelTitlePaint.setTextSize(mModelTitleSize * titleLastScale);
        if (mTitleMode == TitleMode.ACTIVE) mModelTitlePaint.setAlpha(MIN_ALPHA);



    }

    // Method to transform others fraction of NTB and position
    protected void updateInactiveModel(
            final Model model,
            final float leftOffset,
            final float topOffset,
            final float textScale,
            final float matrixScale,
            final float matrixCenterX,
            final float matrixCenterY
    ) {
        if (mIsTitled && mTitleMode == TitleMode.ACTIVE)
            model.mIconMatrix.setTranslate(leftOffset, topOffset);
        model.mIconMatrix.postScale(
               0, 0, matrixCenterX, matrixCenterY
        );


        mModelTitlePaint.setTextSize(mModelTitleSize);
        if (mTitleMode == TitleMode.ACTIVE) mModelTitlePaint.setAlpha(MIN_ALPHA);



    }


    @Override
    public void onPageScrolled(int position, float positionOffset, final int positionOffsetPixels) {
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);

        // If we animate, don`t call this
        if (!mIsSetIndexFromTabBar) {
            mIsResizeIn = position < mIndex;
            mLastIndex = mIndex;
            mIndex = position;

            mStartPointerX = position * mModelSize;
            mEndPointerX = mStartPointerX + mModelSize;
            updateIndicatorPosition(positionOffset);
        }

        // Stop scrolling on animation end and reset values
        if (!mAnimator.isRunning() && mIsSetIndexFromTabBar) {
            mFraction = MIN_FRACTION;
            mIsSetIndexFromTabBar = false;
        }
    }

    @Override
    public void onPageSelected(final int position) {
        // This method is empty, because we call onPageSelected() when scroll state is idle
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        // If VP idle, reset to MIN_FRACTION
        mScrollState = state;
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            if (mOnPageChangeListener != null) mOnPageChangeListener.onPageSelected(mIndex);
            if (mIsViewPagerMode && mOnTabBarSelectedIndexListener != null)
                mOnTabBarSelectedIndexListener.onEndTabSelected(mModels.get(mIndex), mIndex);
        }

        if (mOnPageChangeListener != null) mOnPageChangeListener.onPageScrollStateChanged(state);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mIndex = savedState.index;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        savedState.index = mIndex;
        return savedState;
    }

    protected static class SavedState extends BaseSavedState {

        private int index;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            index = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(index);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected void onConfigurationChanged(final Configuration newConfig) {
        // Config view on rotate etc.
        super.onConfigurationChanged(newConfig);
        requestLayout();

        // Refresh pointer and state after config changed to current
        final int tempIndex = mIndex;
        deselect();
        post(new Runnable() {
            @Override
            public void run() {
                setModelIndex(tempIndex, true);
            }
        });
    }

    // Clamp value to max and min bounds
    protected float clampValue(final float value) {
        return Math.max(
                Math.min(value, NavigationTabBar.MAX_FRACTION), NavigationTabBar.MIN_FRACTION
        );
    }

    // Hide NTB with animation
    public void hide() {
        if (mBehavior != null) mBehavior.hideView(this, (int) getBarHeight(), true);
        else if (getParent() != null && getParent() instanceof CoordinatorLayout) {
            mNeedHide = true;
            mAnimateHide = true;
        } else scrollDown();
    }

    // Show NTB with animation
    public void show() {
        if (mBehavior != null) mBehavior.resetOffset(this, true);
        else scrollUp();
    }

    // Hide NTB or bg on scroll down
    protected void scrollDown() {
        ViewCompat.animate(this)
                .translationY(getBarHeight())
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(DEFAULT_ANIMATION_DURATION)
                .start();
    }

    // Show NTB or bg on scroll up
    protected void scrollUp() {
        ViewCompat.animate(this)
                .translationY(0.0F)
                .setInterpolator(OUT_SLOW_IN_INTERPOLATOR)
                .setDuration(DEFAULT_ANIMATION_DURATION)
                .start();
    }

    // Model class
    public static class Model {

        private int mColor;
   private final Matrix mIconMatrix = new Matrix();
        private String mTitle;

        Model(final Builder builder) {
            mColor = builder.mColor;
            mTitle = builder.mTitle;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(final String title) {
            mTitle = title;
        }

        public int getColor() {
            return mColor;
        }

        public void setColor(final int color) {
            mColor = color;
        }



        public static class Builder {
            private final int mColor;
            private String mTitle;
            public Builder( final int color) {
                mColor = color;
            }

            public Builder title(final String title) {
                mTitle = title;
                return this;
            }

            public Model build() {
                return new Model(this);
            }
        }
    }

    // Custom scroller with custom scroll duration
    protected class ResizeViewPagerScroller extends Scroller {

        ResizeViewPagerScroller(Context context) {
            super(context, new AccelerateDecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mAnimationDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mAnimationDuration);
        }
    }

    // Resize interpolator to create smooth effect on pointer according to inspiration design
    // This is like improved accelerated and decelerated interpolator
    protected class ResizeInterpolator implements Interpolator {

        // Spring factor
        private final static float FACTOR = 0.5F;
        // Check whether side we move
        private boolean mResizeIn;

        @Override
        public float getInterpolation(final float input) {
            if (mResizeIn) return (float) (1.0F - Math.pow((1.0F - input), 2.0F * FACTOR));
            else return (float) (Math.pow(input, 2.0F * FACTOR));
        }

        private float getResizeInterpolation(final float input, final boolean resizeIn) {
            mResizeIn = resizeIn;
            return getInterpolation(input);
        }
    }

    // Model title mode
    public enum TitleMode {
        ALL, ACTIVE;

        public final static int ALL_INDEX = 0;
        public final static int ACTIVE_INDEX = 1;
    }

    // Out listener for selected index
    public interface OnTabBarSelectedIndexListener {
        void onStartTabSelected(final Model model, final int index);

        void onEndTabSelected(final Model model, final int index);
    }
}
