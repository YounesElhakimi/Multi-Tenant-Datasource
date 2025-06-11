package com.izicap.dynamicmultidatabase;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@Api(tags = "Post Management", description = "Operations for managing posts across multiple tenant databases")
public class PostController {
    
    @Autowired
    private PostRepository postRepository;

    @GetMapping("/test")
    @ApiOperation(
        value = "Retrieve posts from specified tenant database",
        notes = "Fetches all posts from the specified tenant database. Use 'client' parameter to specify the tenant database.",
        response = Post.class,
        responseContainer = "List"
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved posts"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<Iterable<Post>> getTest(
            @ApiParam(
                value = "Client identifier to determine which database to query", 
                allowableValues = "main,client-a,client-b",
                defaultValue = "main",
                example = "client-a"
            )
            @RequestParam(defaultValue = "main") String client) {
        
        try {
            switch (client) {
                case "client-a":
                    DBContextHolder.setCurrentDb(DBTypeEnum.CLIENT_A);
                    break;
                case "client-b":
                    DBContextHolder.setCurrentDb(DBTypeEnum.CLIENT_B);
                    break;
                default:
                    DBContextHolder.setCurrentDb(DBTypeEnum.MAIN);
                    break;
            }
            
            Iterable<Post> posts = postRepository.findAll();
            return ResponseEntity.ok(posts);
            
        } finally {
            // Clear the context to prevent memory leaks
            DBContextHolder.clear();
        }
    }

    @GetMapping("/init-data")
    @ApiOperation(
        value = "Initialize sample data",
        notes = "Creates sample posts in all three tenant databases (main, client-a, client-b). " +
               "This is useful for testing the multi-database routing functionality.",
        response = String.class
    )
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Sample data successfully created in all databases"),
        @ApiResponse(code = 500, message = "Error occurred while initializing data")
    })
    public ResponseEntity<String> initialData() {
        try {
            // Insert into main database
            DBContextHolder.setCurrentDb(DBTypeEnum.MAIN);
            postRepository.save(new Post(1L, "Main DB"));
            
            // Insert into client A database
            DBContextHolder.setCurrentDb(DBTypeEnum.CLIENT_A);
            postRepository.save(new Post(1L, "Client A DB"));
            
            // Insert into client B database
            DBContextHolder.setCurrentDb(DBTypeEnum.CLIENT_B);
            postRepository.save(new Post(1L, "Client B DB"));
            
            return ResponseEntity.ok("Success! Sample data created in all databases.");
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error initializing data: " + e.getMessage());
        } finally {
            // Clear the context to prevent memory leaks
            DBContextHolder.clear();
        }
    }
}