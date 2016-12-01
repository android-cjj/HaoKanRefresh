package com.github.cjj.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.cjj.library.core.RefreshLayout;
import com.github.cjj.library.core.WaveView;
import com.github.cjj.library.listener.OnRefreshListener;
import com.github.cjj.library.listener.PullToRefreshListener;
import com.github.cjj.library.listener.PullWaveListener;
import com.github.cjj.library.utils.DensityUtil;
import com.github.cjj.library.utils.LogHelper;


/**
 * 你好，当你看到这个类的时候，说明你已经知道我了。但是，我还是要介绍下：我是CJJ,会写代码
 * 会写小说，会唱歌，会踢球，最主要的是，装得一手好逼。恩，我还喜欢搞基，加我微信androidcjj.
 */
public class HaoKanRefreshLayout extends RefreshLayout {
    private static final float WAVE_HEIGHT = 140;
    private static final float HEAD_HEIGHT = 70;

    private WaveView mWaveView;
    private OnRefreshListener mOnRefreshListener;

    public HaoKanRefreshLayout(Context context) {
        this(context, null, 0);
    }

    public HaoKanRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HaoKanRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 初始化
     */
    private void init(AttributeSet attrs) {
        /**
         * attrs  需要在xml设置什么属性  自己自定义吧  啊哈哈
         */

        /**
         * 初始化headView
         */
        final View headView = LayoutInflater.from(getContext()).inflate(R.layout.view_head, null);
        mWaveView = (WaveView) headView.findViewById(R.id.draweeView);
        final TextView tvRefreshTip = (TextView) headView.findViewById(R.id.tv_refresh_tip);
        final ImageView ivBallJump = (ImageView) headView.findViewById(R.id.iv_ball_jump);
        ivBallJump.setImageResource(R.drawable.ball_jump_anim);
        final AnimationDrawable animationDrawable = (AnimationDrawable) ivBallJump.getDrawable();
        /**
         * 设置波浪的高度
         */
        setWaveHeight(DensityUtil.dip2px(getContext(), WAVE_HEIGHT));
        /**
         * 设置headView的高度
         */
        setHeaderHeight(DensityUtil.dip2px(getContext(), HEAD_HEIGHT));
        /**
         * 设置headView
         */
        setHeaderView(headView);
        /**
         * 监听波浪变化监听
         */
        setPullWaveListener(new PullWaveListener() {
            @Override
            public void onPulling(RefreshLayout refreshLayout, float fraction) {
                float headW = DensityUtil.dip2px(getContext(), WAVE_HEIGHT);
                mWaveView.setHeadHeight((int) (DensityUtil.dip2px(getContext(), HEAD_HEIGHT) * limitValue(1, fraction)));
                mWaveView.setWaveHeight((int) (headW * Math.max(0, fraction - 1)));
                mWaveView.invalidate();

                if (DensityUtil.dip2px(getContext(), HEAD_HEIGHT) > (int) (DensityUtil.dip2px(getContext(), HEAD_HEIGHT) * limitValue(1, fraction))) {
                    tvRefreshTip.setText("下拉刷新");
                } else {
                    tvRefreshTip.setText("释放刷新");
                }

                ivBallJump.setVisibility(GONE);
            }

            @Override
            public void onPullReleasing(RefreshLayout refreshLayout, float fraction) {

            }
        });

        /**
         * 松开后的监听
         */
        setPullToRefreshListener(new PullToRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshLayout) {
                tvRefreshTip.setText("");
                mWaveView.setHeadHeight(DensityUtil.dip2px(getContext(), HEAD_HEIGHT));
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh();
                }

                ValueAnimator animator = ValueAnimator.ofInt(mWaveView.getWaveHeight(), 0, -50, 0);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        LogHelper.log("value--->" + (int) animation.getAnimatedValue());
                        mWaveView.setWaveHeight((int) animation.getAnimatedValue());
                        mWaveView.invalidate();
                    }
                });
                animator.setInterpolator(new LinearInterpolator());
                animator.setDuration(250);
                animator.start();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ivBallJump.setVisibility(VISIBLE);
                        if (!animationDrawable.isRunning()) {
                            animationDrawable.start();
                        }
                    }
                }, 200);
            }
        });
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        mOnRefreshListener = refreshListener;
    }

    public void setWaveBackgroundColor(int color) {
        mWaveView.setColor(color);
    }

    /**
     * 限定值
     *
     * @param a
     * @param b
     * @return
     */
    public float limitValue(float a, float b) {
        float valve = 0;
        final float min = Math.min(a, b);
        final float max = Math.max(a, b);
        valve = valve > min ? valve : min;
        valve = valve < max ? valve : max;
        return valve;
    }

}
