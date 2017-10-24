package application.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Date;

public class Thread {

    private String author;
    private String forum;
    private Date created;
    private String message;
    private String slug;
    private String title;
    private int votes;
    private int id;


    @JsonCreator
    public Thread(@JsonProperty("slug") String slug,
                  @JsonProperty("author") String author,
                  @JsonProperty("message") String message,
                  @JsonProperty("title") String title) {

        this.slug = slug;
        this.author = author;
        this.message = message;
        this.title = title;
        this.created = new Date(System.currentTimeMillis());

    }

    public int getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getForum() {
        return forum;
    }

    public String getCreated() {
        return created.toInstant().toString();
    }

    public String getMessage() {
        return message;
    }

    public String getSlug() {
        return slug;
    }

    public int getVotes() {
        return votes;
    }

    public String getTitle() {
        return title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

}
