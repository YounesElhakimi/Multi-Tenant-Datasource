Dynamic Multi-Database Router
This project is a Spring Boot application demonstrating how to dynamically switch between multiple databases at runtime based on the context of an incoming HTTP request. It's a foundational example of a multi-tenant architecture where each tenant has its own dedicated database.

🚀 Overview
The core idea is to route database operations to a specific tenant's database. This is achieved by identifying the tenant from the request (in this case, via a URL query parameter) and using Spring's AbstractRoutingDataSource to select the appropriate DataSource for the transaction.

✨ How It Works
The request-to-database routing follows these steps:

Request Interception: The PostController receives an incoming HTTP request.

Tenant Identification: The controller inspects the client query parameter from the URL (/test?client=client-a).

Context Setting: Based on the tenant identifier, DBContextHolder sets the corresponding database type (DBTypeEnum) in a ThreadLocal variable. This ensures the context is bound to the current request thread and doesn't interfere with other concurrent requests.

DataSource Routing: The MultiRoutingDataSource, which extends AbstractRoutingDataSource, calls its determineCurrentLookupKey() method. This method retrieves the database type from DBContextHolder.

Connection Selection: The MultiRoutingDataSource uses the key to look up the actual DataSource from a map of configured data sources that was initialized at startup.

Database Operation: Spring Data JPA proceeds with the database operation using the selected data source.

Context Clearing: (Implicit) Once the request is complete, the ThreadLocal is cleared to prevent memory leaks and state pollution for the next request that uses the same thread.

📂 Key Components
DynamicMultiDatabaseApplication.java: The main entry point for the Spring Boot application.

PostController.java: A REST controller that exposes endpoints to test the dynamic routing. It sets the database context based on a request parameter.

PersistenceConfiguration.java: The central configuration class. It defines the DataSource beans for each database (main, clienta, clientb) and configures the MultiRoutingDataSource to manage them. It also sets up the JPA EntityManagerFactory and TransactionManager.

MultiRoutingDataSource.java: The core of the routing mechanism. It extends AbstractRoutingDataSource and implements determineCurrentLookupKey() to decide which database to use.

DBContextHolder.java: A utility class that uses a ThreadLocal to hold the database context for the current request thread.

DBTypeEnum.java: An enumeration (MAIN, CLIENT_A, CLIENT_B) that defines the unique keys for each database.

Post.java: A simple JPA entity.

PostRepository.java: A Spring Data JPA repository for the Post entity.

🛠️ Setup and Configuration
Prerequisites
Java 11 or newer

Maven

A running MySQL instance

Database Setup
You need to create three separate databases in your MySQL server:

multi_main

multi_client_a

multi_client_b

Application Properties
Configure the database connections in src/main/resources/application.properties. Update the username and password to match your MySQL setup.

# Main Database
app.datasource.main.jdbc-url=jdbc:mysql://localhost:3306/multi_main?useSSL=false
app.datasource.main.username=root
app.datasource.main.password=admin

# Client A Database
app.datasource.clienta.jdbc-url=jdbc:mysql://localhost:3306/multi_client_a?useSSL=false
app.datasource.clienta.username=root
app.datasource.clienta.password=admin

# Client B Database
app.datasource.clientb.jdbc-url=jdbc:mysql://localhost:3306/multi_client_b?useSSL=false
app.datasource.clientb.username=root
app.datasource.clientb.password=admin

⚙️ How to Run
Clone the repository.

Build the project using Maven:

mvn clean install

Run the application:

mvn spring-boot:run

The application will start on http://localhost:8080.

🌐 API Endpoints
You can use curl or your web browser to interact with the API endpoints.

1. Initialize Data
This endpoint will insert one record into each of the three databases, allowing you to verify that the routing is working correctly.

URL: GET /init-data

Example:

curl http://localhost:8080/init-data

Expected Response: Success!

2. Fetch Data from a Specific Database
This endpoint fetches all Post records. Use the client query parameter to specify which database to connect to.

URL: GET /test

Query Parameter: client (values: client-a, client-b; defaults to main)

Examples:
Fetch from the Main DB (default):

curl http://localhost:8080/test

Response: [{"id":1,"name":"Main DB"}]

Fetch from Client A's DB:

curl http://localhost:8080/test?client=client-a

Response: [{"id":1,"name":"Client A DB"}]

Fetch from Client B's DB:

curl http://localhost:8080/test?client=client-b

Response: [{"id":1,"name":"Client B DB"}]
