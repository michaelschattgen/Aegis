package com.beemdevelopment.aegis.helpers;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SimpleSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private final RecyclerView recyclerView;

    public SimpleSwipeTouchListener(Context context, RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean result = gestureDetector.onTouchEvent(event);
        if (result) {
            recyclerView.requestDisallowInterceptTouchEvent(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            recyclerView.requestDisallowInterceptTouchEvent(false);
        }
        return result;
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 25;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    return true;
                }
            } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                recyclerView.requestDisallowInterceptTouchEvent(false);
                return false;
            }
            return false;
        }
    }

    public void onSwipeRight() {

    }

    public void onSwipeLeft() {

    }
}