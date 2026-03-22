---
applyTo: "**"
---

# Copilot AI Coding Agent Instructions – Mom Care Platform Backend

## Architecture & Major Patterns
- **Ktor 3.x backend** with Kotlin, MongoDB (KMongo), and Koin DI.
- **Repository pattern**: All business logic is in `service/`, data access in `data/repository/api/` (interfaces) and `data/repository/impl/` (implementations).
- **Unified authentication**: Central `User` table, mapped to `Mom`/`Doctor` via `MomAuth`/`DoctorAuth`.
- **Role-based authorization**: Admin/Mom/Doctor roles with strict access control and caching.
- **Multipart registration**: Both Mom and Doctor registration require multipart form data with a JSON `data` field and three image files (`photo`, `nidFront`, `nidBack`).
- **JWT-based authentication**: All protected endpoints require a Bearer token; JWT includes `userType` and `userId`.
- **Session-based product access**: Moms have internal session tracking for authorization; session count is not exposed in public APIs for privacy reasons.
- **File uploads**: Images are stored in `uploads/` with UUID filenames; file paths are saved in DB, not file content.
- **MongoDB transactions**: Uses replica set for ACID transactions in user registration.
- **Response helper extensions**: Centralized HTTP response handling via `util/ResponseExtensions.kt`.

## Developer Workflows
- **Build**: `./gradlew build -x test`
- **Seed DB**: `./gradlew runSeed` (populates MongoDB with test data)
- **Run server**: `./gradlew run` (default port 8080)
- **Test API**: Use `./tests/main/test-api-comprehensive.sh` for automated testing or `postman_collection.json` for manual testing
- **Coverage**: Check `api-coverage-analysis.md` for endpoint coverage status (currently 100%)
- **Debug**: See Ktor logs in terminal; check MongoDB for data issues.

## Project-Specific Conventions
- **No comments in Kotlin code** except actionable `// TODO:` (see `.github/instructions/no-comments.instructions.md`)
- **No extra blank lines**; code must be self-documenting.
- **All endpoints under `/api/`** (e.g., `/api/auth/login`, `/api/products`)
- **Multipart registration**: Always expect `data` (JSON) + `photo`, `nidFront`, `nidBack` files.
- **Password hashing**: PBKDF2WithHmacSHA256, 120,000 iterations, 32-byte salt, 256-bit output, hex encoding.
- **Error handling**: All API responses use `BasicApiResponse`; never leak sensitive info.

## Integration Points
- **KMP mobile**: See `.github/instructions/kmp-integration.instructions.md` for shared models and API usage.
- **Postman**: Collection covers all endpoints; keep in sync with backend and update instructions as needed.
- **Seeding**: Test data is realistic and covers all flows (see `.github/instructions/project-summary.instructions.md`).
- **API Testing**: Comprehensive test automation with coverage analysis (see `.github/instructions/api-coverage-analysis.md`).

## Key Files & Directories
- `src/main/kotlin/Application.kt`: Ktor entry point
- `src/main/kotlin/Routing.kt`: Route configuration
- `src/main/kotlin/service/`: Business logic organized by domain (auth/, mom/, doctor/, mom/ecommerce/)
- `src/main/kotlin/data/models/`: Data models (User, Mom, Doctor, Product, etc.)
- `src/main/kotlin/util/`: Utilities (hashing, file upload, seeder)
- `.github/instructions/`: All project documentation and conventions
- `postman_collection.json`: API test collection (keep updated)

## Examples
- **Register Mom**: POST `/api/auth/register/mom` (multipart, see Postman example)
- **Login**: POST `/api/auth/login` (JSON, returns JWT)
- **Add to Cart**: POST `/api/cart/add` (requires JWT)

## When in Doubt
- Reference `.github/instructions/main.instructions.md` for API and data model details.
- Use seeded accounts for testing (see project summary).
- Always update documentation and Postman collection after backend changes.

