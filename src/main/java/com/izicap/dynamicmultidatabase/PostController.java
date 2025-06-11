package com.izicap.dynamicmultidatabase;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "Post Management", description = "Operations for managing posts across multiple tenant databases")
public class PostController {
    
    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    
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
        
        logger.info("Received request to fetch posts for client: {}", client);
        
        try {
            DBTypeEnum dbType;
            switch (client) {
                case "client-a":
                    dbType = DBTypeEnum.CLIENT_A;
                    break;
                case "client-b":
                    dbType = DBTypeEnum.CLIENT_B;
                    break;
                default:
                    dbType = DBTypeEnum.MAIN;
                    break;
            }
            
            logger.debug("Setting database context to: {}", dbType);
            DBContextHolder.setCurrentDb(dbType);
            
            logger.debug("Fetching posts from database: {}", dbType);
            Iterable<Post> posts = postRepository.findAll();
            
            logger.info("Successfully retrieved posts for client: {} from database: {}", client, dbType);
            return ResponseEntity.ok(posts);
            
        } catch (Exception e) {
            logger.error("Error occurred while fetching posts for client: {}", client, e);
            return ResponseEntity.internalServerError().build();
        } finally {
            logger.debug("Clearing database context for client: {}", client);
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
        logger.info("Starting initialization of sample data across all databases");
        
        try {
            // Insert into main database
            logger.debug("Inserting sample data into MAIN database");
            DBContextHolder.setCurrentDb(DBTypeEnum.MAIN);
            Post mainPost = postRepository.save(new Post(1L, "Main DB"));
            logger.debug("Successfully saved post in MAIN database: {}", mainPost);
            
            // Insert into client A database
            logger.debug("Inserting sample data into CLIENT_A database");
            DBContextHolder.setCurrentDb(DBTypeEnum.CLIENT_A);
            Post clientAPost = postRepository.save(new Post(1L, "Client A DB"));
            logger.debug("Successfully saved post in CLIENT_A database: {}", clientAPost);
            
            // Insert into client B database
            logger.debug("Inserting sample data into CLIENT_B database");
            DBContextHolder.setCurrentDb(DBTypeEnum.CLIENT_B);
            Post clientBPost = postRepository.save(new Post(1L, "Client B DB"));
            logger.debug("Successfully saved post in CLIENT_B database: {}", clientBPost);
            
            logger.info("Successfully initialized sample data in all databases");
            return ResponseEntity.ok("Success! Sample data created in all databases.");
            
        } catch (Exception e) {
            logger.error("Error occurred while initializing sample data", e);
            return ResponseEntity.internalServerError()
                .body("Error initializing data: " + e.getMessage());
        } finally {
            logger.debug("Clearing database context after data initialization");
            DBContextHolder.clear();
        }
    }
}