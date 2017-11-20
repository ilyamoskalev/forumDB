package application.services;

import application.models.Post;
import application.utils.Converter;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    @Autowired
    private JdbcTemplate template;

    @Nullable
    public Post getById(Integer id) {
        try {
            final String query = "SELECT * FROM posts WHERE id = ?";
            return template.queryForObject(query, POST_MAPPER, id);
        } catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public void update(Integer id, String message) {
            final String query = "UPDATE posts SET message = ?, isedited = TRUE WHERE id = ?";
            template.update(query, message, id);
    }

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
