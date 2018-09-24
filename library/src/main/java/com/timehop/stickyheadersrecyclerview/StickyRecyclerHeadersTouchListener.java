package com.timehop.stickyheadersrecyclerview;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;

public class StickyRecyclerHeadersTouchListener implements RecyclerView.OnItemTouchListener {
  private final GestureDetector mTapDetector;
  private final RecyclerView mRecyclerView;
  private final StickyRecyclerHeadersDecoration mDecor;
  private OnHeaderClickListener mOnHeaderClickListener;

  public interface OnHeaderClickListener {
    void onHeaderClick(View header, int position, long headerId);
  }

  public StickyRecyclerHeadersTouchListener(final RecyclerView recyclerView,
                                            final StickyRecyclerHeadersDecoration decor) {
    mTapDetector = new GestureDetector(recyclerView.getContext(), new SingleTapDetector());
    mRecyclerView = recyclerView;
    mDecor = decor;
  }

  public StickyRecyclerHeadersAdapter getAdapter() {
    if (mRecyclerView.getAdapter() instanceof StickyRecyclerHeadersAdapter) {
      return (StickyRecyclerHeadersAdapter) mRecyclerView.getAdapter();
    } else {
      throw new IllegalStateException("A RecyclerView with " +
          StickyRecyclerHeadersTouchListener.class.getSimpleName() +
          " requires a " + StickyRecyclerHeadersAdapter.class.getSimpleName());
    }
  }


  public void setOnHeaderClickListener(OnHeaderClickListener listener) {
    mOnHeaderClickListener = listener;
  }

  @Override
  public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
    if (this.mOnHeaderClickListener != null) {
      boolean tapDetectorResponse = this.mTapDetector.onTouchEvent(e);
      if (tapDetectorResponse) {
        // Don't return false if a single tap is detected
        return true;
      }
      if (e.getAction() == MotionEvent.ACTION_DOWN) {
        int position = mDecor.findHeaderPositionUnder((int)e.getX(), (int)e.getY());
        return position != -1;
      }
    }
    return false;
  }

  @Override
  public void onTouchEvent(RecyclerView view, MotionEvent e) { /* do nothing? */ }

  @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    // do nothing
  }

  private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener {

    int clickX = 0;
    int clickY = 0;

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      clickX = (int) e.getX();
      clickY = (int) e.getY();
      int position = mDecor.findHeaderPositionUnder(clickX, clickY);
      if (position != -1) {
        View headerView = mDecor.getHeaderView(mRecyclerView, position);
        long headerId = getAdapter().getHeaderId(position);
        mOnHeaderClickListener.onHeaderClick(headerView, position, headerId);
        mRecyclerView.playSoundEffect(SoundEffectConstants.CLICK);
        click(headerView, mDecor.getHeaderRectAtPosition(position));
        return true;
      }
      return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      return true;
    }

    private boolean click(View view, Rect rect) {
      if (view instanceof ViewGroup) {
        ViewGroup viewGroup = (ViewGroup) view;
          for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            Rect r = new Rect();
            child.getHitRect(r);
            r.offset(rect.left, rect.top);
            if (click(child, r)) {
              return true;
            }
          }
        return false;
      } else {
        if (view.isClickable() && view.isEnabled()) {
          if (rect.contains(clickX, clickY)) {
            return view.performClick();
          }
        }
        return false;
      }
    }

  }
}
