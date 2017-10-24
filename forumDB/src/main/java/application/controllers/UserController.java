package application.controllers;

import application.models.User;
import application.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/user/{nickname}")
public class UserController {
    public static final String JSON = "application/json";

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/create", consumes = JSON, produces = JSON)
    public ResponseEntity createUser(@PathVariable("nickname") String nickname, @RequestBody User user) {
        user.setNickname(nickname);
        return userService.createUser(user);
    }

    @PostMapping(path = "/profile", consumes = JSON, produces = JSON)
    public ResponseEntity changeUser(@PathVariable("nickname") String nickname, @RequestBody User user) {
        user.setNickname(nickname);
        return userService.changeUser(user);
    }

    @GetMapping(path = "/profile", produces = JSON)
    public ResponseEntity getUser(@PathVariable("nickname") String nickname) {
        return userService.getUser(nickname);
    }
}
