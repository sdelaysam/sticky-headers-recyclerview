package com.timehop.stickyheadersrecyclerview;

import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StickyRecyclerHeadersTouchListener implements RecyclerView.OnItemTouchListener {
  private final GestureDetector mTapDetector;
  private final RecyclerView mRecyclerView;
  private final StickyRecyclerHeadersDecoration mDecor;
  private OnHeaderClickListener mOnHeaderClickListener;
  private OnHeaderClickListener mOnHeaderViewClickListener;
  private int mHeaderViewId = 0;

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

  public void setOnHeaderViewClickListener(@IdRes int viewId, OnHeaderClickListener listener) {
    mOnHeaderViewClickListener = listener;
    mHeaderViewId = viewId;
  }

  @Override
  public boolean onInterceptTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) {
    if (this.mOnHeaderClickListener != null || this.mOnHeaderViewClickListener != null) {
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
  public void onTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) {
    this.mTapDetector.onTouchEvent(e);
  }

  @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    // do nothing
  }

  private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      int clickX = (int) e.getX();
      int clickY = (int) e.getY();
      int position = mDecor.findHeaderPositionUnder(clickX, clickY);
      if (position != -1) {
        View headerView = mDecor.getHeaderView(mRecyclerView, position);
        long headerId = getAdapter().getHeaderId(position);
        if (mOnHeaderClickListener != null) {
          mOnHeaderClickListener.onHeaderClick(headerView, position, headerId);
          mRecyclerView.playSoundEffect(SoundEffectConstants.CLICK);
        }
        if (mOnHeaderViewClickListener != null && mHeaderViewId > 0) {
          if (headerView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) headerView;
            View clickView = viewGroup.findViewById(mHeaderViewId);
            if (clickView != null) {
              Rect r = new Rect();
              clickView.getDrawingRect(r);
              viewGroup.offsetDescendantRectToMyCoords(clickView, r);
              Rect headerRect = mDecor.getHeaderRectAtPosition(position);
              r.offset(headerRect.left, headerRect.top);
              if (r.contains(clickX, clickY)) {
                mOnHeaderViewClickListener.onHeaderClick(headerView, position, headerId);
              }
            }
          }
        }
        return true;
      }
      return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      return true;
    }
  }
}
