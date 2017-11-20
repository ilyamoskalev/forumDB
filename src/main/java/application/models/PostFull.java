package application.models;

/**
 * Created by ilamoskalev on 20.11.2017.
 */
public class PostFull {
    private Post post;
    private User author;
    private Forum forum;
    private MyThread thread;

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public MyThread getThread() {
        return thread;
    }

    public void setThread(MyThread thread) {
        this.thread = thread;
    }
}
