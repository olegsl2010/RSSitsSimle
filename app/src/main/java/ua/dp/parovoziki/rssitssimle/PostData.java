package ua.dp.parovoziki.rssitssimle;

public class PostData {

    private String postThumbUrl;

    private String postTitle;

    private String postDate;

    public void setPostThumbUrl(String postThumbUrl) {
        this.postThumbUrl = postThumbUrl;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getPostThumbUrl() {

        return postThumbUrl;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public String getPostDate() {
        return postDate;
    }
}
