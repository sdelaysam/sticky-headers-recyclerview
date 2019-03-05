package com.timehop.stickyheadersrecyclerview.caching;

import android.view.View;
import android.view.ViewGroup;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.timehop.stickyheadersrecyclerview.util.OrientationProvider;

import androidx.collection.LongSparseArray;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * An implementation of {@link HeaderProvider} that creates and caches header views
 */
public class HeaderViewCache implements HeaderProvider {

  private final StickyRecyclerHeadersAdapter mAdapter;
  private final LongSparseArray<RecyclerView.ViewHolder> mHeaderViews = new LongSparseArray<>();
  private final OrientationProvider mOrientationProvider;

  public HeaderViewCache(StickyRecyclerHeadersAdapter adapter,
      OrientationProvider orientationProvider) {
    mAdapter = adapter;
    mOrientationProvider = orientationProvider;
  }

  @Override
  public RecyclerView.ViewHolder getHeaderViewHolder(RecyclerView parent, int position) {
    long headerId = mAdapter.getHeaderId(position);

    RecyclerView.ViewHolder header = mHeaderViews.get(headerId);
    if (header == null) {
      //TODO - recycle views
      header = mAdapter.onCreateHeaderViewHolder(parent);
      mAdapter.onBindHeaderViewHolder(header, position);

      View view = header.itemView;
      if (view.getLayoutParams() == null) {
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      }

      int widthSpec;
      int heightSpec;

      if (mOrientationProvider.getOrientation(parent) == LinearLayoutManager.VERTICAL) {
        widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.EXACTLY);
        heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.UNSPECIFIED);
      } else {
        widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getWidth(), View.MeasureSpec.UNSPECIFIED);
        heightSpec = View.MeasureSpec.makeMeasureSpec(parent.getHeight(), View.MeasureSpec.EXACTLY);
      }

      int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
              parent.getPaddingLeft() + parent.getPaddingRight(), view.getLayoutParams().width);
      int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
              parent.getPaddingTop() + parent.getPaddingBottom(), view.getLayoutParams().height);
      view.measure(childWidth, childHeight);
      view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
      mHeaderViews.put(headerId, header);
    }
    return header;
  }

  @Override
  public View getHeader(RecyclerView parent, int position) {
    return getHeaderViewHolder(parent, position).itemView;
  }

  @Override
  public void invalidate() {
    mHeaderViews.clear();
  }
}
