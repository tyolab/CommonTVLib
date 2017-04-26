package au.com.tyo.common.tv;

import android.animation.TimeAnimator;
import android.content.res.Resources;
import android.support.v17.leanback.R;
import android.support.v17.leanback.graphics.ColorOverlayDimmer;
import android.support.v17.leanback.widget.FocusHighlight;
import android.support.v17.leanback.widget.ShadowOverlayContainer;
import android.support.v17.leanback.widget.ShadowOverlayHelper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by monfee on 14/3/17.
 */

public class FocusHighlightHelper {


    static boolean isValidZoomIndex(int zoomIndex) {
        return zoomIndex == FocusHighlight.ZOOM_FACTOR_NONE || getResId(zoomIndex) > 0;
    }

    static int getResId(int zoomIndex) {
        switch (zoomIndex) {
            case FocusHighlight.ZOOM_FACTOR_SMALL:
                return R.fraction.lb_focus_zoom_factor_small;
            case FocusHighlight.ZOOM_FACTOR_XSMALL:
                return R.fraction.lb_focus_zoom_factor_xsmall;
            case FocusHighlight.ZOOM_FACTOR_MEDIUM:
                return R.fraction.lb_focus_zoom_factor_medium;
            case FocusHighlight.ZOOM_FACTOR_LARGE:
                return R.fraction.lb_focus_zoom_factor_large;
            default:
                return 0;
        }
    }

    static class FocusAnimator implements TimeAnimator.TimeListener {
        private final View mView;
        private final int mDuration;
        private final ShadowOverlayContainer mWrapper;
        private final float mScaleDiff;
        private float mFocusLevel = 0f;
        private float mFocusLevelStart;
        private float mFocusLevelDelta;
        private final TimeAnimator mAnimator = new TimeAnimator();
        private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
        private final ColorOverlayDimmer mDimmer;

        void animateFocus(boolean select, boolean immediate) {
            endAnimation();
            final float end = select ? 1 : 0;
            if (immediate) {
                setFocusLevel(end);
            } else if (mFocusLevel != end) {
                mFocusLevelStart = mFocusLevel;
                mFocusLevelDelta = end - mFocusLevelStart;
                mAnimator.start();
            }
        }

        FocusAnimator(View view, float scale, boolean useDimmer, int duration) {
            mView = view;
            mDuration = duration;
            mScaleDiff = scale - 1f;
            if (view instanceof ShadowOverlayContainer) {
                mWrapper = (ShadowOverlayContainer) view;
            } else {
                mWrapper = null;
            }
            mAnimator.setTimeListener(this);
            if (useDimmer) {
                mDimmer = ColorOverlayDimmer.createDefault(view.getContext());
            } else {
                mDimmer = null;
            }
        }

        void setFocusLevel(float level) {
            mFocusLevel = level;
            float scale = 1f + mScaleDiff * level;
            mView.setScaleX(scale);
            mView.setScaleY(scale);
            if (mWrapper != null) {
                mWrapper.setShadowFocusLevel(level);
            } else {
                ShadowOverlayHelper.setNoneWrapperShadowFocusLevel(mView, level);
            }
            if (mDimmer != null) {
                mDimmer.setActiveLevel(level);
                int color = mDimmer.getPaint().getColor();
                if (mWrapper != null) {
                    mWrapper.setOverlayColor(color);
                } else {
                    ShadowOverlayHelper.setNoneWrapperOverlayColor(mView, color);
                }
            }
        }

        float getFocusLevel() {
            return mFocusLevel;
        }

        void endAnimation() {
            mAnimator.end();
        }

        @Override
        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
            float fraction;
            if (totalTime >= mDuration) {
                fraction = 1;
                mAnimator.end();
            } else {
                fraction = (float) (totalTime / (double) mDuration);
            }
            if (mInterpolator != null) {
                fraction = mInterpolator.getInterpolation(fraction);
            }
            setFocusLevel(mFocusLevelStart + fraction * mFocusLevelDelta);
        }
    }

    public static class BoxFocusHighlight implements FocusHighlightHandler {
        private static final int DURATION_MS = 150;

        private int mScaleIndex;
        private final boolean mUseDimmer;

        public BoxFocusHighlight(int zoomIndex, boolean useDimmer) {
            if (!isValidZoomIndex(zoomIndex)) {
                throw new IllegalArgumentException("Unhandled zoom index");
            }
            mScaleIndex = zoomIndex;
            mUseDimmer = useDimmer;
        }

        private float getScale(Resources res) {
            return mScaleIndex == FocusHighlight.ZOOM_FACTOR_NONE ? 1f :
                    res.getFraction(getResId(mScaleIndex), 1, 1);
        }

        @Override
        public void onItemFocused(View view, boolean hasFocus) {
            view.setSelected(hasFocus);
            getOrCreateAnimator(view).animateFocus(hasFocus, false);
        }

        @Override
        public void onInitializeView(View view) {
            getOrCreateAnimator(view).animateFocus(false, true);
        }

        private FocusAnimator getOrCreateAnimator(View view) {
            FocusAnimator animator = (FocusAnimator) view.getTag(R.id.lb_focus_animator);
            if (animator == null) {
                animator = new FocusAnimator(
                        view, getScale(view.getResources()), mUseDimmer, DURATION_MS);
                view.setTag(R.id.lb_focus_animator, animator);
            }
            return animator;
        }

    }

}
