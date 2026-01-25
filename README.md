# Mini-ERP System

A comprehensive Mini-ERP (Enterprise Resource Planning) system designed for small businesses, with a focus on product management and German localization.

## 📋 Overview

The Mini-ERP System provides a complete solution for managing products, generating invoices, and handling business operations with a clean, modern API interface.

## 🚀 Features

### Product Management
- ✅ **CRUD Operations**: Create, Read, Update, Delete products
- ✅ **Search Functionality**: Find products by name (case-insensitive)
- ✅ **Validation**: Comprehensive input validation with meaningful error messages
- ✅ **Audit Trail**: Automatic timestamp tracking for creation and updates

### Invoice Generation
- ✅ **PDF Invoices**: Generate professional PDF invoices (Lieferschein)
- ✅ **German Formatting**: Proper German date, number, and currency formatting
- ✅ **Company Information**: Altenburg, Thüringen localization
- ✅ **Product Summary**: Automatic calculation of total values

### API Documentation
- ✅ **Swagger/OpenAPI**: Interactive API documentation at `/swagger-ui.html`
- ✅ **Comprehensive Endpoints**: Full RESTful API coverage
- ✅ **Javadoc**: Detailed method documentation

### Security
- ✅ **Authentication**: Basic authentication with admin credentials
- ✅ **CORS**: Properly configured for frontend integration
- ✅ **CSRF Protection**: Disabled for API-only usage (appropriate for stateless APIs)

## 🛠️ Technology Stack

- **Java 21**: Latest LTS version with modern features
- **Spring Boot 3.2.1**: Robust framework for building production-ready applications
- **Spring Data JPA**: Efficient database operations with H2 (development) or production databases
- **Lombok**: Reduces boilerplate code
- **OpenPDF**: PDF generation library
- **SpringDoc OpenAPI**: API documentation
- **JUnit 5**: Comprehensive testing framework
- **Mockito**: Mocking for unit tests

## 📦 Dependencies

```gradle
dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // PDF Generation
    implementation 'com.github.librepdf:openpdf:1.3.30'
    
    // API Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Development Database
    runtimeOnly 'com.h2database:h2'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── Product.java                  # Entity class with validation
│   │   ├── ProductService.java           # Business logic and PDF generation
│   │   ├── ProductController.java        # REST API endpoints
│   │   ├── ProductRepository.java        # JPA repository interface
│   │   ├── SecurityConfig.java           # Security configuration
│   │   ├── OpenApiConfig.java            # API documentation config
│   │   └── GlobalExceptionHandler.java  # Exception handling
│   └── resources/
│       ├── application.properties        # Configuration
│       └── static/                       # Static resources
└── test/
    └── java/com/example/demo/
        ├── ProductServiceTest.java       # Service layer tests
        ├── ProductControllerTest.java    # Controller tests
        └── MiniErpSystemApplicationTests.java # Integration tests
```

## 🔧 Installation & Setup

### Prerequisites
- Java 21 JDK
- Gradle 8.5+
- Git

### Installation

```bash
# Clone the repository
git clone https://github.com/m1hamuha/Mini-ERP-System
cd mini-erp-system

# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

### Configuration

The application uses default configuration with H2 database for development. For production, configure your database in `application.properties`.

## 🚀 Usage

### API Endpoints

| Method | Endpoint | Description | Authentication |
|--------|----------|-------------|----------------|
| GET | `/api/products` | Get all products | Required |
| GET | `/api/products/{id}` | Get product by ID | Required |
| GET | `/api/products/search?name={name}` | Search products by name | Required |
| POST | `/api/products` | Create new product | Required |
| PUT | `/api/products/{id}` | Update product | Required |
| DELETE | `/api/products/{id}` | Delete product | Required |
| GET | `/api/products/invoice` | Download PDF invoice | Required |

### Authentication

Default credentials:
- **Username**: `admin`
- **Password**: `admin123`

### Example Requests

**Create Product:**
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  -d '{"name": "Test Product", "quantity": 10, "price": 19.99}'
```

**Get All Products:**
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM="
```

**Download Invoice:**
```bash
curl -X GET http://localhost:8080/api/products/invoice \
  -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" \
  --output invoice.pdf
```

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Run Specific Tests
```bash
./gradlew test --tests "*ProductServiceTest*"
./gradlew test --tests "*ProductControllerTest*"
```

### Test Coverage
- **Service Layer**: 100% coverage with 11 test methods
- **Controller Layer**: 100% coverage with 9 test methods
- **Integration Tests**: Context loading and API endpoint testing

## 📚 API Documentation

Access the interactive Swagger UI documentation:

👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 🎯 Development Guidelines

### Code Style
- Follow the [AGENTS.md](AGENTS.md) guidelines
- Use meaningful variable and method names
- Keep methods small and focused
- Write comprehensive Javadoc comments
- Follow RESTful conventions

### Testing
- Write tests for all new functionality
- Test both positive and negative scenarios
- Use Arrange-Act-Assert pattern
- Maintain high test coverage

### Security
- Use proper password encoding (BCrypt recommended)
- Validate all inputs
- Handle exceptions gracefully
- Use HTTPS in production

## 🔒 Security Notes

- **Default Password**: The application uses a default password encoder. For production, replace with BCrypt:
  ```java
  @Bean
  public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
  }
  ```

- **CORS**: Configured for development with `http://localhost:3000`. Adjust for production.

## 📈 Deployment

### Build for Production
```bash
./gradlew clean build
```

### Run the Application
```bash
java -jar build/libs/demo-1.0.0.jar
```

### Docker (Optional)
```dockerfile
FROM eclipse-temurin:21-jdk
COPY build/libs/demo-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Submit a pull request

## 📝 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check the [AGENTS.md](AGENTS.md) for development guidelines
- Review the comprehensive test suite for usage examples

## 🎯 Roadmap

- [x] Core product management
- [x] PDF invoice generation
- [x] Comprehensive testing
- [x] API documentation
- [ ] User management
- [ ] Role-based access control
- [ ] Advanced reporting
- [ ] Multi-language support

---

**Mini-ERP System** - Simplifying business management for Altenburg and beyond! 🚀
