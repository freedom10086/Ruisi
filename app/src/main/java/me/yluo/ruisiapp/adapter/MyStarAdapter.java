package me.yluo.ruisiapp.adapter;

import android.app.Activity;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.PostActivity;
import me.yluo.ruisiapp.listener.ListItemClickListener;
import me.yluo.ruisiapp.listener.ListItemLongClickListener;
import me.yluo.ruisiapp.model.MyStarData;

/**
 * Created by yang on 20-2-12.
 * 我的收藏
 */
public class MyStarAdapter extends BaseAdapter {

    private static final int CONTENT = 0;
    private List<MyStarData> data = new ArrayList<>();
    private final Activity activity;
    private ListItemClickListener clickListener;
    private ListItemLongClickListener longClickListener;

    public MyStarAdapter(Activity activity, List<MyStarData> datas) {
        data = datas;
        this.activity = activity;
    }

    public void setClickListener(ListItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLongClickListener(ListItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    protected int getDataCount() {
        return data.size();
    }

    @Override
    protected int getItemType(int pos) {
        return CONTENT;
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_star_list, parent, false));
    }

    private class SimpleViewHolder extends BaseViewHolder {
        protected TextView title;
        protected TextView time;

        SimpleViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            time = itemView.findViewById(R.id.time);
            itemView.findViewById(R.id.main_item_btn_item).setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onListItemClick(v, getAdapterPosition());
                } else {
                    itemClick();
                }
            });
            itemView.findViewById(R.id.main_item_btn_item).setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(v, getAdapterPosition());
                    return true;
                }
                return false;
            });
        }

        @Override
        void setData(int position) {
            String title = data.get(position).title;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.title.setText(Html.fromHtml(title, 0));
            } else {
                this.title.setText(Html.fromHtml(title));
            }
            String time = data.get(position).time;
            this.time.setText(time);
        }

        void itemClick() {
            MyStarData d = data.get(getAdapterPosition());
            String url = d.url;
            if (url != null && url.length() > 0) {
                PostActivity.open(activity, url, App.getName(activity));
            }
        }
    }
}
