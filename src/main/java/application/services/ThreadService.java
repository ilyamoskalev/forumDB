package application.services;

import application.models.MyThread;
import application.models.Post;
import application.models.Vote;
import application.utils.Converter;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ThreadService {
    @Autowired
    private JdbcTemplate template;

    public void createPosts(List<Post> posts) {
        long start = System.currentTimeMillis();
        String query = "INSERT INTO posts(author, created, forum, message, parent, thread) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        for (Post post : posts) {
            final Integer id = template.queryForObject(query, Integer.class, post.getAuthor(), Converter.toTimestamp(post.getCreated()), post.getForum(), post.getMessage(), post.getParent(), post.getThread());
            post.setId(id);
        }
        long end = System.currentTimeMillis();
        System.out.println("Thread: createPosts "+(end-start)+"ms");
    }

    @Nullable
    public MyThread getBySlug(String slug) {
        long start = System.currentTimeMillis();
        String query;
        try {
            final Integer id = Integer.parseInt(slug);
            try {
                query = "SELECT * FROM Threads WHERE id = ?";
                MyThread thread = template.queryForObject(query, THREAD_MAPPER, id);
                long end = System.currentTimeMillis();
                System.out.println("Thread: getBySlug "+(end-start)+"ms");
                return thread;
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        } catch (NumberFormatException e) {
            try {
                query = "SELECT * FROM Threads WHERE LOWER(slug) = LOWER(?)";
                MyThread thread =  template.queryForObject(query, THREAD_MAPPER, slug);
                long end = System.currentTimeMillis();
                System.out.println("Thread: getBySlug!!! "+(end-start)+"ms");
                return thread;
            } catch (EmptyResultDataAccessException e1) {
                return null;
            }
        }
    }

    public Integer vote(Vote vote) {
        long start = System.currentTimeMillis();
        Integer voice = vote.getVoice();
        try {
            final String query = "INSERT INTO Votes(username, voice, thread) VALUES(?, ?, ?)";
            template.update(query, vote.getNickname(), voice, vote.getThread());
        } catch (DuplicateKeyException e) {
            String query = "SELECT voice FROM Votes WHERE username = ? AND thread = ?";
            final Integer oldVoice = template.queryForObject(query, Integer.class, vote.getNickname(), vote.getThread());
            query = "UPDATE Votes SET voice = ? WHERE username = ? AND thread = ?";
            template.update(query, voice, vote.getNickname(), vote.getThread());
            voice -= oldVoice;
        }
        final String query = "UPDATE Threads SET votes = votes + ? WHERE id = ? RETURNING votes";
        Integer votes = template.queryForObject(query, Integer.class, voice, vote.getThread());
        long end = System.currentTimeMillis();
        System.out.println("Thread: Vote "+(end-start)+"ms");
        return votes;
    }

    @Nullable
    public List<Post> getPosts(Integer thread, Integer limit, Integer since, String sort, Boolean desc) {
        long start = System.currentTimeMillis();
        List<Post> posts = null;
        if (sort == null || sort.equals("flat")) {
            posts = flat(thread, limit, since, desc);
            long end = System.currentTimeMillis();
            System.out.println("Thread: flat "+(end-start)+"ms");
        } else if (sort.equals("tree")) {
            posts = tree(thread, limit, since, desc);
            long end = System.currentTimeMillis();
            System.out.println("Thread: Tree "+(end-start)+"ms");
        } else if (sort.equals("parent_tree")) {
            posts = parentTree(thread, limit, since, desc);
            long end = System.currentTimeMillis();
            System.out.println("Thread: parentTree "+(end-start)+"ms");
        }
        return posts;
    }

    public List<Post> flat(Integer thread, Integer limit, Integer since, Boolean desc) {
        String query = "SELECT * FROM Posts WHERE thread = " + thread.toString();
        if (since != null) {
            if (desc) {
                query += " AND id < " + since.toString();
            } else {
                query += " AND id > " + since.toString();
            }
        }
        if (desc) {
            query += " ORDER BY created DESC, id DESC ";
        } else {
            query += " ORDER BY created , id ";
        }
        if (limit != null) {
            query += " LIMIT " + limit.toString();
        }
        return template.query(query, POST_MAPPER);
    }

    public List<Post> tree(Integer thread, Integer limit, Integer since, Boolean desc) {
        String query = "SELECT * FROM Posts WHERE thread = ?";
        if (since != null) {
            if (desc) {
                query += " AND path < (SELECT path FROM posts WHERE id = " + since.toString() + ") ";
            } else {
                query += " AND path > (SELECT path FROM posts WHERE id = " + since.toString() + ") ";
            }
        }
        query += "ORDER BY path";
        if (desc) {
            query += " DESC";
        }
        if (limit != null) {
            query += " LIMIT " + limit;
        }
        return template.query(query, POST_MAPPER, thread);
    }

    private List<Post> parentTree(Integer thread, Integer limit, Integer since, Boolean desc) {
        String query = "SELECT * FROM posts " +
                "WHERE thread = ? AND path[1] = ?";
        String parentQuery = "SELECT id FROM posts " +
                "WHERE thread = ? AND parent = 0";
        if (since != null) {
            if (desc) {
                query += " AND path[1] < (SELECT path[1] FROM posts WHERE id = " + since + ") ";
                parentQuery += " AND path[1] < (SELECT path[1] FROM posts WHERE id = " + since + ") ";
            } else {
                query += " AND path[1] > (SELECT path[1] FROM posts WHERE id = " + since + ") ";
                parentQuery += " AND path[1] > (SELECT path[1] FROM posts WHERE id = " + since + ") ";
            }
        }
        if (desc) {
            query += " ORDER BY path DESC, id DESC";
            parentQuery += " ORDER BY id DESC";
        } else {
            query += " ORDER BY path, id";
            parentQuery += " ORDER BY id";
        }
        if (limit != null) {
            parentQuery += " LIMIT " + limit;
        }
        final List<Integer> parents = template.query(parentQuery, (res, num) -> res.getInt("id"), thread);
        final List<Post> posts = new ArrayList<>();
        for (int parent : parents) {
            posts.addAll(template.query(query, POST_MAPPER, thread, parent));
        }
        return posts;
    }

    public void update(MyThread thread) {
        long start = System.currentTimeMillis();
        final String query = "Update Threads SET message = ?, title = ? WHERE id = ?";
        template.update(query, thread.getMessage(), thread.getTitle(), thread.getId());
        long end = System.currentTimeMillis();
        System.out.println("Thread: updateThread "+(end-start)+"ms");
    }

    private static final RowMapper<MyThread> THREAD_MAPPER = (res, num) -> new MyThread(
            res.getString("author"),
            res.getString("forum"),
            Converter.fromTimestamp(res.getTimestamp("created")),
            res.getString("message"),
            res.getString("slug"),
            res.getString("title"),
            res.getInt("votes"),
            res.getInt("id")
    );

    private static final RowMapper<Post> POST_MAPPER = (res, num) -> new Post(
            res.getInt("id"),
            res.getString("forum"),
            res.getString("author"),
            res.getInt("thread"),
            Converter.fromTimestamp(res.getTimestamp("created")),
            res.getBoolean("isEdited"),
            res.getString("message"),
            res.getInt("parent")
    );
}
