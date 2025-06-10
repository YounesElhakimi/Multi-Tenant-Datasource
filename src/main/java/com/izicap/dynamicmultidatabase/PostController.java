package com.izicap.dynamicmultidatabase;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class PostController {
    @Autowired
    private PostRepository postRepository;
    // test -> main DB
    // test?client=client-a -> Client A DB
    // test?client=client-b -> Client B DB
    @GetMapping("/test")
    @ResponseBody
    public Iterable<Post> getTest(@RequestParam(defaultValue = "main") String client) {
        switch (client) {
            case "client-a":
                DBContextHolder.setCurrentDb(DBTypeEnum.CLIENT_A);
                break;
            case "client-b":
                DBContextHolder.setCurrentDb(DBTypeEnum.CLIENT_B);
                break;
        }
        return postRepository.findAll();
    }
    @GetMapping("/init-data")
    @ResponseBody
    public String initialData() {
        String name = "main - " + (new Random()).nextInt();
        postRepository.save(new Post("Main DB"));
        DBContextHolder.setCurrentDb(DBTypeEnum.CLIENT_A);
        postRepository.save(new Post("Client A DB"));
        DBContextHolder.setCurrentDb(DBTypeEnum.CLIENT_B);
        postRepository.save(new Post("Client B DB"));
        return "Success!";
    }
}