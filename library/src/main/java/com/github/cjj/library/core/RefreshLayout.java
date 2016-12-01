package com.github.cjj.library.core;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.github.cjj.library.listener.PullToRefreshListener;
import com.github.cjj.library.listener.PullWaveListener;
import com.github.cjj.library.utils.LogHelper;

/**
 * hi,可以关注我GitHub:android-cjj,也可以关注我微博：AndroidCJJ，还可以加我微信：androidcjj。
 */
public class RefreshLayout extends FrameLayout {
    //波浪的高度
    protected float mWaveHeight;

    //头部的高度
    protected float mHeadHeight;

    //子控件
    private View mChildView;

    //头部layout
    protected FrameLayout mHeadLayout;

    //刷新的状态
    protected boolean mIsRefreshing;

    //触摸获得Y的位置
    private float mTouchY;

    //当前Y的位置
    private float mCurrentY;

    //动画的变化率
    private DecelerateInterpolator mDecelerateInterpolator;

    //设置wave监听
    private PullWaveListener mPullWaveListener;

    //设置下拉监听
    private PullToRefreshListener mPullToRefreshPullingListener;

    public RefreshLayout(Context context) {
        this(context, null, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        LogHelper.log("init");
    }

    /**
     * 初始化
     */
    private void init() {
        //使用isInEditMode解决可视化编辑器无法识别自定义控件的问题
        if (isInEditMode()) {
            return;
        }

        if (getChildCount() > 1) {
            throw new RuntimeException("只能拥有一个子控件哦");
        }

        //在动画开始的地方快然后慢;
        mDecelerateInterpolator = new DecelerateInterpolator(10);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        LogHelper.log("onAttachedToWindow");

        //添加头部
        FrameLayout headViewLayout = new FrameLayout(getContext());
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        layoutParams.gravity = Gravity.TOP;
        headViewLayout.setLayoutParams(layoutParams);

        mHeadLayout = headViewLayout;
        this.addView(mHeadLayout);

        //获得子控件
        mChildView = getChildAt(0);
    }

    private void setChildViewTransLocationY(float... values) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(mChildView, View.TRANSLATION_Y, values);
        oa.setInterpolator(new DecelerateInterpolator());
        oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) mChildView.getTranslationY();//获得mChildView当前y的位置

                LogHelper.log("mChildView.getTranslationY----------->" + height);
                mHeadLayout.getLayoutParams().height = height;
                mHeadLayout.requestLayout();//重绘

                if (mPullWaveListener != null) {
                    mPullWaveListener.onPullReleasing(RefreshLayout.this, height / mHeadHeight);
                }
            }
        });
        oa.start();
    }

    /**
     * 拦截事件
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsRefreshing) {
            return true;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchY = ev.getY();
                mCurrentY = mTouchY;
                break;
            case MotionEvent.ACTION_MOVE:
                float currentY = ev.getY();
                float dy = currentY - mTouchY;
                if (dy > 0 && !canChildScrollUp()) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 响应事件
     *
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mIsRefreshing) {
            return super.onTouchEvent(e);
        }

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mCurrentY = e.getY();

                float dy = mCurrentY - mTouchY;
                dy = Math.min(mWaveHeight * 2, dy);
                dy = Math.max(0, dy);

                if (mChildView != null) {
                    float offsetY = mDecelerateInterpolator.getInterpolation(dy / mWaveHeight / 2) * dy / 2;
                    mChildView.setTranslationY(offsetY);

                    mHeadLayout.getLayoutParams().height = (int) offsetY;
                    mHeadLayout.requestLayout();

                    if (mPullWaveListener != null) {
                        mPullWaveListener.onPulling(RefreshLayout.this, offsetY / mHeadHeight);
                    }
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mChildView != null) {
                    if (mChildView.getTranslationY() >= mHeadHeight) {
                        setChildViewTransLocationY(mHeadHeight);
                        mIsRefreshing = true;
                        if (mPullToRefreshPullingListener != null) {
                            mPullToRefreshPullingListener.onRefresh(RefreshLayout.this);
                        }
                    } else {
                        setChildViewTransLocationY(0);
                    }

                }
                return true;
        }
        return super.onTouchEvent(e);
    }

    /**
     * 用来判断是否可以上拉
     *
     * @return boolean
     */
    public boolean canChildScrollUp() {
        if (mChildView == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < 14) {
            if (mChildView instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mChildView;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mChildView, -1) || mChildView.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mChildView, -1);
        }
    }

    public void setPullToRefreshListener(PullToRefreshListener pullToRefreshPullingListener) {
        this.mPullToRefreshPullingListener = pullToRefreshPullingListener;
    }

    public void setPullWaveListener(PullWaveListener pullWaveListener) {
        this.mPullWaveListener = pullWaveListener;
    }

    public void setRefreshing(final boolean refreshing){
        post(new Runnable() {
            @Override
            public void run() {
                if(refreshing){
                    setChildViewTransLocationY(mHeadHeight);
                    mIsRefreshing = true;
                    if (mPullToRefreshPullingListener != null) {
                        mPullToRefreshPullingListener.onRefresh(RefreshLayout.this);
                    }
                }else {
                    mIsRefreshing = false;
                    setChildViewTransLocationY(0);
                }
            }
        });
    }

    /**
     * 设置头部View
     *
     * @param headerView
     */
    public void setHeaderView(final View headerView) {
        post(new Runnable() {
            @Override
            public void run() {
                mHeadLayout.addView(headerView);
            }
        });
    }

    /**
     * 设置wave的下拉高度
     *
     * @param waveHeight
     */
    public void setWaveHeight(float waveHeight) {
        this.mWaveHeight = waveHeight;
    }

    /**
     * 设置下拉头的高度
     *
     * @param headHeight
     */
    public void setHeaderHeight(float headHeight) {
        this.mHeadHeight = headHeight;
    }

}
