package me.yluo.ruisiapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import me.yluo.ruisiapp.utils.UrlUtils;

/**
 * Created by yang on 16-3-11.
 * 单篇文章数据包括评论
 */
public class SingleArticleData implements Parcelable {

    //用来标识是楼主还是内容还是loadmore
    public SingleType type;
    public String username;
    public String postTime;
    public int uid;
    public String pid;
    //楼层
    public String index;
    //回复链接
    public String replyUrl;
    public String content;
    public String textContent;
    public String title;

    //能否管理
    public boolean canManage;

    //投票
    public VoteData vote;

    // 当前页数
    public int page;


    public SingleArticleData(SingleType type, String title, int uid,
                             String username, String postTime, String index,
                             String replyUrl, String content, String pid,
                             int page) {
        this.type = type;
        this.username = username;
        this.postTime = postTime;
        this.index = index;
        this.replyUrl = replyUrl;
        this.content = content;
        this.title = title;
        this.pid = pid;
        this.uid = uid;
        this.page = page;
    }

    public SingleArticleData(SingleType type, String title, int uid, String username,
                             String postTime, String index, String replyUrl,
                             String content, String pid, int page, boolean canManage) {
        this(type, title, uid, username, postTime, index, replyUrl, content, pid, page);
        //TODO 存储数据canManage以及管理功能
        this.canManage = canManage;
    }

    public String getImg() {
        return UrlUtils.getAvaterurlm(String.valueOf(uid));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.username);
        dest.writeString(this.postTime);
        dest.writeInt(this.uid);
        dest.writeString(this.pid);
        dest.writeString(this.index);
        dest.writeString(this.replyUrl);
        dest.writeString(this.content);
        dest.writeString(this.title);
    }

    protected SingleArticleData(Parcel in) {
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : SingleType.values()[tmpType];
        this.username = in.readString();
        this.postTime = in.readString();
        this.uid = in.readInt();
        this.pid = in.readString();
        this.index = in.readString();
        this.replyUrl = in.readString();
        this.content = in.readString();
        this.title = in.readString();
    }

    public static final Parcelable.Creator<SingleArticleData> CREATOR = new Parcelable.Creator<SingleArticleData>() {
        @Override
        public SingleArticleData createFromParcel(Parcel source) {
            return new SingleArticleData(source);
        }

        @Override
        public SingleArticleData[] newArray(int size) {
            return new SingleArticleData[size];
        }
    };

}
