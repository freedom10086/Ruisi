package me.yluo.ruisiapp.api.entity;

import java.util.List;

/**
 * @author LuoYang
 */
public class ApiForumIndex {
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
    public String member_email;
    public String member_credits;
    public String setting_bbclosed;
    public Group group;
    
    public List<Catlist> catlist;
    public List<Forumlist> forumlist;
}
