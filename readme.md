# Dependency Network API

Provide detailed information on your product and how to run it here

### Required Dependencies
- Docker 

To make your life easier you can find some of the endpoints in the Postman collection bellow:
https://www.getpostman.com/collections/fa7a362c4255e8e87bb9

### Improvements
- In memory DB for Neo4J
- Fix DNetwork tests (due to the chain of the requests some tests fail, but works properly when run them sequentially)
- Input validation for the routes
- Make the paths in one place
- Make sample of data for Integration tests
- Add Swagger file

### Stack
- Akka HTTP
- H2 in memory Relational Database
- Neo4J for Dependency network storage
- JWT for authentication / authorization
- Dockerized application
- BONUS implementation (more than bonus)

Please check the `it` folder in the `src` for the Integration Tests
Please check the `test` folder in the `src` for the Unit Tests

### Execute
```
- chmod +x run.sh
- ./run.sh
```