package application.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Forum {

    private String user;
    private String title;
    private String slug;
    private int threads;
    private long posts;

    @JsonCreator
    public Forum(@JsonProperty("slug") String slug,
                 @JsonProperty("title") String title,
                 @JsonProperty("user") String user) {

        this.slug = slug;
        this.title = title;
        this.user = user;
        this.threads = 0;
        this.posts = 0;
    }

    public Forum(String user, String title, String slug, int threads, long posts) {
        this.user = user;
        this.title = title;
        this.slug = slug;
        this.threads = threads;
        this.posts = posts;

    }

    public String getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public int getThreads() {
        return threads;
    }

    public long getPosts() {
        return posts;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

}
