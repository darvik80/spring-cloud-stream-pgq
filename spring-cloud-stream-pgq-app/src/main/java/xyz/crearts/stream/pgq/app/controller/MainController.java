package xyz.crearts.stream.pgq.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController("/")
public class MainController {
    @Autowired
    private DataSource source;

    @GetMapping
    public String index() {
        return "success";
    }
}
