package me.yluo.ruisiapp.model;

/**
 * Created by yang on 16-4-12.
 * 好友列表data
 */
public class FriendData {

    public String userName;
    public String imgUrl;
    public String info;
    public String uid;
    public boolean isOnline;

    public FriendData(String userName, String imgUrl, String info, String uid, boolean isOnline) {
        this.userName = userName;
        this.imgUrl = imgUrl;
        this.info = info;
        this.uid = uid;
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }
}
