package application.controllers;

import application.models.*;
import application.services.ForumService;
import application.services.PostService;
import application.services.ThreadService;
import application.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping(value = "/api/post")
public class PostController {
    public static final String JSON = "application/json";
    @Autowired
    private PostService postService;
    @Autowired
    private ForumService forumService;
    @Autowired
    private UserService userService;
    @Autowired
    private ThreadService threadService;

    @GetMapping(path = "/{id}/details", produces = JSON)
    public ResponseEntity getPost(@PathVariable(name = "id") int id, @RequestParam(name = "related", required = false) String[] related) {
        final Post post = postService.getById(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Post " + id + " not found"));
        }
        final PostFull responce = new PostFull();
        responce.setPost(post);
        if (related != null) {
            if(Arrays.asList(related).contains("forum")) {
                final Forum forum = forumService.details(post.getForum());
                responce.setForum(forum);
            }
            if(Arrays.asList(related).contains("user")) {
                final User user = userService.getUser(post.getAuthor());
                responce.setAuthor(user);
            }
            if(Arrays.asList(related).contains("thread")) {
                final MyThread thread = threadService.getBySlug(String.valueOf(post.getThread()));
                responce.setThread(thread);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(responce);
    }

    @PostMapping(path = "/{id}/details", consumes = JSON, produces = JSON)
    public ResponseEntity getPost(@PathVariable(name = "id") int id, @RequestBody Post newPost) {
        final Post post = postService.getById(id);
        if (post == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message("Post " + id + " not found"));
        }
        final String message = newPost.getMessage();
        if (message != null && !message.equals(post.getMessage())){
            post.setMessage(message);
            post.setIsEdited(true);
            postService.update(id, message);
        }
        return ResponseEntity.status(HttpStatus.OK).body(post);
    }
}
