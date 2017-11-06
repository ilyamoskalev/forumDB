package application.services;

import application.models.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServiceService {
    @Autowired
    private JdbcTemplate template;

    public ResponseEntity status() {
        final Status status = new Status();
        int tmp = template.queryForObject("SELECT COUNT(*) FROM Users", Integer.class);
        status.setUser(tmp);
        tmp = template.queryForObject("SELECT COUNT(*) FROM Forums", Integer.class);
        status.setForum(tmp);
        tmp = template.queryForObject("SELECT COUNT(*) FROM Threads", Integer.class);
        status.setThread(tmp);
        tmp = template.queryForObject("SELECT COUNT(*) FROM Posta", Integer.class);
        status.setPost(tmp);
        return ResponseEntity.status(HttpStatus.OK).body(status);
    }

    public ResponseEntity clear() {
        template.execute("TRUNCATE TABLE Posts, Votes, Threads, Forums, Users CASCADE");
        return ResponseEntity.status(HttpStatus.OK).body("Success");
    }
}
