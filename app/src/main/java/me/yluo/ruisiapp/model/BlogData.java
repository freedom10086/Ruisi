package me.yluo.ruisiapp.model;

public class BlogData {

    private Integer id;

    private String title;

    private String content;

    private String author;

    private String postTime;

    private String viewCount;

    private String replyCount;

    public BlogData(Integer id, String title, String content, String author, String postTime, String viewCount, String replyCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.postTime = postTime;
        this.viewCount = viewCount;
        this.replyCount = replyCount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(String replyCount) {
        this.replyCount = replyCount;
    }
}
