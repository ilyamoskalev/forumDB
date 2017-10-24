package application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServiceService {
    @Autowired
    private JdbcTemplate template;

//    public ResponseEntity status() {
//
//    }

    public ResponseEntity clear() {
        template.execute("TRUNCATE TABLE Posts, Votes, Threads, Forums, Users CASCADE");
        return ResponseEntity.status(HttpStatus.OK).body("Success");
    }
}
