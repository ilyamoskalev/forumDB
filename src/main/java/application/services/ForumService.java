package application.services;

import application.models.Forum;
import application.models.Message;
import application.models.Thread;
import application.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForumService {

    @Autowired
    private JdbcTemplate template;

    private final UserService service = new UserService();

    private static final RowMapper<Forum> FORUM_MAPPER = (res, num) -> new Forum(
            res.getString("username"),
            res.getString("title"),
            res.getString("slug"),
            res.getInt("threads"),
            res.getLong("posts")
    );

    private static final RowMapper<Thread> THREAD_MAPPER = (res, num) -> new Thread(
            res.getString("author"),
            res.getString("forum"),
            res.getTimestamp("created"),
            res.getString("message"),
            res.getString("slug"),
            res.getString("title"),
            res.getInt("votes"),
            res.getInt("id")
    );

    public ResponseEntity createForum(Forum forum) {
        final String nickname = forum.getUser();
        final ResponseEntity entity = service.getUser(nickname);
        if( entity.getStatusCode() == HttpStatus.NOT_FOUND ){
            return entity;
        }


        try {
            final String query = "INSERT INTO Forums(slug, title, username) VALUES(?, ?, ?)";
            template.update(query, forum.getSlug(), forum.getTitle(), nickname);
            return ResponseEntity.status(HttpStatus.OK).body(forum);
        } catch (DuplicateKeyException e) {
            final String query = "SELECT * FROM Forums WHERE slug = ?";
            final Forum conflictForum = template.queryForObject(query, FORUM_MAPPER, forum.getSlug());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictForum);
        }
    }

    public ResponseEntity createThread(Thread thread){
        final String nickname = thread.getAuthor();
        ResponseEntity entity = service.getUser(nickname);
        if( entity.getStatusCode() == HttpStatus.NOT_FOUND ){
            return entity;
        }
        final String forum = thread.getForum();
        entity = details(forum);
        if( entity.getStatusCode() == HttpStatus.NOT_FOUND ){
            return entity;
        }

        try {
            String query = "INSERT INTO Threads(author, created, forum, message, slug, title) VALUES(?, ?, ?, ?, ?, ?)";
            template.update(query, nickname, thread.getCreated(), forum, thread.getMessage(), thread.getSlug(), thread.getTitle());
            query = "UPDATE Forum SET threads = threads + 1 WHERE slug = ?";
            template.update(query, forum);
            return ResponseEntity.status(HttpStatus.OK).body(thread);
        } catch (DuplicateKeyException e) {
            final String query = "SELECT * FROM Threads WHERE slug = ?";
            final Thread conflictThread = template.queryForObject(query, THREAD_MAPPER, forum);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictThread);
        }
    }

    public ResponseEntity details(String slug) {
        try {
            final String query = "SELECT * FROM Forums WHERE slug = ?";
            final Forum forum = template.queryForObject(query, FORUM_MAPPER, slug);
            return ResponseEntity.status(HttpStatus.OK).body(forum);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Forum " + slug + " not found"));
        }
    }

    public ResponseEntity threads(String slug, Integer limit, String since, Boolean desc) {
        final ResponseEntity entity = details(slug);
        if( entity.getStatusCode() == HttpStatus.NOT_FOUND ){
            return entity;
        }
        String query = "SELECT * FROM Threads WHERE forum = ?";
        if( since != null ) {
            if (desc) {
                query += " AND created <= '" + since + "'";
            } else {
                query += " AND created >= '" + since + "'";
            }
        }
        if(desc) {
            query += " DESC";
        }
        if( limit != null ) {
            query += " LIMIT " + limit.toString();
        }
        final List<Thread> threads = template.queryForList(query, Thread.class, slug);
        return ResponseEntity.status(HttpStatus.OK).body(threads);
    }

    public ResponseEntity users(String slug, Integer limit, String since, Boolean desc) {
        ResponseEntity entity = details(slug);
        if( entity.getStatusCode() == HttpStatus.NOT_FOUND ){
            return entity;
        }
        String query = "SELECT * FROM (SELECT u.about, u.mail, u.fullname, u.nickname " +
                "FROM Threads t LEFT JOIN Users u ON t.author = u.nickname AND t.forum = ? " +
                "UNION SELECT u.about, u.mail, u.fullname, u.nickname " +
                "FROM Posts p LEFT JOIN Users u ON p.author = u.nickname AND p.forum = ?) as u";
        if( since != null ) {
            if (desc) {
                query += " WHERE nickname < '" + since + "'";
            } else {
                query += " WHERE nickname > '" + since + "'";
            }
        }
        if(desc) {
            query += " DESC";
        }
        if( limit != null ) {
            query += " LIMIT " + limit.toString();
        }
        final List<User> users = template.queryForList(query, User.class, slug, slug);
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

}
