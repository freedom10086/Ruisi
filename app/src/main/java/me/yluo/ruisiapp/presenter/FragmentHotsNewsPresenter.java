package me.yluo.ruisiapp.presenter;

import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.yluo.ruisiapp.App;
import me.yluo.ruisiapp.adapter.PostListAdapter;
import me.yluo.ruisiapp.database.MyDB;
import me.yluo.ruisiapp.fragment.FrageHotsNews;
import me.yluo.ruisiapp.interfaces.IFragHotsNewsInterface;
import me.yluo.ruisiapp.model.ArticleListData;
import me.yluo.ruisiapp.myhttp.HttpUtil;
import me.yluo.ruisiapp.myhttp.ResponseHandler;
import me.yluo.ruisiapp.utils.GetId;

public class FragmentHotsNewsPresenter {
    private WeakReference<IFragHotsNewsInterface> fragHotsNewsInterfaceWeakReference;

    public FragmentHotsNewsPresenter(IFragHotsNewsInterface interfaces) {
        fragHotsNewsInterfaceWeakReference = new WeakReference<>(interfaces);
    }

    public void getData(String url) {
        HttpUtil.get(url, new ResponseHandler() {
            @Override
            public void onSuccess(byte[] response) {
                new GetNewArticleListTaskMe().execute(new String(response));
            }

            @Override
            public void onFailure(Throwable e) {
                IFragHotsNewsInterface newsInterface = fragHotsNewsInterfaceWeakReference.get();
                if (newsInterface != null) {
                    newsInterface.onRequestDataFailed();
                }
            }
        });
    }

    private class GetNewArticleListTaskMe extends AsyncTask<String, Void, List<ArticleListData>> {
        @Override
        protected List<ArticleListData> doInBackground(String... params) {
            List<ArticleListData> dataset = new ArrayList<>();
            Document doc = Jsoup.parse(params[0]);
            Elements body = doc.select("div[class=threadlist]"); // 具有 href 属性的链接
            Elements links = body.select("li");
            IFragHotsNewsInterface newsInterface = fragHotsNewsInterfaceWeakReference.get();
            Context context;
            if (newsInterface != null && newsInterface instanceof FrageHotsNews) {
                context = ((FrageHotsNews) newsInterface).getActivity();
            } else {
                context = App.context;
            }
            for (Element src : links) {
                String url = src.select("a").attr("href");
                int titleColor = GetId.getColor(context, src.select("a").attr("style"));
                String author = src.select(".by").text();
                src.select("span.by").remove();
                String replyCount = src.select("span.num").text();
                src.select("span.num").remove();
                String title = src.select("a").text();
                String img = src.select("img").attr("src");
                PostListAdapter.MobilePostType postType = PostListAdapter.MobilePostType.parse(img);
                dataset.add(new ArticleListData(postType, title, url, author, replyCount, titleColor));
            }

            MyDB myDB = new MyDB(context);
            return myDB.handReadHistoryList(dataset);
        }

        @Override
        protected void onPostExecute(List<ArticleListData> datas) {
            IFragHotsNewsInterface newsInterface = fragHotsNewsInterfaceWeakReference.get();
            if (newsInterface != null) {
                newsInterface.onNewArticleListDataReceived(datas);
            }
        }
    }
}
