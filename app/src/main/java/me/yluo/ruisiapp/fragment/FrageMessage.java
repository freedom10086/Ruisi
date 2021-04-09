package me.yluo.ruisiapp.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.R;
import me.yluo.ruisiapp.adapter.BaseAdapter;
import me.yluo.ruisiapp.adapter.MessageAdapter;
import me.yluo.ruisiapp.listener.LoadMoreListener;
import me.yluo.ruisiapp.model.ListType;
import me.yluo.ruisiapp.model.MessageData;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.utils.DimenUtils;
import me.yluo.ruisiapp.utils.GetId;
import me.yluo.ruisiapp.utils.UrlUtils;
import me.yluo.ruisiapp.widget.BatchRadioButton;
import me.yluo.ruisiapp.widget.MyListDivider;

/**
 * 消息页面 回复/提到/AT
 *
 * @author yang
 */
public class FrageMessage extends BaseLazyFragment implements LoadMoreListener.OnLoadMoreListener {
    protected RecyclerView messageList;
    protected SwipeRefreshLayout refreshLayout;
    private MessageAdapter adapter;
    private final List<MessageData> datas = new ArrayList<>();
    private int index = 0;
    int lastReplyId = 0, lastAtId = 0;
    int currReplyId = 1, currAtId = 1;
    private boolean lastLoginState = false;
    private boolean enableLoadMore = false;
    private int currentPage = 1, totalPage = 1;
    private boolean haveReply, isHavePm, isHaveAt;
    private BatchRadioButton tab1, tab2, tab3;

    public static FrageMessage newInstance() {
        return new FrageMessage();
    }

    public void updateNotifiCations(boolean haveReply, boolean havePm, boolean haveAt) {
        this.haveReply = haveReply;
        this.isHavePm = havePm;
        this.isHaveAt = haveAt;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        messageList = mRootView.findViewById(R.id.recycler_view);
        tab1 = mRootView.findViewById(R.id.btn_1);
        tab2 = mRootView.findViewById(R.id.btn_2);
        tab3 = mRootView.findViewById(R.id.btn_3);
        //设置可以滑出底栏
        messageList.setClipToPadding(false);
        messageList.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.bottombarHeight));
        refreshLayout = mRootView.findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(R.color.red_light, R.color.green_light, R.color.blue_light, R.color.orange_light);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        messageList.setLayoutManager(layoutManager);
        messageList.addItemDecoration(new MyListDivider(getActivity(), MyListDivider.VERTICAL));
        messageList.addOnScrollListener(new LoadMoreListener(layoutManager, this, 8));
        adapter = new MessageAdapter(getActivity(), datas);
        if (!App.isLogin(getActivity())) {
            adapter.changeLoadMoreState(BaseAdapter.STATE_NEED_LOGIN);
        }
        messageList.setAdapter(adapter);
        refreshLayout.setOnRefreshListener(() -> pullRefresh());
        RadioGroup swictchMes = mRootView.findViewById(R.id.btn_change);

        swictchMes.setOnCheckedChangeListener((radioGroup, id) -> {
            int pos = 2;
            if (id == R.id.btn_1) {
                pos = 0;
            } else if (id == R.id.btn_2) {
                pos = 1;
            }

            if (pos != index) {
                index = pos;
                getData(true);
            }
        });
        return mRootView;
    }

    private void pullRefresh() {
        currentPage = 1;
        getData(true);
    }

    @Override
    public void onFirstUserVisible() {
        updateBatch();
        if (App.isLogin(getContext())) {
            getData(false);
        } else {
            setNeedLoginState();
        }
    }

    @Override
    public void onUserVisible() {
        Log.d("FrageMessage", "last:" + lastLoginState + " now:" + App.isLogin(getActivity()));
        if (lastLoginState != App.isLogin(getActivity())) {
            lastLoginState = !lastLoginState;
            Log.d("FrageMessage", "登录状态改变新状态:" + lastLoginState);
            if (lastLoginState) { //变为登录
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
                getData(true);
            } else {
                setNeedLoginState();
            }
        }
        updateBatch();
    }

    private void updateBatch() {
        tab1.setState(haveReply);
        tab2.setState(isHavePm);
        tab3.setState(isHaveAt);
    }

    @Override
    public void scrollToTop() {
        if (datas.size() > 0) {
            int offset = messageList.computeVerticalScrollOffset();
            if (offset == 0) {
                pullRefresh();
            } if (offset > DimenUtils.getScreenHeight() * 4) {
                messageList.scrollToPosition(0);
            } else {
                messageList.smoothScrollToPosition(0);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_msg_hot;
    }

    private void getData(boolean needRefresh) {
        if (needRefresh) {
            datas.clear();
            adapter.notifyDataSetChanged();
            totalPage = 1;
            currentPage = 1;
            refreshLayout.setRefreshing(true);
        }

        lastReplyId = getContext().getSharedPreferences(App.MY_SHP_NAME, Activity.MODE_PRIVATE)
                .getInt(App.NOTICE_MESSAGE_REPLY_KEY, 0);
        currReplyId = lastReplyId;

        lastAtId = getContext().getSharedPreferences(App.MY_SHP_NAME, Activity.MODE_PRIVATE)
                .getInt(App.NOTICE_MESSAGE_AT_KEY, 0);
        currAtId = lastAtId;

        //reply
        String url;
        if (index == 0) { //reply
            url = "home.php?mod=space&do=notice&mobile=2&page=" + currentPage;
        } else if (index == 1) { //pm
            url = "home.php?mod=space&do=pm&mobile=2&page=" + currentPage;
        } else { //@wo
            url = "home.php?mod=space&do=notice&view=mypost&type=at&mobile=2&page=" + currentPage;
        }

        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                String res = new String(response);
                if (index == 1) {
                    new GetUserPmTask().execute(res);
                } else {
                    new GetMessageTask(index).execute(res);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                e.printStackTrace();
                refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 500);
                adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_FAIL);
            }
        });
    }

    private void finishGetData(List<MessageData> temdatas) {
        if (temdatas.size() == 0) {
            totalPage = currentPage;
        }
        //datas.clear();
        int start = datas.size();
        datas.addAll(temdatas);

        if (currentPage < totalPage) {
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOADING);
        } else {
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
        }

        if (currentPage == 1) {
            adapter.notifyDataSetChanged();
        } else if (temdatas.size() > 0) {
            adapter.notifyItemRangeInserted(start, temdatas.size());
        }
        refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 500);
        enableLoadMore = true;

        boolean haveUnRead = false;
        for (MessageData d : datas) {
            if (!d.isRead()) {
                haveUnRead = true;
                break;
            }
        }

        if (index == 0) {
            haveReply = haveUnRead;
        }
        if (index == 1) {
            isHavePm = haveUnRead;
        }
        if (index == 2) {
            isHaveAt = haveUnRead;
        }
        updateBatch();
    }

    private void setNeedLoginState() {
        datas.clear();
        adapter.changeLoadMoreState(BaseAdapter.STATE_NEED_LOGIN);
        refreshLayout.setRefreshing(false);
        adapter.notifyDataSetChanged();
        totalPage = 1;
        currentPage = 1;
    }

    @Override
    public void onLoadMore() {
        if (enableLoadMore) {
            enableLoadMore = false;
            if (currentPage < totalPage) {
                currentPage++;
                getData(false);
            } else {
                return;
            }
        }
    }

    //获得回复我的
    //获得@我的
    private class GetMessageTask extends AsyncTask<String, Void, List<MessageData>> {

        private final int type; //0 reply 2-@
        private static final int TYPE_REPLY = 0;
        private static final int TYPE_AT = 2;

        public GetMessageTask(int type) {
            this.type = type;
        }

        @Override
        protected List<MessageData> doInBackground(String... params) {
            Document document = Jsoup.parse(params[0]);
            Element d1 = document.select(".pg strong").first();
            if (d1 == null || TextUtils.isEmpty(d1.text())) {
                currentPage = 1;
            } else {
                currentPage = Integer.parseInt(d1.text());
            }

            Element d2 = document.select(".pg label span").first();
            if (d2 == null || TextUtils.isEmpty(d2.text())) {
                totalPage = 1;
            } else {
                totalPage = GetId.getNumber(d2.text());
                if (totalPage == 0) {
                    totalPage = 1;
                }
            }

            List<MessageData> tempdatas = new ArrayList<>();
            Elements lists = document.select(".nts").select("dl.cl");
            for (Element tmp : lists) {
                int noticeId = Integer.parseInt(tmp.attr("notice"));
                String authorImage = UrlUtils.getFullUrl(tmp.select(".avt").select("img").attr("src"));
                String time = tmp.select(".xg1.xw0").text();
                String authorTitle;
                String titleUrl;
                String content;

                if (type == TYPE_REPLY) {
                    content = tmp.select(".ntc_body").select("a[href^=forum.php?mod=redirect]").text().replace("查看", "");
                    if (content.isEmpty()) {
                        //这是系统消息
                        authorTitle = "系统消息";
                        titleUrl = tmp.select(".ntc_body").select("a").attr("href");
                        content = tmp.select(".ntc_body").text();
                    } else {
                        //这是回复消息
                        authorTitle = tmp.select(".ntc_body").select("a[href^=home.php]").text() + " 回复了我";
                        titleUrl = tmp.select(".ntc_body").select("a[href^=forum.php?mod=redirect]").attr("href");
                    }
                } else { //@消息
                    authorTitle = tmp.select(".ntc_body").select("a[href^=home.php]").text() + " 提到了我";
                    titleUrl = tmp.select(".ntc_body").select("a[href^=forum.php?mod=redirect]").attr("href");
                    content = "在主题[" + tmp.select(".ntc_body").select("a[href^=forum.php?mod=redirect]").text() + "]\n" +
                            tmp.select(".ntc_body").select(".quote").text();
                }


                boolean isRead;
                SharedPreferences prf = getActivity().getSharedPreferences(App.MY_SHP_NAME, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = prf.edit();
                if (type == TYPE_REPLY) {
                    isRead = (noticeId <= lastReplyId);
                    if (noticeId > currReplyId) {
                        currReplyId = noticeId;
                    }

                    if (lastReplyId < currReplyId) {
                        editor.putInt(App.NOTICE_MESSAGE_REPLY_KEY, currReplyId);
                        editor.apply();
                    }
                } else {
                    isRead = (noticeId <= lastAtId);
                    if (noticeId > currAtId) {
                        currAtId = noticeId;
                    }

                    if (lastAtId < currAtId) {
                        editor.putInt(App.NOTICE_MESSAGE_AT_KEY, currAtId);
                        editor.apply();
                    }
                }
                tempdatas.add(new MessageData(ListType.REPLAYME, authorTitle, titleUrl, authorImage, time, isRead, content));
            }

            return tempdatas;
        }

        @Override
        protected void onPostExecute(List<MessageData> tempdatas) {
            finishGetData(tempdatas);
        }
    }

    //获得pm消息
    private class GetUserPmTask extends AsyncTask<String, Void, List<MessageData>> {
        @Override
        protected List<MessageData> doInBackground(String... params) {
            Document document = Jsoup.parse(params[0]);
            List<MessageData> temdatas = new ArrayList<>();
            Elements lists = document.select(".pmbox").select("ul").select("li");
            for (Element tmp : lists) {
                boolean isRead = true;
                if (tmp.select(".num").text().length() > 0) {
                    isRead = false;
                }
                String title = tmp.select(".cl").select(".name").text();
                String time = tmp.select(".cl.grey").select(".time").text();
                tmp.select(".cl.grey").select(".time").remove();
                String content = tmp.select(".cl.grey").text();
                String authorImage = UrlUtils.getFullUrl(tmp.select("img").attr("src"));
                String titleUrl = tmp.select("a").attr("href");

                boolean exist = false;
                for (MessageData d : datas) {
                    if (d.getTitleUrl().equals(titleUrl)) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    temdatas.add(new MessageData(ListType.MYMESSAGE, title, titleUrl, authorImage, time, isRead, content));
                }
            }
            return temdatas;
        }

        @Override
        protected void onPostExecute(List<MessageData> tempdatas) {
            if (tempdatas.size() > 0) {
                totalPage = currentPage + 1;
            }
            finishGetData(tempdatas);
        }
    }
}
