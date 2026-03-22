# Onboard New Developer

## Overview
Comprehensive onboarding guide for new developers joining the Mom Care Platform backend team.

## Prerequisites

### System Requirements
- [ ] macOS, Linux, or Windows with WSL2
- [ ] Java 17 or higher
- [ ] Git installed and configured
- [ ] IDE (IntelliJ IDEA, VS Code, or Cursor)
- [ ] MongoDB installed and running
- [ ] curl and jq for API testing

### Development Tools
- [ ] Docker (optional, for containerized development)
- [ ] Postman (for API testing)
- [ ] MongoDB Compass (for database management)
- [ ] Git client (GitHub CLI recommended)

## Environment Setup

### 1. Clone Repository
```bash
# Clone the repository
git clone https://github.com/yehia2030/MomProjBackend.git
cd MomProjBackend

# Checkout the main branch
git checkout main
git pull origin main
```

### 2. Install Dependencies
```bash
# Install Java 17 (if not already installed)
# macOS with Homebrew
brew install openjdk@17

# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# Windows with Chocolatey
choco install openjdk17
```

### 3. Install MongoDB
```bash
# macOS with Homebrew
brew tap mongodb/brew
brew install mongodb-community

# Ubuntu/Debian
wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | sudo apt-key add -
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/6.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-6.0.list
sudo apt-get update
sudo apt-get install -y mongodb-org

# Start MongoDB
brew services start mongodb-community  # macOS
sudo systemctl start mongod            # Linux
```

### 4. Install Development Tools
```bash
# Install curl and jq
# macOS
brew install curl jq

# Ubuntu/Debian
sudo apt install curl jq

# Install GitHub CLI
# macOS
brew install gh

# Ubuntu/Debian
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update
sudo apt install gh
```

## Project Setup

### 1. Build Project
```bash
# Make gradlew executable
chmod +x gradlew

# Build the project
./gradlew build

# Run tests to verify setup
./gradlew test
```

### 2. Database Setup
```bash
# Start MongoDB service
brew services start mongodb-community  # macOS
sudo systemctl start mongod            # Linux

# Seed the database with test data
./seed.sh

# Verify database connection
mongosh
# In MongoDB shell:
# show dbs
# use momcare
# show collections
# exit
```

### 3. Start Development Server
```bash
# Start the Ktor server
./gradlew run

# Server should start on http://localhost:8080
# Verify it's running:
curl http://localhost:8080/api/categories
```

### 4. Run API Tests
```bash
# Run comprehensive API tests
./tests/run-all-tests.sh

# Run individual test suites
./tests/test-auth.sh
./tests/test-mom.sh
./tests/test-doctor.sh
./tests/test-admin.sh
```

## IDE Configuration

### IntelliJ IDEA Setup
1. **Import Project**
   - Open IntelliJ IDEA
   - Import project from `build.gradle.kts`
   - Wait for Gradle sync to complete

2. **Configure Kotlin**
   - Install Kotlin plugin if not already installed
   - Set project SDK to Java 17
   - Configure Kotlin compiler settings

3. **Code Style**
   - Import code style from project settings
   - Enable auto-formatting on save
   - Configure import organization

### VS Code Setup
1. **Install Extensions**
   - Kotlin Language
   - Gradle for Java
   - REST Client
   - MongoDB for VS Code

2. **Configure Settings**
   - Set Java home to Java 17
   - Configure Kotlin language server
   - Enable format on save

### Cursor Setup
1. **Install Cursor**
   - Download from https://cursor.sh
   - Install and configure

2. **Project Configuration**
   - Open project in Cursor
   - Configure Kotlin support
   - Set up AI assistance

## Project Understanding

### 1. Architecture Overview
```
Mom Care Platform Backend
├── Authentication & Authorization (JWT, RBAC)
├── User Management (Mom, Doctor, Admin)
├── E-commerce (Products, Cart, Orders)
├── File Management (Photos, Documents)
└── Admin Operations (User Management, Content)
```

### 2. Technology Stack
- **Framework**: Ktor (Kotlin)
- **Database**: MongoDB with KMongo
- **Authentication**: JWT with refresh tokens
- **Serialization**: Gson (NOT kotlinx.serialization)
- **Dependency Injection**: Koin
- **Testing**: Bash scripts with curl and jq
- **Documentation**: OpenAPI/Swagger, Postman

### 3. Project Structure
```
src/main/kotlin/
├── Application.kt                 # Main application entry point
├── auth/                         # Authentication & authorization
├── data/
│   ├── models/                   # Data models
│   ├── requests/                 # Request DTOs
│   ├── responses/                # Response DTOs
│   └── repository/               # Data access layer
├── di/                          # Dependency injection (Koin)
├── routes/                      # API route definitions
├── service/                     # Business logic layer
└── util/                        # Utility functions
```

### 4. Key Concepts

#### Authentication & Authorization
- **JWT Tokens**: Used for authentication
- **Role-Based Access Control**: Mom, Doctor, Admin roles
- **Route Interceptors**: `momRoute`, `doctorRoute`, `adminRoute`
- **Basic Routes**: `momRouteBasic`, `doctorRouteBasic` (no authorization check)

#### Serialization
- **Gson**: Primary JSON serialization library
- **Request Models**: Data classes for API requests
- **Response Models**: Data classes for API responses
- **Validation**: Server-side validation for all inputs

#### Testing
- **Unit Tests**: Kotlin test classes
- **API Tests**: Bash scripts with curl
- **Test Data**: Seeded database with test users
- **Test Coverage**: Comprehensive endpoint testing

## Development Workflow

### 1. Feature Development
```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Make changes
# ... develop your feature ...

# Run tests
./gradlew test
./tests/run-all-tests.sh

# Commit changes
git add .
git commit -m "feat: Add your feature description"

# Push branch
git push origin feature/your-feature-name

# Create pull request
gh pr create --title "feat: Add your feature" --body "Description of changes"
```

### 2. Code Standards
- **No Comments**: Except actionable TODOs
- **Clean Code**: Self-documenting code
- **Gson Only**: Never use kotlinx.serialization
- **Validation**: Always validate inputs
- **Testing**: Write tests for new functionality
- **Documentation**: Update Swagger and Postman

### 3. Testing Workflow
```bash
# Run unit tests
./gradlew test

# Run API tests
./tests/run-all-tests.sh

# Run specific test suite
./tests/test-mom.sh

# Check test results
tail -20 test-results.log

# Clean up log files after analysis
rm -f test-results.log
```

## Common Tasks

### 1. Adding New API Endpoint
1. Create request/response models
2. Add repository methods
3. Implement service logic
4. Create route handlers
5. Add tests
6. Update documentation

### 2. Debugging Issues
```bash
# Check server logs
tail -f logs/application.log

# Test specific endpoint
curl -v -X GET http://localhost:8080/api/endpoint \
  -H "Authorization: Bearer YOUR_TOKEN"

# Check database
mongosh
use momcare
db.collection.find()
```

### 3. Database Operations
```bash
# Connect to MongoDB
mongosh

# Switch to database
use momcare

# View collections
show collections

# Query data
db.moms.find()
db.doctors.find()
db.products.find()

# Exit
exit
```

## Learning Resources

### 1. Project Documentation
- **README.md**: Project overview and setup
- **.cursorrules**: Coding standards and guidelines
- **OpenAPI docs**: API documentation at `/swagger`
- **Postman collection**: API testing collection

### 2. Technology Documentation
- **Ktor**: https://ktor.io/docs/
- **Kotlin**: https://kotlinlang.org/docs/
- **MongoDB**: https://docs.mongodb.com/
- **Gson**: https://github.com/google/gson

### 3. Code Examples
- **Existing routes**: Study `src/main/kotlin/routes/`
- **Service patterns**: Review `src/main/kotlin/service/`
- **Test examples**: Check `tests/` directory
- **Request models**: See `src/main/kotlin/data/requests/`

## Troubleshooting

### Common Issues

#### Build Failures
```bash
# Clean and rebuild
./gradlew clean build

# Check Java version
java -version

# Verify Gradle wrapper
./gradlew --version
```

#### Database Connection Issues
```bash
# Check MongoDB status
brew services list | grep mongodb  # macOS
sudo systemctl status mongod       # Linux

# Restart MongoDB
brew services restart mongodb-community  # macOS
sudo systemctl restart mongod            # Linux
```

#### Test Failures
```bash
# Check server is running
curl http://localhost:8080/api/categories

# Verify test data
./seed.sh

# Check test logs
tail -20 test-results.log

# Clean up log files after analysis
rm -f test-results.log
```

#### Authentication Issues
```bash
# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"beth@example.com","password":"password123"}'

# Check JWT token
# Use the token from login response for other requests
```

## Getting Help

### 1. Team Resources
- **Code Review**: Ask for code reviews on pull requests
- **Pair Programming**: Request pair programming sessions
- **Documentation**: Check project documentation first
- **Issues**: Create GitHub issues for bugs or questions

### 2. External Resources
- **Stack Overflow**: For technical questions
- **Kotlin Slack**: For Kotlin-specific questions
- **Ktor Community**: For Ktor framework questions
- **MongoDB Community**: For database questions

### 3. Best Practices
- **Ask Questions**: Don't hesitate to ask for help
- **Read Documentation**: Check docs before asking
- **Test Thoroughly**: Always test your changes
- **Follow Standards**: Adhere to project coding standards
- **Code Reviews**: Participate in code reviews

## First Week Checklist

### Day 1: Environment Setup
- [ ] Clone repository
- [ ] Install dependencies
- [ ] Build project
- [ ] Start server
- [ ] Run tests

### Day 2: Project Understanding
- [ ] Read project documentation
- [ ] Explore codebase structure
- [ ] Understand authentication flow
- [ ] Review API endpoints

### Day 3: Development Setup
- [ ] Configure IDE
- [ ] Set up debugging
- [ ] Create test branch
- [ ] Make small change

### Day 4: Testing & Documentation
- [ ] Run comprehensive tests
- [ ] Update documentation
- [ ] Create pull request
- [ ] Get code review

### Day 5: First Feature
- [ ] Implement small feature
- [ ] Write tests
- [ ] Update documentation
- [ ] Deploy and verify

## Success Metrics

### Week 1
- [ ] Environment fully set up
- [ ] Can build and run project
- [ ] Understands project architecture
- [ ] Can make simple changes

### Week 2
- [ ] Can implement new features
- [ ] Writes comprehensive tests
- [ ] Updates documentation
- [ ] Participates in code reviews

### Month 1
- [ ] Contributes to major features
- [ ] Mentors other developers
- [ ] Improves project processes
- [ ] Becomes team expert

## Welcome to the Team!

You're now ready to contribute to the Mom Care Platform backend. Remember to:
- Follow the coding standards
- Write comprehensive tests
- Update documentation
- Ask questions when needed
- Participate in code reviews

Good luck with your development journey! 🚀
