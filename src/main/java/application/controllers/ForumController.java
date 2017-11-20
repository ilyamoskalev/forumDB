package application.controllers;

import application.models.Forum;
import application.models.Message;
import application.models.MyThread;
import application.models.User;
import application.services.ForumService;
import application.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/forum")
public class ForumController {
    public static final String JSON = "application/json";
    @Autowired
    private ForumService service;
    @Autowired
    private UserService userService;

    @PostMapping(path = "/create", consumes = JSON, produces = JSON)
    public ResponseEntity createForum(@RequestBody Forum forum) {
        final String nickname = forum.getUser();
        final User user = userService.getUser(nickname);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(nickname + "not found"));
        }
        forum.setUser(user.getNickname());
        return service.createForum(forum);
    }

    @PostMapping(path = "/{slug}/create", consumes = JSON, produces = JSON)
    public ResponseEntity changeUser(@PathVariable("slug") String slug, @RequestBody MyThread thread) {
        thread.setForum(slug);
        final String nickname = thread.getAuthor();
        final User user = userService.getUser(nickname);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(nickname + " not found"));
        }
        final String forunName = thread.getForum();
        final Forum forum = service.details(forunName);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(forunName + " not found"));
        }
        thread.setForum(forum.getSlug());
        return service.createThread(thread);
    }

    @GetMapping(path = "/{slug}/details", produces = JSON)
    public ResponseEntity details(@PathVariable("slug") String slug) {
        final Forum forum = service.details(slug);
        if (forum == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Forum " + slug + " not found"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(forum);
    }

    @GetMapping(path = "/{slug}/threads", produces = JSON)
    public ResponseEntity threads(@PathVariable("slug") String slug, @RequestParam(value = "limit", required = false) Integer limit,
                                  @RequestParam(value = "since", required = false) String since,
                                  @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc) {
        final ResponseEntity entity = details(slug);
        if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
            return entity;
        }
        return ResponseEntity.status(HttpStatus.OK).body(service.threads(slug, limit, since, desc));
    }

    @GetMapping(path = "/{slug}/users", produces = JSON)
    public ResponseEntity users(@PathVariable("slug") String slug, @RequestParam(value = "limit", required = false) Integer limit,
                                @RequestParam(value = "since", required = false) String since,
                                @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc) {
        final ResponseEntity entity = details(slug);
        if (entity.getStatusCode() == HttpStatus.NOT_FOUND) {
            return entity;
        }
        return ResponseEntity.status(HttpStatus.OK).body(service.users(slug, limit, since, desc));
    }
}
