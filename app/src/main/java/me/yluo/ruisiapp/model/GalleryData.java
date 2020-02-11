package me.yluo.ruisiapp.model;

import me.yluo.ruisiapp.App;

/**
 * Created by yang on 16-6-22.
 * gallery data
 */
public class GalleryData {
    private String imgurl;
    private String title;
    private String titleUrl;

    public GalleryData(String imgurl, String title, String titleUrl) {
        if (imgurl.startsWith("./")) {
            imgurl = App.getBaseUrl() + imgurl.substring(2);
        }
        this.imgurl = imgurl;
        this.title = title;
        this.titleUrl = titleUrl;
    }

    public String getImgurl() {
        return imgurl;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleUrl() {
        return titleUrl;
    }
}
