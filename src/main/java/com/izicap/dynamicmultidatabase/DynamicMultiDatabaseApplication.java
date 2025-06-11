package com.izicap.dynamicmultidatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class DynamicMultiDatabaseApplication {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMultiDatabaseApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Dynamic Multi-Database Application...");
        
        try {
            var context = SpringApplication.run(DynamicMultiDatabaseApplication.class, args);
            Environment env = context.getEnvironment();
            
            String serverPort = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "");
            
            logger.info("=================================================================");
            logger.info("Dynamic Multi-Database Application started successfully!");
            logger.info("Application is running on: http://localhost:{}{}", serverPort, contextPath);
            logger.info("Swagger UI available at: http://localhost:{}{}/swagger-ui/", serverPort, contextPath);
            logger.info("API Documentation: http://localhost:{}{}/v2/api-docs", serverPort, contextPath);
            logger.info("=================================================================");
            
        } catch (Exception e) {
            logger.error("Failed to start Dynamic Multi-Database Application", e);
            System.exit(1);
        }
    }
}