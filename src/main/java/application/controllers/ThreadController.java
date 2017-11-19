package application.controllers;

import application.models.*;
import application.services.ThreadService;
import application.services.UserService;
import application.utils.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping(value = "/api/thread")
public class ThreadController {
    public static final String JSON = "application/json";
    @Autowired
    private ThreadService threadService;
    @Autowired
    private UserService userService;

    @PostMapping(value = "/{slug_or_id}/create", consumes = JSON, produces = JSON)
    public ResponseEntity createPost(@PathVariable(name = "slug_or_id") String slug, @RequestBody List<Post> posts) {
        if (!posts.isEmpty()) {
            final MyThread thread = threadService.getBySlug(slug);
            if (thread == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Thread " + slug + " not found"));
            }
            final String created = Converter.fromTimestamp(new Timestamp(System.currentTimeMillis()));
            for (Post post : posts) {
                final User user = userService.getUser(post.getAuthor());
                if (user == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("User " + post.getAuthor() + " not found"));
                }
                if (post.getCreated() == null) {
                    post.setCreated(created);
                }
                post.setThread(thread.getId());
                post.setForum(thread.getForum());
            }
            threadService.createPosts(posts);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(posts);

    }

    @PostMapping(value = "/{slug_or_id}/vote", consumes = JSON, produces = JSON)
    public ResponseEntity createPost(@PathVariable(name = "slug_or_id") String slug, @RequestBody Vote vote) {
        final MyThread thread = threadService.getBySlug(slug);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Thread " + slug + " not found"));
        }
        vote.setThread(thread.getId());
        final String nickname = vote.getNickname();
        final User user = userService.getUser(nickname);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("User " + nickname + " not found"));
        }
        vote.setNickname(user.getNickname());
        thread.setVotes(threadService.vote(vote));
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @GetMapping(value = "/{slug_or_id}/details", produces = JSON)
    public ResponseEntity createPost(@PathVariable(name = "slug_or_id") String slug) {
        final MyThread thread = threadService.getBySlug(slug);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Thread " + slug + " not found"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }
}

