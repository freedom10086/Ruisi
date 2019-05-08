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
import me.yluo.ruisiapp.adapter.SimpleListAdapter;
import me.yluo.ruisiapp.listener.LoadMoreListener;
import me.yluo.ruisiapp.model.FrageType;
import me.yluo.ruisiapp.model.ListType;
import me.yluo.ruisiapp.model.SimpleListData;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.widget.MyListDivider;

/**
 * Created by free2 on 16-7-14.
 * 收藏/主题/历史纪录
 * //todo 删除浏览历史
 */
public class FrageTopicStar extends BaseFragment implements LoadMoreListener.OnLoadMoreListener {

    private List<SimpleListData> datas;
    private SimpleListAdapter adapter;
    private int CurrentPage = 1;
    private boolean isEnableLoadMore = true;
    private boolean isHaveMore = true;
    private int currentIndex = 0;
    private String title = "";

    private String url;

    public static FrageTopicStar newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt("type", type);
        FrageTopicStar fragment = new FrageTopicStar();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Bundle bundle = getArguments();//从activity传过来的Bundle
        int uid = 0;
        if (bundle != null) {
            int type = bundle.getInt("type", -1);
            uid = bundle.getInt("uid", 0);
            String username = bundle.getString("username", "我的");
            switch (type) {
                case FrageType.TOPIC:
                    currentIndex = 0;
                    if (uid == 0) {
                        title = "我的帖子";
                    } else {
                        title = username + "的帖子";
                    }
                    break;
                case FrageType.START:
                    currentIndex = 1;
                    title = "我的收藏";
                    break;
            }
        }
        initToolbar(true, title);
        RecyclerView recyclerView = mRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        SwipeRefreshLayout refreshLayout = mRootView.findViewById(R.id.refresh_layout);
        refreshLayout.setEnabled(false);
        String myUid = App.getUid(getActivity());
        switch (currentIndex) {
            case 0:
                //主题
                url = "home.php?mod=space&uid=" + (uid > 0 ? uid : myUid) + "&do=thread&view=me&mobile=2";
                break;
            case 1:
                //我的收藏
                url = "home.php?mod=space&uid=" + myUid + "&do=favorite&view=me&type=thread&mobile=2";
                break;
        }

        datas = new ArrayList<>();
        adapter = new SimpleListAdapter(ListType.ARTICLE, getActivity(), datas);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(new MyListDivider(getActivity(), MyListDivider.VERTICAL));
        recyclerView.addOnScrollListener(new LoadMoreListener((LinearLayoutManager) layoutManager, this, 10));
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
            CurrentPage++;
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
        String newurl = url + "&page=" + CurrentPage;
        HttpUtil.get(newurl, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                if (currentIndex == 0) {
                    new GetUserArticles().execute(res);
                } else if (currentIndex == 1) {
                    new GetUserStarTask().execute(res);
                }
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

    //获得用户收藏
    private class GetUserStarTask extends AsyncTask<String, Void, List<SimpleListData>> {
        @Override
        protected List<SimpleListData> doInBackground(String... params) {
            String res = params[0];
            List<SimpleListData> temp = new ArrayList<>();
            Elements lists = Jsoup.parse(res).select(".threadlist").select("ul").select("li");
            for (Element tmp : lists) {
                String key = tmp.select("a").text();
                if (key.isEmpty()) {
                    isHaveMore = false;
                    break;
                }
                String link = tmp.select("a").attr("href");
                temp.add(new SimpleListData(key, "", link));
            }
            if (temp.size() % 10 != 0) {
                isHaveMore = false;
            }
            return temp;
        }

        @Override
        protected void onPostExecute(List<SimpleListData> data) {
            super.onPostExecute(data);
            if (datas.size() == 0 && data.size() == 0) {
                adapter.setPlaceHolderText("你还没有收藏");
            }
            onLoadCompete(data);
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
