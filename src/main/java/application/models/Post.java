package application.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Post {

    private int id;
    private String forum;
    private String author;
    private int thread;
    private String created;
    private boolean isEdited;
    private String message;
    private long parent;

    @JsonCreator
    public Post(@JsonProperty("author") String author,
                @JsonProperty("message") String message,
                @JsonProperty("parent") long parent) {

        this.author = author;
        this.message = message;
        this.parent = parent;
        this.isEdited = false;
        this.id = 0;
    }

    public Post(int id, String forum, String author, int thread, String created, boolean isEdited, String message, long parent) {
        this.id = id;
        this.forum = forum;
        this.author = author;
        this.thread = thread;
        this.created = created;
        this.isEdited = isEdited;
        this.message = message;
        this.parent = parent;
    }

    public long getId() {
        return id;
    }

    public String getForum() {
        return forum;
    }

    public String getAuthor() {
        return author;
    }

    public String getCreated() {
        return created;
    }

    public boolean getIsEdited() {
        return isEdited;
    }

    public String getMessage() {
        return message;
    }

    public long getParent() {
        return parent;
    }

    public int getThread() {
        return thread;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public void setIsEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setParent(long parent) {
        this.parent = parent;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

}
