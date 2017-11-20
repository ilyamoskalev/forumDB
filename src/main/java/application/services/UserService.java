package application.services;

import application.models.User;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private JdbcTemplate template;

    @Nullable
    public User getUser(String nickname) {
        try {
            final String query = "SELECT * FROM Users WHERE LOWER (nickname) = LOWER (?)";
            User user =  template.queryForObject(query, USER_MAPPER, nickname);
            return user;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final RowMapper<User> USER_MAPPER = (res, num) -> new User(res.getString("about"),
            res.getString("email"),
            res.getString("fullname"),
            res.getString("nickname"));

    @Nullable
    public List<User> createUser(User user) {
        try {
            final String query = "INSERT INTO Users(nickname, fullname, email, about) VALUES(?,?,?,?)";
            template.update(query, user.getNickname(), user.getFullname(), user.getEmail(), user.getAbout());
            return null;
        } catch (DuplicateKeyException e) {
            final String query = "SELECT * FROM Users WHERE LOWER(nickname) =  LOWER(?) OR LOWER(email) =  LOWER(?)";
            List<User> users =  template.query(query, USER_MAPPER, user.getNickname(), user.getEmail());
            return users;
        }
    }

    @Nullable
    public User changeUser(User user, User newUser) {
        final List<Object> args = new ArrayList<>();
        String query = "";
        final String fullname = user.getFullname();
        if (fullname != null) {
            newUser.setFullname(fullname);
            query += "fullname = ?";
            args.add(fullname);
        }
        final String email = user.getEmail();
        if (email != null) {
            if (!query.isEmpty()) {
                query += ", ";
            }
            newUser.setEmail(email);
            query += "email = ?";
            args.add(email);
        }
        final String about = user.getAbout();
        if (about != null) {
            if (!query.isEmpty()) {
                query += ", ";
            }
            newUser.setAbout(about);
            query += "about = ?";
            args.add(about);
        }
        if (!query.isEmpty()) {
            query = "UPDATE Users SET " + query + " WHERE LOWER (nickname) = LOWER (?)";
            args.add(user.getNickname());
            try {
                template.update(query, args.toArray(new Object[args.size()]));
                return newUser;
            } catch (DuplicateKeyException e) {
                return null;
            }
        }
        return newUser;
    }

}
