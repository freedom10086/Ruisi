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
import me.yluo.ruisiapp.model.ListType;
import me.yluo.ruisiapp.model.SimpleListData;

/**
 * Created by yang on 16-4-7.
 * 简单的adapter 比如用户信息 搜索结果
 * 等都用这个
 */
public class SimpleListAdapter extends BaseAdapter {

    private static final int CONTENT = 0;
    private List<SimpleListData> data = new ArrayList<>();
    private final Activity activity;
    private final ListType type;
    private ListItemClickListener clickListener;

    public SimpleListAdapter(ListType type, Activity activity, List<SimpleListData> datas) {
        data = datas;
        this.activity = activity;
        this.type = type;
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
        return CONTENT;
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        return new SimpleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sim_list, parent, false));
    }


    private class SimpleViewHolder extends BaseViewHolder {
        protected TextView key;
        protected TextView value;

        SimpleViewHolder(View itemView) {
            super(itemView);
            key = itemView.findViewById(R.id.key);
            value = itemView.findViewById(R.id.value);
            itemView.findViewById(R.id.main_item_btn_item).setOnClickListener(v -> {
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
                value.setText(values);
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
