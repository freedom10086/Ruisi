package me.yluo.ruisiapp.api.entity;

import java.util.List;

/**
 * @author yang
 */
public class ApiForumList {
    public String cookiepre;
    public String auth;
    public String saltkey;
    public String member_uid;
    public String member_username;
    public String groupid;
    public String formhash;
    public String ismoderator;
    public String readaccess;
    public Notice notice;

    public Forum forum;
    public Group group;

    public List<ForumThreadlist> forum_threadlist;
    public List<String> sublist;
    public int tpp;
    public int page;
}
