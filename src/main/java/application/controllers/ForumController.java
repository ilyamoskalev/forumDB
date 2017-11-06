package application.controllers;

import application.models.Forum;
import application.models.Thread;
import application.services.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/forum")
public class ForumController {
    public static final String JSON = "application/json";

    private ForumService service;

    @Autowired
    public ForumController(ForumService service) {
        this.service = service;
    }

    @PostMapping(path = "/create", consumes = JSON, produces = JSON)
    public ResponseEntity createForum(@RequestBody Forum forum) {
        return service.createForum(forum);
    }

    @PostMapping(path = "/{slug}/create", consumes = JSON, produces = JSON)
    public ResponseEntity changeUser(@PathVariable("slug") String slug, @RequestBody Thread thread) {
        thread.setForum(slug);
        return service.createThread(thread);
    }

    @GetMapping(path = "/{slug}/details", produces = JSON)
    public ResponseEntity details(@PathVariable("slug") String slug) {
        return service.details(slug);
    }

    @GetMapping(path = "/{slug}/threads", produces = JSON)
    public ResponseEntity threads(@PathVariable("slug") String slug, @RequestParam(value = "limit", required = false) Integer limit,
                                  @RequestParam(value = "since", required = false) String since,
                                  @RequestParam(value = "desc", required = false) Boolean desc) {
        return service.threads(slug, limit, since, desc);
    }

    @GetMapping(path = "/{slug}/users", produces = JSON)
    public ResponseEntity users(@PathVariable("slug") String slug, @RequestParam(value = "limit", required = false) Integer limit,
                                @RequestParam(value = "since", required = false) String since,
                                @RequestParam(value = "desc", required = false) Boolean desc) {
        return service.users(slug, limit, since, desc);
    }
}
