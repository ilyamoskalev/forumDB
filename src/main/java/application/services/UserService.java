package application.services;

import application.models.Message;
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
public class UserService {

    @Autowired
    private JdbcTemplate template;

    public ResponseEntity getUser(String nickname) {
        try {
            final String query = "SELECT * FROM Users WHERE nickname = ?";
            final User user = template.queryForObject(query, USER_MAPPER, nickname);
            user.setNickname(nickname);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(nickname + "not found"));
        }
    }

    private static final RowMapper<User> USER_MAPPER = (res, num) -> new User(res.getString("about"),
            res.getString("email"),
            res.getString("fullname"),
            res.getString("nickname"));

    public ResponseEntity createUser(User user) {
        try {
            final String query = "INSERT INTO Users(nickname, fullname, email, about) VALUES(?,?,?,?)";
            template.update(query, user.getNickname(),user.getFullname(), user.getEmail(), user.getAbout());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (DuplicateKeyException e) {
            final String query = "SELECT * FROM Users WHERE LOWER(nickname) =  LOWER(?) OR LOWER(email) =  LOWER(?)";
            final List<User> users = template.query(query, USER_MAPPER, user.getNickname(), user.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(users);
        }
    }

    public ResponseEntity changeUser(User user) {
        final ResponseEntity responce = getUser(user.getNickname());
        if (responce.getStatusCode() == HttpStatus.NOT_FOUND) {
            return responce;
        }
        try {
            final String query = "UPDATE Users SET " +
                    "fullname = ?, " +
                    "email = ?, " +
                    "about = ? " +
                    "WHERE nickname = ?";
            template.update(query, user.getFullname(), user.getEmail(), user.getAbout(), user.getNickname());
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("email already exists"));
        }
    }

}
