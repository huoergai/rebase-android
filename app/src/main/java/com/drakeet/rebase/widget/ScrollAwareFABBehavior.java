/*
 * Copyright (C) 2017 Drakeet <drakeet.me@gmail.com>
 *
 * This file is part of rebase-android
 *
 * rebase-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * rebase-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rebase-android. If not, see <http://www.gnu.org/licenses/>.
 */

package com.drakeet.rebase.widget;

import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

@Keep
public class ScrollAwareFabBehavior extends FloatingActionButton.Behavior {

    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();
    private boolean isAnimatingOut = false;
    private ShowInInterceptor showInInterceptor;


    public interface ShowInInterceptor {
        boolean onFabShowIn(@NonNull FloatingActionButton fab);
    }


    public ScrollAwareFabBehavior(Context context, AttributeSet attrs) {
        super();
    }


    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
            super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
    }


    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target,
                               int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
        if (dyConsumed > 0 && !this.isAnimatingOut && child.getVisibility() == View.VISIBLE) {
            // User scrolled down and the FAB is currently visible -> hide the FAB
            animateOut(child, new ViewPropertyAnimatorListener() {
                public void onAnimationStart(View view) {
                    ScrollAwareFabBehavior.this.isAnimatingOut = true;
                }


                public void onAnimationCancel(View view) {
                    ScrollAwareFabBehavior.this.isAnimatingOut = false;
                }


                public void onAnimationEnd(View view) {
                    ScrollAwareFabBehavior.this.isAnimatingOut = false;
                    view.setVisibility(View.INVISIBLE);
                }
            });
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            // User scrolled up and the FAB is currently not visible -> show the FAB
            if (showInInterceptor == null) {
                animateIn(child);
            } else {
                if (!showInInterceptor.onFabShowIn(child)) {
                    animateIn(child);
                }
            }
        }
    }


    public void animateOut(@NonNull FloatingActionButton fab) {
        animateOut(fab, new ViewPropertyAnimatorListener() {
            public void onAnimationStart(View view) {
                ScrollAwareFabBehavior.this.isAnimatingOut = true;
            }


            public void onAnimationCancel(View view) {
                ScrollAwareFabBehavior.this.isAnimatingOut = false;
            }


            public void onAnimationEnd(View view) {
                ScrollAwareFabBehavior.this.isAnimatingOut = false;
                view.setVisibility(View.INVISIBLE);
            }
        });
    }


    public static class DefaultViewPropertyAnimatorListener implements ViewPropertyAnimatorListener {

        public void onAnimationEnd(View view) {
            view.setVisibility(View.INVISIBLE);
        }


        @Override
        public void onAnimationStart(View view) {}


        @Override
        public void onAnimationCancel(View view) {}
    }


    ;


    public static void animateOut(@NonNull FloatingActionButton button, @Nullable ViewPropertyAnimatorListener listener) {
        if (listener == null) {
            listener = new DefaultViewPropertyAnimatorListener();
        }
        ViewCompat.animate(button)
            .scaleX(0.0F)
            .scaleY(0.0F)
            .alpha(0.0F)
            .setInterpolator(INTERPOLATOR)
            .withLayer()
            .setListener(listener)
            .start();
    }


    public static void animateIn(FloatingActionButton button) {
        button.setVisibility(View.VISIBLE);
        ViewCompat.animate(button)
            .scaleX(1.0F)
            .scaleY(1.0F)
            .alpha(1.0F)
            .setInterpolator(INTERPOLATOR)
            .withLayer()
            .setListener(null)
            .start();
    }


    public void setShowInInterceptor(@NonNull ShowInInterceptor showInInterceptor) {
        this.showInInterceptor = showInInterceptor;
    }
}