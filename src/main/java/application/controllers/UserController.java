package application.controllers;

import application.models.Message;
import application.models.User;
import application.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/user/{nickname}")
public class UserController {
    public static final String JSON = "application/json";

    @Autowired
    private UserService userService;

    @PostMapping(path = "/create", consumes = JSON, produces = JSON)
    public ResponseEntity createUser(@PathVariable("nickname") String nickname, @RequestBody User user) {
        user.setNickname(nickname);
        final List<User> conflictUsers = userService.createUser(user);
        if(conflictUsers == null){
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictUsers);
    }

    @PostMapping(path = "/profile", consumes = JSON, produces = JSON)
    public ResponseEntity changeUser(@PathVariable("nickname") String nickname, @RequestBody User user) {
        user.setNickname(nickname);
        final User newUser = userService.getUser(nickname);
        if (newUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(nickname + " not found"));
        }
        user =  userService.changeUser(user, newUser);
        if(user == null){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("email already exists"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping(path = "/profile", produces = JSON)
    public ResponseEntity getUser(@PathVariable("nickname") String nickname) {
        final User user = userService.getUser(nickname);
        if(user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(nickname + " not found"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
