package me.yluo.ruisiapp.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.listener.ListItemClickListener;
import me.yluo.ruisiapp.model.BlogData;

/**
 * @author yang
 */
public class MyBlogListAdapter extends BaseAdapter {

    private List<BlogData> datas = new ArrayList<>();
    private final Activity activity;
    private ListItemClickListener clickListener;

    public MyBlogListAdapter(Activity activity, List<BlogData> datas) {
        this.datas = datas;
        this.activity = activity;
    }

    public void setClickListener(ListItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    protected int getDataCount() {
        return datas.size();
    }

    @Override
    protected int getItemType(int pos) {
        return 0;
    }


    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        return new MyPostsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_blog_list, parent, false));
    }


    private class MyPostsViewHolder extends BaseViewHolder {
        protected TextView title, content, postTime, viewCount, replyCount;

        MyPostsViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            postTime = itemView.findViewById(R.id.post_time);
            viewCount = itemView.findViewById(R.id.view_count);
            replyCount = itemView.findViewById(R.id.reply_count);


            itemView.findViewById(R.id.item_root).setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onListItemClick(v, getAdapterPosition());
                } else {
                    itemClick();
                }
            });
        }

        @Override
        void setData(int position) {
            BlogData data = datas.get(position);
            title.setText(data.getTitle());
            content.setText(data.getContent());
            postTime.setText("\uf017 " + data.getPostTime());
            viewCount.setText("\uf06e " + data.getViewCount());
            replyCount.setText("\uf0e6 " + data.getReplyCount());
        }

        void itemClick() {
            BlogData d = datas.get(getAdapterPosition());
            Integer blogId = d.getId();
            //BlogActivity.open(activity, d);
        }
    }
}
