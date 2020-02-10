package me.yluo.ruisiapp.model;

import java.util.List;

/**
 * Created by yangluo on 2017/3/23.
 * 一个板块
 */

public class Category {
    public String name;
    public boolean login; // 是否需要登录才能看到
    public int gid;
    public boolean canPost; // 是否可以发帖
    public List<Forum> forums;

    public Category() {
    }

    public Category(String name, int gid, boolean login, boolean canPost, List<Forum> forums) {
        this.name = name;
        this.login = login;
        this.forums = forums;
        this.gid = gid;
        this.canPost = canPost;
    }
}
