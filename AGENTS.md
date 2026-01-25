# AGENTS.md - Mini-ERP System

This document provides guidelines for agentic coding tools operating in this repository.

## Build/Lint/Test Commands

### Build Commands
- **Build the project**: `./gradlew build`
- **Run the application**: `./gradlew bootRun`
- **Clean build artifacts**: `./gradlew clean`

### Test Commands
- **Run all tests**: `./gradlew test`
- **Run a single test**: `./gradlew test --tests "com.example.demo.ClassName.methodName"`
- **Run tests with fail-fast**: `./gradlew test --fail-fast`
- **Run tests in debug mode**: `./gradlew test --debug-jvm`

### Lint/Format Commands
- **Check code style**: `./gradlew check`
- **Generate Javadoc**: `./gradlew javadoc`

## Code Style Guidelines

### Imports
- Use specific imports (e.g., `import java.util.List` instead of `import java.util.*`)
- Group imports by package (Java standard library, third-party libraries, project-specific)
- Lombok annotations should be imported separately

### Formatting
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Opening braces on the same line
- One statement per line
- Consistent spacing around operators and after commas

### Types
- Use primitive types (`int`, `double`) for simple values
- Use wrapper classes (`Integer`, `Double`) when nullability is required
- Prefer interfaces over implementations (e.g., `List` over `ArrayList`)

### Naming Conventions
- **Classes**: PascalCase (e.g., `ProductService`)
- **Methods**: camelCase (e.g., `getProductById`)
- **Variables**: camelCase (e.g., `productName`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_PRODUCTS`)
- **Packages**: lowercase (e.g., `com.example.demo`)

### Error Handling
- Use specific exception types (e.g., `ResourceNotFoundException`)
- Provide meaningful error messages
- Use `@ExceptionHandler` for global exception handling
- Validate input using Jakarta Validation annotations

### Documentation
- Use Javadoc for public classes and methods
- Include `@param` and `@return` tags where applicable
- Document exceptions with `@throws`
- Keep comments concise and meaningful

### Spring Boot Specific
- Use constructor injection for dependencies
- Annotate services with `@Service`, controllers with `@RestController`
- Use `@Repository` for DAO classes
- Follow RESTful conventions for API endpoints

### Lombok Usage
- Use `@Data` for simple POJOs
- Use `@NoArgsConstructor` and `@AllArgsConstructor` for entity classes
- Use `@Builder` for complex object creation

### Testing
- Use JUnit 5 for unit tests
- Use `@SpringBootTest` for integration tests
- Follow the Arrange-Act-Assert pattern
- Test both positive and negative scenarios

### PDF Generation
- Use OpenPDF library for PDF generation
- Follow German formatting conventions for dates and numbers
- Include proper error handling for PDF generation

## Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── Product.java          # Entity class
│   │   ├── ProductService.java   # Service layer
│   │   ├── ProductController.java # REST controller
│   │   ├── ProductRepository.java # JPA repository
│   │   ├── GlobalExceptionHandler.java # Exception handling
│   │   └── SecurityConfig.java    # Security configuration
│   └── resources/
└── test/
    └── java/com/example/demo/
        └── MiniErpSystemApplicationTests.java # Test class
```

## Dependencies

- Spring Boot 3.2.1
- Java 21
- Lombok for boilerplate code reduction
- OpenPDF for PDF generation
- H2 Database for development
- Spring Security for authentication
- Spring Data JPA for database operations

## Best Practices

1. Keep methods small and focused on a single responsibility
2. Use meaningful variable and method names
3. Follow the DRY (Don't Repeat Yourself) principle
4. Write tests for all new functionality
5. Handle edge cases and validate inputs
6. Use proper logging for debugging and monitoring
7. Keep controllers thin, move business logic to services
8. Use DTOs for API requests/responses when needed
9. Follow RESTful conventions for API design
10. Document all public APIs

## Common Patterns

- Repository pattern for data access
- Service layer for business logic
- DTO pattern for API communication
- Exception handling with `@ControllerAdvice`
- Dependency injection via constructors
- Validation using Jakarta Validation annotations
