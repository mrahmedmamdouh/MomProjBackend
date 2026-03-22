---
applyTo: '**'
---

# Copilot Instructions — Postman Collection Updates (v2.1)

You are editing a **Postman Collection (schema v2.1)** stored at the project root as `postman_collection.json`.  
Your goal is to **add or modify endpoints, tests, and variables** without breaking the collection or leaking secrets.

## Hard rules
- **Edit only this file** (`postman_collection.json`). Do not touch any other files.

# Postman Collection Maintenance Instructions

## Purpose
This file provides clear guidelines for maintaining the Postman collection (`postman_collection.json`) used for API testing and documentation. The collection must always reflect the current backend implementation.

## Update Guidelines
1. **Sync Endpoints**: Ensure every backend API endpoint is present in the Postman collection. Add, update, or remove endpoints as needed.
2. **Folder Structure**: Organize endpoints into logical folders:
   - Auth
   - Moms
   - Doctors
   - Products
   - Categories
   - Cart
   - Orders
   - Payments
   - Reviews
   - Inventory
   - Sellers
3. **Variables**: Use collection variables for `base_url` (default: `http://localhost:8080`) and `auth_token` (set after login) to simplify environment switching and authentication.
4. **Request Examples**: Each endpoint should include a sample request with realistic data. Use seeded test accounts for authentication (see `project-summary.instructions.md`).
5. **Tests & Scripts**: Add basic tests to validate response status and structure. Use pre-request scripts for authentication if needed.
6. **Documentation**: Add descriptions to each folder and endpoint explaining its purpose and usage. Reference the main API documentation (`main.instructions.md`) for details.
7. **Keep Updated**: After any backend change, immediately update the Postman collection and this instructions file.


## Current Collection Summary

### ✅ Implemented Folders & Endpoints

- **🔐 Authentication** (3 endpoints)
  - Mom Registration (multipart with photos/NID)
  - Doctor Registration (multipart with photos/NID) 
  - Universal Login (returns JWT token)

- **📦 Categories** (5 endpoints)
  - Get All Categories (public)
  - Get Category by ID (public)
  - Create Category (auth required)
  - Update Category (auth required)
  - Delete Category (auth required)

- **📁 File Uploads** (1 endpoint)
  - Access Uploaded File (static file serving)

### ⏳ Planned But Not Implemented

The following endpoints are documented in `main.instructions.md` but **NOT yet implemented**:
- Products, SKUs, Offers
- Mom/Doctor profiles
- Cart management
- Orders & payments
- Reviews & ratings
- Inventory management
- Seller management

### Variables
- `base_url`: Default `http://localhost:8080`
- `auth_token`: Automatically set after successful login

### Test Data
- **Seeded Mom Accounts**: alice@example.com, beth@example.com (password: password123)
- **Seeded Doctor Accounts**: dr.smith@example.com, dr.johnson@example.com, etc. (password: password123)
- **Categories**: Mother Care, Fitness, Nutrition (seeded in database)

### Testing Features
- **Auto Token Management**: Login endpoint automatically sets auth_token variable
- **File Upload Examples**: Multipart registration with realistic form data
- **Error Handling**: Proper validation and error response examples
- **Authentication Flow**: Complete registration → login → authenticated requests

### Current Limitations
⚠️ **Important**: This collection reflects the **actual current implementation** only. Many endpoints listed in the main API documentation are **planned features** that haven't been implemented yet.

### Maintenance Checklist
1. After backend changes, update endpoints and folders in the collection.
2. Ensure all request examples use valid, realistic data.
3. Add/Update tests and scripts as needed.
4. Keep this instructions file in sync with the collection.
5. Reference main API docs for endpoint details.

## Reference Files
- `main.instructions.md`: API documentation
- `project-summary.instructions.md`: Test data and usage examples
- `tasks.instructions.md`: Development roadmap

## Last Updated
- September 2025
