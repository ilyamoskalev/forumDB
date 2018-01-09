package application.controllers;

import application.models.*;
import application.services.PostService;
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
    @Autowired
    private PostService postService;

    @PostMapping(value = "/{slug_or_id}/create", consumes = JSON, produces = JSON)
    public ResponseEntity createPost(@PathVariable(name = "slug_or_id") String slug, @RequestBody List<Post> posts) {
        final MyThread thread = threadService.getBySlug(slug);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Thread " + slug + " not found"));
        }
        if (!posts.isEmpty()) {
            final String created = Converter.fromTimestamp(new Timestamp(System.currentTimeMillis()));
            for (Post post : posts) {
                final Integer parentId = post.getParent();
                if (parentId != 0) {
                    final Post parent = postService.getById(parentId);
                    if (parent == null) {
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + parentId);
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("No parent"));
                    }
                    if (parent.getThread() != thread.getId()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Message("Parent post was created in another thread"));
                    }
                }
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
    public ResponseEntity createVote(@PathVariable(name = "slug_or_id") String slug, @RequestBody Vote vote) {
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
    public ResponseEntity details(@PathVariable(name = "slug_or_id") String slug) {
        final MyThread thread = threadService.getBySlug(slug);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Thread " + slug + " not found"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @PostMapping(value = "/{slug_or_id}/details", produces = JSON)
    public ResponseEntity updatePost(@PathVariable(name = "slug_or_id") String slug, @RequestBody MyThread newThread) {
        final MyThread thread = threadService.getBySlug(slug);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Thread " + slug + " not found"));
        }
        final String message = newThread.getMessage();
        if (message != null) {
            thread.setMessage(message);
        }
        final String title = newThread.getTitle();
        if (title != null) {
            thread.setTitle(title);
        }
        threadService.update(thread);
        return ResponseEntity.status(HttpStatus.OK).body(thread);
    }

    @GetMapping(value = "/{slug_or_id}/posts", produces = JSON)
    public ResponseEntity getPosts(@PathVariable(name = "slug_or_id") String slug,
                                   @RequestParam(value = "limit", required = false) Integer limit,
                                   @RequestParam(value = "since", required = false) Integer since,
                                   @RequestParam(value = "sort", required = false) String sort,
                                   @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc) {

        final MyThread thread = threadService.getBySlug(slug);
        if (thread == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Thread " + slug + " not found"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(threadService.getPosts(thread.getId(), limit, since, sort, desc));
    }
}

