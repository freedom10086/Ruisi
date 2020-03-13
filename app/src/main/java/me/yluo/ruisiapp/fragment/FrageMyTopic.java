package me.yluo.ruisiapp.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.adapter.BaseAdapter;
import me.yluo.ruisiapp.adapter.MyPostsListAdapter;
import me.yluo.ruisiapp.listener.LoadMoreListener;
import me.yluo.ruisiapp.model.ListType;
import me.yluo.ruisiapp.model.SimpleListData;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.widget.MyListDivider;

/**
 * Created by yang on 16-7-14.
 * 我的帖子
 */
public class FrageMyTopic extends BaseFragment implements LoadMoreListener.OnLoadMoreListener {

    private List<SimpleListData> datas;
    private MyPostsListAdapter adapter;
    private int currentPage = 1;
    private boolean isEnableLoadMore = true;
    private boolean isHaveMore = true;
    private String title = "";

    private String url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle bundle = getArguments();//从activity传过来的Bundle
        int uid = 0;
        if (bundle != null) {
            uid = bundle.getInt("uid", 0);
            String username = bundle.getString("username", "我的");
            if (uid == 0) {
                title = "我的帖子";
            } else {
                title = username + "的帖子";
            }
        }
        initToolbar(true, title);
        RecyclerView recyclerView = mRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        SwipeRefreshLayout refreshLayout = mRootView.findViewById(R.id.refresh_layout);
        refreshLayout.setEnabled(false);
        String myUid = App.getUid(getActivity());

        url = "home.php?mod=space&uid=" + (uid > 0 ? uid : myUid) + "&do=thread&view=me&mobile=2";
        datas = new ArrayList<>();
        adapter = new MyPostsListAdapter(getActivity(), datas);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(new MyListDivider(getActivity(), MyListDivider.VERTICAL));
        recyclerView.addOnScrollListener(new LoadMoreListener(layoutManager, this, 10));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        refresh();
        return mRootView;
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
                new GetUserArticles().execute(res);
            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_FAIL);
            }
        });
    }


    //获得主题
    private class GetUserArticles extends AsyncTask<String, Void, List<SimpleListData>> {
        @Override
        protected List<SimpleListData> doInBackground(String... strings) {
            String res = strings[0];
            List<SimpleListData> temp = new ArrayList<>();
            Elements lists = Jsoup.parse(res).select(".threadlist").select("ul").select("li");
            for (Element tmp : lists) {
                String title = tmp.select("a").text();
                if (title.isEmpty()) {
                    isHaveMore = false;
                    break;
                }
                String titleUrl = tmp.select("a").attr("href");
                String num = tmp.select(".num").text();
                temp.add(new SimpleListData(title, num, titleUrl));
            }

            if (temp.size() % 10 != 0) {
                isHaveMore = false;
            }
            return temp;
        }

        @Override
        protected void onPostExecute(List<SimpleListData> aVoid) {
            if (datas.size() == 0 && aVoid.size() == 0) {
                adapter.setPlaceHolderText("你还没有发过帖子");
            }
            onLoadCompete(aVoid);
        }

    }

    //加载完成
    private void onLoadCompete(List<SimpleListData> d) {
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
