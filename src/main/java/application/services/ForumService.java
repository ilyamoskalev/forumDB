package application.services;

import application.models.Forum;
import application.models.MyThread;
import application.models.User;
import application.utils.Converter;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class ForumService {

    @Autowired
    private JdbcTemplate template;

    private static final RowMapper<Forum> FORUM_MAPPER = (res, num) -> new Forum(
            res.getString("username"),
            res.getString("title"),
            res.getString("slug"),
            res.getInt("threads"),
            res.getLong("posts")
    );

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

    public ResponseEntity createForum(Forum forum) {
        final long start = System.currentTimeMillis();
        try {
            final String query = "INSERT INTO Forums(slug, title, username) VALUES(?, ?, ?)";
            template.update(query, forum.getSlug(), forum.getTitle(), forum.getUser());
            final long end = System.currentTimeMillis();
            System.out.println("Forum: createForum "+(end-start)+"ms");
            return ResponseEntity.status(HttpStatus.CREATED).body(forum);
        } catch (DuplicateKeyException e) {
            final String query = "SELECT * FROM Forums WHERE LOWER (slug) = LOWER (?)";
            final Forum conflictForum = template.queryForObject(query, FORUM_MAPPER, forum.getSlug());
            final long end = System.currentTimeMillis();
            System.out.println("Forum: createForumConflict "+(end-start)+"ms");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictForum);
        }
    }

    public ResponseEntity createThread(MyThread thread) {
        final long start = System.currentTimeMillis();
        final String nickname = thread.getAuthor();
        final String forum = thread.getForum();
        try {
            if (thread.getCreated() == null) {
                final String query = "INSERT INTO Threads(author, forum, message, slug, title) VALUES(?, ?, ?, ?, ?) RETURNING id";
                final Integer id = template.queryForObject(query, Integer.class, nickname, forum, thread.getMessage(), thread.getSlug(), thread.getTitle());
                thread.setId(id);
            } else {
                final String query = "INSERT INTO Threads(author, created, forum, message, slug, title) VALUES(?, ?, ?, ?, ?, ?) RETURNING id";
                final Integer id = template.queryForObject(query, Integer.class, nickname, Converter.toTimestamp(thread.getCreated()), forum, thread.getMessage(), thread.getSlug(), thread.getTitle());
                thread.setId(id);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(thread);
        } catch (DuplicateKeyException e) {
            final String query = "SELECT * FROM Threads WHERE LOWER (slug) = LOWER (?)";
            final MyThread conflictThread = template.queryForObject(query, THREAD_MAPPER, thread.getSlug());
            long end = System.currentTimeMillis();
            System.out.println("Forum: CreateThread "+(end-start)+"ms");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictThread);
        }
    }

    @Nullable
    public Forum details(String slug) {
        long start = System.currentTimeMillis();
        try {
            final String query = "SELECT * FROM Forums WHERE LOWER (slug) = LOWER (?)";
            Forum forum = template.queryForObject(query, FORUM_MAPPER, slug);
            long end = System.currentTimeMillis();
            System.out.println("Forum: Details "+(end-start)+"ms");
            return forum;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<MyThread> threads(String slug, Integer limit, String since, Boolean desc) {
        long start = System.currentTimeMillis();
        String query = "SELECT * FROM Threads WHERE LOWER (forum) = LOWER (?)";
        if (since != null) {
            Timestamp date = Converter.toTimestamp(since);
            if (desc) {
                query += " AND created <= '" + date + "'";
            } else {
                query += " AND created >= '" + date + "'";
            }
        }

        query += " ORDER BY created";

        if (desc) {
            query += " DESC";
        }
        if (limit != null) {
            query += " LIMIT " + limit.toString();
        }
        List<MyThread> threads = template.query(query, THREAD_MAPPER, slug);
        long end = System.currentTimeMillis();
        System.out.println("Forum: Threads "+(end-start)+"ms");
        return threads;
    }

    public List<User> users(String slug, Integer limit, String since, Boolean desc) {
        long start = System.currentTimeMillis();
        String query = "SELECT u.about, u.email, u.fullname, u.nickname FROM Boost b LEFT JOIN Users u ON b.username = u.nickname WHERE LOWER (b.slug) = LOWER(?)";
        if (since != null) {
            if (desc) {
                query += " AND LOWER(nickname) < LOWER('" + since + "')";
            } else {
                query += " AND LOWER(nickname) > LOWER('" + since + "')";
            }
        }
        query += " ORDER BY LOWER(u.nickname)";
        if (desc) {
            query += " DESC";
        }
        if (limit != null) {
            query += " LIMIT " + limit;
        }
        List<User> users = template.query(query, USER_MAPPER, slug);
        long end = System.currentTimeMillis();
        System.out.println("Forum: Users "+(end-start)+"ms");
        return users;
    }

    private static final RowMapper<User> USER_MAPPER = (res, num) -> new User(res.getString("about"),
            res.getString("email"),
            res.getString("fullname"),
            res.getString("nickname"));

}
