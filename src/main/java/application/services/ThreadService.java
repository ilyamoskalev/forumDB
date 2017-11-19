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

import java.util.List;

@Service
public class ThreadService {
    @Autowired
    private JdbcTemplate template;

    public void createPosts(List<Post> posts) {
        String query = "INSERT INTO posts(author, created, forum, message, parent, thread) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        for (Post post : posts) {
            final Integer id = template.queryForObject(query, Integer.class, post.getAuthor(), Converter.toTimestamp(post.getCreated()), post.getForum(), post.getMessage(), post.getParent(), post.getThread());
            post.setId(id);
        }
        query = "UPDATE Forums SET posts = posts + ? WHERE LOWER(slug) = LOWER(?)";
        template.update(query, posts.size(), posts.get(0).getForum());
    }

    @Nullable
    public MyThread getBySlug(String slug) {
        String query;
        try {
            final Integer id = Integer.parseInt(slug);
            try {
                query = "SELECT * FROM Threads WHERE LOWER(slug) = LOWER(?) OR id = ?";
                return template.queryForObject(query, THREAD_MAPPER, slug, id);
            } catch(EmptyResultDataAccessException e){
                return null;
            }
        } catch(NumberFormatException e) {
            try {
                query = "SELECT * FROM Threads WHERE LOWER(slug) = LOWER(?)";
                return template.queryForObject(query, THREAD_MAPPER, slug);
            } catch(EmptyResultDataAccessException e1){
                return null;
            }
        }
    }

    public Integer vote(Vote vote) {
        Integer voice = vote.getVoice();
        try {
            final String query = "INSERT INTO Votes(username, voice, thread) VALUES(?, ?, ?)";
            template.update(query, vote.getNickname(), voice, vote.getThread());
        } catch (DuplicateKeyException e) {
            String query = "SELECT voice FROM Votes WHERE username = ?";
            Integer oldVoice = template.queryForObject(query, Integer.class, vote.getNickname());
            query = "UPDATE Votes SET voice = ? WHERE username = ?";
            template.update(query, voice, vote.getNickname());
            voice -= oldVoice;
        }
        final String query = "UPDATE Threads SET votes = votes + ? WHERE id = ? RETURNING votes";
        return template.queryForObject(query, Integer.class, voice,vote.getThread());
    }

//    @Nullable
//    public MyThread getById(Integer id){
//        try {
//            final String query = "SELECT * FROM Threads WHERE id = ?";
//            return template.queryForObject(query, THREAD_MAPPER, id);
//        } catch (EmptyResultDataAccessException e) {
//            return null;
//        }
//    }

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
}
