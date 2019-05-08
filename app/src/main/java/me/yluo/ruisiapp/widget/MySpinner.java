package me.yluo.ruisiapp.widget;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.List;

import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.utils.DimenUtils;


public class MySpinner<T> extends PopupWindow implements AdapterView.OnItemClickListener {

    private Context mContext;
    private ListView listView;
    private OnItemSelectListener listener;
    private MySpinnerListAdapter<T> adapter;

    private int currentSelect = -1;

    public MySpinner(Context context) {
        super(context);
        mContext = context;
        init();
    }


    private void init() {
        listView = new ListView(mContext);
        listView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setOnItemClickListener(this);

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.my_spinner_bg));
        setElevation(DimenUtils.dip2px(mContext, 4));

        setFocusable(true);
        setContentView(listView);
    }

    public void setData(List<T> datas) {
        adapter = new MySpinnerListAdapter<>(mContext, datas);
        listView.setAdapter(adapter);
    }

    public T getSelectItem() {
        if (currentSelect > adapter.getCount() - 1 || currentSelect < 0) {
            return null;
        }
        return adapter.getItem(currentSelect);
    }

    public void setListener(OnItemSelectListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
        dismiss();
        if (listener != null && currentSelect != pos) {
            currentSelect = pos;
            listener.onItemSelectChanged(pos, view);
        }

    }

    public interface OnItemSelectListener {
        void onItemSelectChanged(int pos, View v);
    }

    private class MySpinnerListAdapter<T> extends BaseAdapter {

        private List<T> datas;
        private Context context;


        MySpinnerListAdapter(Context context, List<T> datas) {
            this.datas = datas;
            this.context = context;
        }


        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public T getItem(int i) {
            return datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            TextView v = new TextView(context);
            v.setTag(i);
            v.setTextColor(ContextCompat.getColor(context, R.color.text_color_pri));
            v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            v.setText(datas.get(i).toString());
            v.setGravity(Gravity.CENTER);
            v.setLayoutParams(new AbsListView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            int hPadding = DimenUtils.dip2px(context, 8);
            int vPadding = DimenUtils.dip2px(context, 8);

            v.setPadding(hPadding, vPadding, hPadding, vPadding);
            return v;
        }
    }
}