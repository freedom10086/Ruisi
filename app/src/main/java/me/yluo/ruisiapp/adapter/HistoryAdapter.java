package me.yluo.ruisiapp.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.activity.PostActivity;
import me.yluo.ruisiapp.listener.ListItemLongClickListener;
import me.yluo.ruisiapp.model.ReadHistoryData;

/**
 * Created by yang on 16-12-10.
 * 浏览历史adapter
 */
public class HistoryAdapter extends BaseAdapter {

    private static final int CONTENT = 0;
    private List<ReadHistoryData> datas;
    private Context context;
    private ListItemLongClickListener longClickListener;

    public HistoryAdapter(Context context, List<ReadHistoryData> datas) {
        this.datas = datas;
        this.context = context;
    }

    public void setLongClickListener(ListItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    protected int getDataCount() {
        return datas.size();
    }

    @Override
    protected int getItemType(int pos) {
        return CONTENT;
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup parent, int viewType) {
        return new HistoryVivwHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false));
    }


    private class HistoryVivwHolder extends BaseViewHolder {
        protected TextView title, author, time;

        HistoryVivwHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.author);
            time = itemView.findViewById(R.id.time);
            itemView.findViewById(R.id.main_item_btn_item).setOnClickListener(v -> itemClick());
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
            title.setText(datas.get(position).title);
            author.setText(datas.get(position).author);
            time.setText(datas.get(position).readTime);
        }

        void itemClick() {
            String tid = datas.get(getAdapterPosition()).tid;
            if (!TextUtils.isEmpty(tid)) {
                PostActivity.open(context, "tid=" + tid, datas.get(getAdapterPosition()).author);
            }
        }
    }
}
