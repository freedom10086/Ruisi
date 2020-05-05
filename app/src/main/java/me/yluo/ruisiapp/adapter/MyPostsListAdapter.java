package me.yluo.ruisiapp.adapter;

import android.app.Activity;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.PostActivity;
import me.yluo.ruisiapp.listener.ListItemClickListener;
import me.yluo.ruisiapp.model.SimpleListData;

/**
 * @author yang
 */
public class MyPostsListAdapter extends BaseAdapter {

    private List<SimpleListData> data = new ArrayList<>();
    private Activity activity;
    private ListItemClickListener clickListener;

    public MyPostsListAdapter(Activity activity, List<SimpleListData> datas) {
        data = datas;
        this.activity = activity;
    }

    public void setClickListener(ListItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    protected int getDataCount() {
        return data.size();
    }

    @Override
    protected int getItemType(int pos) {
        return 0;
    }


    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        return new MyPostsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_posts_list, parent, false));
    }


    private class MyPostsViewHolder extends BaseViewHolder {
        protected TextView key;
        protected TextView value;

        MyPostsViewHolder(View itemView) {
            super(itemView);
            key = itemView.findViewById(R.id.title);
            value = itemView.findViewById(R.id.reply_count);
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
            String keystr = data.get(position).getKey();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                key.setText(Html.fromHtml(keystr, 0));
            } else {
                key.setText(Html.fromHtml(keystr));
            }
            String values = data.get(position).getValue();
            if (!TextUtils.isEmpty(values)) {
                value.setVisibility(View.VISIBLE);
                value.setText("\uf0e6 " + values);
            } else {
                value.setVisibility(View.GONE);
            }
        }

        void itemClick() {
            SimpleListData d = data.get(getAdapterPosition());
            String url = d.getExtradata();
            if (url != null && url.length() > 0) {
                PostActivity.open(activity, url, d.getValue());
            }
        }
    }
}
