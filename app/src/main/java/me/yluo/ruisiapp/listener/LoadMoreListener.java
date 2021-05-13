package me.yluo.ruisiapp.listener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LoadMoreListener extends RecyclerView.OnScrollListener {

    private final LinearLayoutManager linearLayoutManager;
    private final OnLoadMoreListener onLoadMoreListener;
    private int limit = 9;

    public LoadMoreListener(@NonNull LinearLayoutManager linearLayoutManager, @NonNull OnLoadMoreListener onLoadMoreListener, int limit) {
        super();
        this.linearLayoutManager = linearLayoutManager;
        this.onLoadMoreListener = onLoadMoreListener;
        this.limit = limit;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        //-1最后  -2 倒数第二
        int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
        if (lastVisiblePosition > limit && lastVisiblePosition == linearLayoutManager.getItemCount() - 1) {
            onLoadMoreListener.onLoadMore();
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

}
