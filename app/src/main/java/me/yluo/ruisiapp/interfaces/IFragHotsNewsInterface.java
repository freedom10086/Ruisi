package me.yluo.ruisiapp.interfaces;


import java.util.List;

import me.yluo.ruisiapp.model.ArticleListData;

public interface IFragHotsNewsInterface {

    void onRequestDataFailed();

    void onNewArticleListDataReceived(List<ArticleListData> data);
}