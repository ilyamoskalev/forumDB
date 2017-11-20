package application.controllers;

import application.services.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/service")
public class ServiceController {
    public static final String JSON = "application/json";
    private ServiceService service;

    @Autowired
    public ServiceController(ServiceService service) {
        this.service = service;
    }

    @GetMapping(path = "/status", produces = JSON)
    public ResponseEntity createUser() {
        return service.status();
    }

    @PostMapping(path = "/clear", produces = JSON)
    public ResponseEntity getUser() {
        return service.clear();
    }
}
