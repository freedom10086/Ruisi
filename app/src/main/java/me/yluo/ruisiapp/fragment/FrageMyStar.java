package me.yluo.ruisiapp.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.adapter.BaseAdapter;
import me.yluo.ruisiapp.adapter.MyStarAdapter;
import me.yluo.ruisiapp.listener.ListItemLongClickListener;
import me.yluo.ruisiapp.listener.LoadMoreListener;
import me.yluo.ruisiapp.model.MyStarData;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.widget.MyListDivider;

/**
 * Created by yang on 16-7-14.
 * 收藏
 */
public class FrageMyStar extends BaseFragment implements LoadMoreListener.OnLoadMoreListener, ListItemLongClickListener {

    private List<MyStarData> datas;
    private MyStarAdapter adapter;
    private int currentPage = 1;
    private boolean isEnableLoadMore = true;
    private boolean isHaveMore = true;
    private RecyclerView recyclerView;
    private String url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initToolbar(true, "我的收藏");
        recyclerView = mRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        SwipeRefreshLayout refreshLayout = mRootView.findViewById(R.id.refresh_layout);
        refreshLayout.setEnabled(false);
        String myUid = App.getUid(getActivity());

        //我的收藏
        url = "home.php?mod=space&uid=" + myUid + "&do=favorite&view=me&type=thread&mobile=no";

        datas = new ArrayList<>();
        adapter = new MyStarAdapter(getActivity(), datas);
        adapter.setLongClickListener((v, position) -> {
            int favId = datas.get(position).favId;
            String title = datas.get(position).title;
            new AlertDialog.Builder(getActivity())
                    .setTitle("取消收藏")
                    .setMessage(title)
                    .setPositiveButton("取消收藏", (dialog, which) -> unStarPost(favId, position))
                    .setNegativeButton("关闭", null)
                    .setCancelable(true)
                    .create()
                    .show();
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(new MyListDivider(getActivity(), MyListDivider.VERTICAL));
        recyclerView.addOnScrollListener(new LoadMoreListener((LinearLayoutManager) layoutManager, this, 10));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        refresh();
        return mRootView;
    }

    private void unStarPost(final int favId, int pos) {
        String url = "home.php?mod=spacecp&ac=favorite&op=delete&favid=" + favId + "&type=all&inajax=1&mobile=2";
        HashMap<String, String> pa = new HashMap<>();
        pa.put("deletesubmit", "true");
        //pa.put("formhash")
        pa.put("handlekey", "a_delete_" + favId);
        HttpUtil.post(url, pa, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String s = new String(response);
                if (s.contains("操作成功")) {
                    removeRes(true, pos);
                } else {
                    removeRes(false, pos);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                removeRes(false, pos);
            }
        });
    }

    private void removeRes(boolean b, int pos) {
        if (b) {
            datas.remove(pos);
            adapter.notifyItemRemoved(pos);
            Snackbar.make(recyclerView, "取消收藏成功！", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(recyclerView, "操作失败！", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.list_toolbar;
    }


    @Override
    public void onLoadMore() {
        if (isEnableLoadMore && isHaveMore) {
            currentPage++;
            getWebDatas();
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
            isEnableLoadMore = false;
        }
    }

    private void refresh() {
        datas.clear();
        adapter.notifyDataSetChanged();
        getWebDatas();
    }


    private void getWebDatas() {
        String newurl = url + "&page=" + currentPage;
        HttpUtil.get(newurl, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                new GetUserStarTask().execute(res);
            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_FAIL);
            }
        });
    }

    @Override
    public void onItemLongClick(View v, int position) {

    }


    //获得用户收藏
    private class GetUserStarTask extends AsyncTask<String, Void, List<MyStarData>> {
        @Override
        protected List<MyStarData> doInBackground(String... params) {
            String res = params[0];
            List<MyStarData> temp = new ArrayList<>();
            Elements lists = Jsoup.parse(res).select("#favorite_ul").select("li");
            if (lists.size() == 0) {
                isHaveMore = false;
            } else {
                for (Element tmp : lists) {
                    String title = tmp.select("a[href^=forum.php?mod=viewthread]").text();
                    String link = tmp.select("a[href^=forum.php?mod=viewthread]").attr("href");
                    String time = tmp.select("span.xg1").text();
                    int favId = Integer.parseInt(tmp.select("input").attr("value"));
                    temp.add(new MyStarData(favId, link, title, time));
                }
                if (temp.size() % 10 != 0) {
                    isHaveMore = false;
                }
            }

            return temp;
        }

        @Override
        protected void onPostExecute(List<MyStarData> data) {
            super.onPostExecute(data);
            if (datas.size() == 0 && data.size() == 0) {
                adapter.setPlaceHolderText("你还没有收藏");
            }
            onLoadCompete(data);
        }
    }


    //加载完成
    private void onLoadCompete(List<MyStarData> d) {
        if (isHaveMore && d.size() > 0) {
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
        } else {
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
        }

        if (d.size() > 0) {
            int i = datas.size();
            datas.addAll(d);
            if (i == 0) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(i, d.size());
            }
        } else if (datas.size() == 0) {
            adapter.notifyDataSetChanged();
        }
        isEnableLoadMore = true;
    }
}
