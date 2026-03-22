# Stories Acceptance Criteria

## Overview
This document defines the acceptance criteria for user stories and features in the Mom Care Platform Backend. Each story must meet these criteria before being considered complete.

## Mom Registration & Profile Enhancement

### Story: Step-by-Step Mom Registration Flow with KYC
**As a** mom  
**I want** to complete my registration through guided steps with KYC questions  
**So that** I can be matched with appropriate circles and access platform features

#### Registration Steps Flow:
1. **Step 1: Basic Information** (Required to proceed)
   - [ ] Full name
   - [ ] Email verification
   - [ ] Phone number verification
   - [ ] Basic profile photo

2. **Step 2: Location & Demographics** (Required to proceed)
   - [ ] Current address
   - [ ] City and district
   - [ ] GPS coordinates (auto-detected)
   - [ ] Age and date of birth
   - [ ] Family status (single mom, married, etc.)

3. **Step 3: Socioeconomic Profile** (Required to proceed)
   - [ ] Car ownership (has car or not)
   - [ ] Car type (if applicable)
   - [ ] Mobile device type (iPhone or Android)
   - [ ] Mobile model
   - [ ] Profession/occupation
   - [ ] Income range (optional)

4. **Step 4: Language & Preferences** (Required to proceed)
   - [ ] Preferred language for communication
   - [ ] Local app language on her phone
   - [ ] Notification preferences
   - [ ] Privacy settings

#### Acceptance Criteria:
- [ ] **Sequential Step Validation**: Cannot proceed to next step without completing current step
- [ ] **Step Progress Tracking**: Track which step mom is currently on
- [ ] **Profile Completion Percentage**: Calculate completion percentage based on completed steps
- [ ] **Resume Registration**: Mom can resume registration from where she left off
- [ ] **Step Validation**: Each step has required fields that must be completed
- [ ] **Data Persistence**: Save progress after each step completion
- [ ] **Step Navigation**: Allow going back to previous steps to modify information
- [ ] **Skip Options**: Allow skipping optional questions within steps
- [ ] **Profile Completion Status**: Track overall profile completion status
- [ ] **Registration Blocking**: Block app functionality until registration is complete

### Story: App Functionality Blocking & Profile Completion
**As a** mom  
**I want** to be guided to complete my profile before accessing full app features  
**So that** I can have a complete profile for better matching and platform functionality

#### Acceptance Criteria:
- [ ] **Profile Completion Check**: Check profile completion status on app launch
- [ ] **Functionality Blocking**: Block access to main features until profile is complete
- [ ] **Light Dashboard Access**: Allow access to limited features (profile completion, basic info)
- [ ] **Completion Banner**: Show prominent banner/notification to complete profile
- [ ] **Progress Indicator**: Show profile completion progress (e.g., "3 of 5 steps completed")
- [ ] **Resume Registration**: Direct mom to resume registration from current step
- [ ] **Skip to Light Mode**: Allow temporary access to light dashboard with completion reminder
- [ ] **Feature Restrictions**: Block circle creation, e-commerce, and advanced features
- [ ] **Completion Rewards**: Show benefits of completing profile (better matching, full access)

#### UI Insights & Requirements:
- [ ] **Step Progress UI**: Visual progress bar showing current step and completion
- [ ] **Step Navigation**: Clear navigation between steps with validation
- [ ] **Completion Status**: Visual indicators for completed vs pending steps
- [ ] **Banner Design**: Prominent banner design for profile completion reminder
- [ ] **Light Dashboard**: Simplified dashboard for incomplete profiles
- [ ] **Step Validation UI**: Clear error messages and validation feedback
- [ ] **Resume Flow**: Smooth transition from incomplete profile to registration steps
- [ ] **Mobile Responsive**: All UI components work on mobile devices

#### Technical Requirements:
- [ ] **Profile Completion API**: Endpoints to check and update completion status
- [ ] **Step Progress API**: Track and retrieve current step progress
- [ ] **Registration Resume API**: Resume registration from specific step
- [ ] **Feature Access Control**: Middleware to check profile completion
- [ ] **UI State Management**: Track UI state for step navigation
- [ ] **Data Models**: Enhanced mom profile with step completion tracking
- [ ] **Database Schema**: Tables for step progress and completion status
- [ ] **Validation Logic**: Step-by-step validation and completion checks

## Circle/Cluster Management System

### Story: Mom Circle Creation & Management
**As a** mom  
**I want** to create or join circles based on location and compatibility  
**So that** I can connect with other moms in my area with similar backgrounds

#### Acceptance Criteria:
- [ ] **Circle Creation**: Mom can initiate a new circle/cluster
- [ ] **Location-based Matching**: Circles are primarily based on location proximity
- [ ] **Smart Matching Algorithm**: Circles are formed based on:
  - [ ] Location (primary criteria)
  - [ ] Age compatibility
  - [ ] Socioeconomic compatibility
  - [ ] Language preferences
  - [ ] Local app language
- [ ] **Existing Circle Assignment**: If a circle exists in the same location, new moms are assigned to existing circles
- [ ] **Circle Size Management**: 
  - [ ] Virtual circles: 1-4 members
  - [ ] Physical circles: 5+ members
- [ ] **Circle Status Tracking**: Track circle status (virtual/physical, active/inactive)
- [ ] **Member Management**: Add/remove members from circles
- [ ] **Circle Discovery**: Moms can discover available circles in their area

#### Technical Requirements:
- [ ] Circle/Cluster data model
- [ ] Matching algorithm implementation
- [ ] Location-based search and filtering
- [ ] Circle management APIs
- [ ] Real-time circle status updates
- [ ] Database indexing for location queries

## Points & Authorization System

### Story: Mom Points System
**As a** mom  
**I want** to earn points by attending circles  
**So that** I can unlock e-commerce features and become authorized

#### Acceptance Criteria:
- [ ] **Points Earning**: Moms earn points for:
  - [ ] Attending circle sessions
  - [ ] Completing circle activities
  - [ ] Providing circle reviews
- [ ] **Authorization Threshold**: 120 points required for `isAuthorized = true`
- [ ] **Points Tracking**: Real-time points balance tracking
- [ ] **Points History**: Complete history of points earned/lost
- [ ] **E-commerce Integration**: Points unlock e-commerce features
- [ ] **Points Display**: Moms can view their current points and progress

#### Technical Requirements:
- [ ] Points data model and tracking
- [ ] Points calculation logic
- [ ] Authorization status updates
- [ ] Points history API
- [ ] Integration with e-commerce features

### Story: Admin Points Management
**As an** admin  
**I want** to manage mom points and apply penalties  
**So that** I can maintain platform quality and discipline

#### Acceptance Criteria:
- [ ] **Points Deduction**: Admin can deduct points for violations
- [ ] **Penalty System**: Configurable penalty amounts for different violations
- [ ] **Points Adjustment**: Admin can manually adjust points (add/remove)
- [ ] **Violation Tracking**: Track reasons for point deductions
- [ ] **Notification System**: Notify moms of point changes
- [ ] **Audit Trail**: Complete audit trail of all point changes

#### Technical Requirements:
- [ ] Admin points management APIs
- [ ] Penalty configuration system
- [ ] Notification system integration
- [ ] Audit logging for point changes
- [ ] Admin dashboard for points management

## Review System

### Story: Circle Review System
**As a** mom  
**I want** to review circles I attended  
**So that** I can provide feedback and earn points for session completion

#### Acceptance Criteria:
- [ ] **Circle Review**: Mom can review circles after attending
- [ ] **Review Requirements**: Review required to gain points for session
- [ ] **Review Criteria**: Rating system for circle quality
- [ ] **Review Text**: Optional text feedback
- [ ] **Review Validation**: Ensure mom actually attended the circle
- [ ] **Points Integration**: Review completion triggers points earning

#### Technical Requirements:
- [ ] Circle review data model
- [ ] Review submission APIs
- [ ] Review validation logic
- [ ] Integration with points system
- [ ] Review display for circles

### Story: Mom-to-Mom Review System
**As a** mom  
**I want** to review other moms I interacted with  
**So that** I can provide feedback on their behavior and participation

#### Acceptance Criteria:
- [ ] **Mom Review**: Mom can review other moms in the same circle
- [ ] **Review Criteria**: Rating system for mom behavior/participation
- [ ] **Review Text**: Optional text feedback
- [ ] **Review Validation**: Ensure moms were in the same circle
- [ ] **Review Privacy**: Reviews are anonymous or identifiable based on settings

#### Technical Requirements:
- [ ] Mom-to-mom review data model
- [ ] Review submission APIs
- [ ] Review validation logic
- [ ] Privacy controls for reviews
- [ ] Review aggregation system

### Story: Public Review Display
**As a** platform user  
**I want** to see reviews for moms and doctors  
**So that** I can make informed decisions about interactions

#### Acceptance Criteria:
- [ ] **Public Reviews**: Display 3 reviews for each mom and doctor
- [ ] **Review Selection**: Show most recent or highest-rated reviews
- [ ] **Review Display**: Show rating and text feedback
- [ ] **Review Privacy**: Respect privacy settings for review display
- [ ] **Review Moderation**: Admin can moderate inappropriate reviews

#### Technical Requirements:
- [ ] Review display APIs
- [ ] Review selection algorithm
- [ ] Privacy controls implementation
- [ ] Review moderation system
- [ ] Public profile integration

## Payment Processing System

### Story: Payment Integration
**As a** mom  
**I want** to make payments for e-commerce purchases  
**So that** I can buy products and services through the platform

#### Acceptance Criteria:
- [ ] **Payment Methods**: Support multiple payment methods (credit card, mobile payment, etc.)
- [ ] **Payment Processing**: Secure payment processing integration
- [ ] **Payment Validation**: Validate payment information
- [ ] **Payment Confirmation**: Confirm successful payments
- [ ] **Payment History**: Track payment history
- [ ] **Refund Processing**: Handle refunds for cancelled orders
- [ ] **Payment Security**: Secure handling of payment information
- [ ] **Payment Notifications**: Notify users of payment status

#### Technical Requirements:
- [ ] Payment gateway integration
- [ ] Payment data models
- [ ] Payment processing APIs
- [ ] Security compliance (PCI DSS)
- [ ] Payment notification system
- [ ] Refund processing system

## Data Models & Database Schema

### New Data Models Required:
- [ ] **EnhancedMomProfile**: Location, age, socioeconomic info, language preferences
- [ ] **MomRegistrationStep**: Track completion status for each registration step
- [ ] **MomProfileCompletion**: Overall profile completion status and progress
- [ ] **RegistrationStepData**: Store data for each step (basic info, location, socioeconomic, etc.)
- [ ] **Circle**: Circle/cluster information, location, status, members
- [ ] **CircleMember**: Mom-circle relationship with join date, status
- [ ] **Points**: Points tracking, history, balance
- [ ] **PointsTransaction**: Individual point transactions with reasons
- [ ] **CircleReview**: Reviews for circles
- [ ] **MomReview**: Reviews between moms
- [ ] **Payment**: Payment information and history
- [ ] **PaymentMethod**: Stored payment methods for moms

### Database Indexes Required:
- [ ] Location-based indexes for circle matching
- [ ] Points balance indexes for quick lookups
- [ ] Review indexes for efficient querying
- [ ] Payment history indexes

## API Endpoints Required

### Mom Profile Enhancement & Step-by-Step Registration:
- [ ] `GET /api/moms/registration/status` - Get current registration step and completion status
- [ ] `POST /api/moms/registration/step/{stepNumber}` - Submit data for specific registration step
- [ ] `GET /api/moms/registration/step/{stepNumber}` - Get data for specific registration step
- [ ] `PUT /api/moms/registration/step/{stepNumber}` - Update data for specific registration step
- [ ] `GET /api/moms/registration/progress` - Get overall registration progress
- [ ] `POST /api/moms/registration/complete` - Mark registration as complete
- [ ] `GET /api/moms/profile/completion` - Get profile completion status
- [ ] `PUT /api/moms/profile/location` - Update location
- [ ] `PUT /api/moms/profile/socioeconomic` - Update socioeconomic info
- [ ] `PUT /api/moms/profile/language` - Update language preferences

### Circle Management:
- [ ] `POST /api/circles` - Create new circle
- [ ] `GET /api/circles/nearby` - Find circles near location
- [ ] `POST /api/circles/{id}/join` - Join existing circle
- [ ] `GET /api/circles/{id}` - Get circle details
- [ ] `PUT /api/circles/{id}/status` - Update circle status
- [ ] `GET /api/circles/my-circles` - Get mom's circles

### Points System:
- [ ] `GET /api/moms/points` - Get mom's points
- [ ] `GET /api/moms/points/history` - Get points history
- [ ] `POST /api/admin/points/adjust` - Admin points adjustment
- [ ] `GET /api/admin/points/transactions` - Admin points audit

### Review System:
- [ ] `POST /api/circles/{id}/review` - Review circle
- [ ] `POST /api/moms/{id}/review` - Review mom
- [ ] `GET /api/moms/{id}/reviews` - Get mom's public reviews
- [ ] `GET /api/doctors/{id}/reviews` - Get doctor's public reviews

### Payment System:
- [ ] `POST /api/payments` - Process payment
- [ ] `GET /api/payments/history` - Get payment history
- [ ] `POST /api/payments/{id}/refund` - Process refund
- [ ] `GET /api/payments/methods` - Get stored payment methods

## Step-by-Step Registration Flow Details

### Registration Step Definitions:

#### Step 1: Basic Information
```json
{
  "stepNumber": 1,
  "stepName": "Basic Information",
  "required": true,
  "fields": [
    {"name": "fullName", "type": "string", "required": true},
    {"name": "email", "type": "email", "required": true, "verified": true},
    {"name": "phoneNumber", "type": "string", "required": true, "verified": true},
    {"name": "profilePhoto", "type": "file", "required": true}
  ],
  "validation": {
    "email": "Must be unique and verified",
    "phoneNumber": "Must be unique and verified",
    "profilePhoto": "Must be valid image file"
  }
}
```

#### Step 2: Location & Demographics
```json
{
  "stepNumber": 2,
  "stepName": "Location & Demographics",
  "required": true,
  "fields": [
    {"name": "address", "type": "string", "required": true},
    {"name": "city", "type": "string", "required": true},
    {"name": "district", "type": "string", "required": true},
    {"name": "coordinates", "type": "object", "required": true, "autoDetected": true},
    {"name": "dateOfBirth", "type": "date", "required": true},
    {"name": "familyStatus", "type": "enum", "required": true, "options": ["single", "married", "divorced", "widowed"]}
  ],
  "validation": {
    "coordinates": "Must be valid GPS coordinates",
    "dateOfBirth": "Must be valid date, age >= 18"
  }
}
```

#### Step 3: Socioeconomic Profile
```json
{
  "stepNumber": 3,
  "stepName": "Socioeconomic Profile",
  "required": true,
  "fields": [
    {"name": "hasCar", "type": "boolean", "required": true},
    {"name": "carType", "type": "string", "required": false, "conditional": "hasCar == true"},
    {"name": "mobileDevice", "type": "enum", "required": true, "options": ["iPhone", "Android"]},
    {"name": "mobileModel", "type": "string", "required": true},
    {"name": "profession", "type": "string", "required": true},
    {"name": "incomeRange", "type": "enum", "required": false, "options": ["low", "medium", "high"]}
  ]
}
```

#### Step 4: Language & Preferences
```json
{
  "stepNumber": 4,
  "stepName": "Language & Preferences",
  "required": true,
  "fields": [
    {"name": "preferredLanguage", "type": "enum", "required": true, "options": ["Arabic", "English", "French"]},
    {"name": "appLanguage", "type": "enum", "required": true, "options": ["Arabic", "English", "French"]},
    {"name": "notificationPreferences", "type": "object", "required": true},
    {"name": "privacySettings", "type": "object", "required": true}
  ]
}
```


### Profile Completion Status:
```json
{
  "momId": "string",
  "isRegistrationComplete": false,
  "currentStep": 3,
  "totalSteps": 4,
  "completionPercentage": 75,
  "completedSteps": [1, 2],
  "pendingSteps": [3, 4],
  "lastUpdated": "2025-01-15T10:30:00Z",
  "canAccessFullFeatures": false,
  "canAccessLightDashboard": true
}
```

## Testing Requirements

### Unit Tests:
- [ ] Step-by-step registration validation tests
- [ ] Profile completion calculation tests
- [ ] Circle matching algorithm tests
- [ ] Points calculation tests
- [ ] Review validation tests
- [ ] Payment processing tests
- [ ] Profile completion tests

### Integration Tests:
- [ ] Step-by-step registration flow (all 4 steps)
- [ ] Profile completion status tracking
- [ ] Registration resume functionality
- [ ] App functionality blocking for incomplete profiles
- [ ] Light dashboard access for incomplete profiles
- [ ] Circle creation and joining flow
- [ ] Points earning and deduction flow
- [ ] Review submission and display flow
- [ ] Payment processing flow
- [ ] Profile enhancement flow

### End-to-End Tests:
- [ ] Complete step-by-step mom registration flow (all 4 steps)
- [ ] Registration resume after interruption
- [ ] App functionality blocking until registration complete
- [ ] Light dashboard access with completion banner
- [ ] Circle participation and review flow
- [ ] Points accumulation and authorization flow
- [ ] E-commerce purchase with payment flow

## UI/UX Requirements & Insights

### Registration Flow UI:
- [ ] **Progress Indicator**: Visual progress bar showing "Step X of 4"
- [ ] **Step Navigation**: Clear back/next buttons with validation
- [ ] **Field Validation**: Real-time validation with clear error messages
- [ ] **Skip Options**: Clear skip buttons for optional fields
- [ ] **Auto-save**: Save progress automatically after each field
- [ ] **Mobile Responsive**: Optimized for mobile devices
- [ ] **Accessibility**: Screen reader support and keyboard navigation

### App Functionality Blocking UI:
- [ ] **Completion Banner**: Prominent banner at top of app
- [ ] **Progress Display**: "Complete your profile: 3 of 4 steps done"
- [ ] **Feature Restrictions**: Grayed out or disabled features
- [ ] **Light Dashboard**: Simplified interface for incomplete profiles
- [ ] **Resume Button**: Clear call-to-action to continue registration
- [ ] **Benefits Display**: Show advantages of completing profile

### Profile Completion Status UI:
- [ ] **Completion Percentage**: Visual percentage indicator
- [ ] **Step Status**: Icons showing completed/pending steps
- [ ] **Last Updated**: Timestamp of last profile update
- [ ] **Next Step Preview**: Preview of what's needed for next step

## Security Requirements

### Data Protection:
- [ ] Encrypt sensitive personal information
- [ ] Secure payment data handling
- [ ] Privacy controls for reviews
- [ ] Location data anonymization options
- [ ] Step-by-step data validation and sanitization

### Access Control:
- [ ] Role-based access for admin functions
- [ ] Mom can only access their own data
- [ ] Admin can manage points and reviews
- [ ] Secure payment processing

## Performance Requirements

### Response Times:
- [ ] Circle matching: < 2 seconds
- [ ] Points calculation: < 1 second
- [ ] Review submission: < 1 second
- [ ] Payment processing: < 5 seconds

### Scalability:
- [ ] Support 10,000+ moms
- [ ] Handle 1,000+ concurrent circles
- [ ] Process 100+ payments per minute
- [ ] Efficient location-based queries

## Documentation Requirements

### API Documentation:
- [ ] Complete Swagger/OpenAPI documentation
- [ ] Request/response examples
- [ ] Error response documentation
- [ ] Authentication requirements

### Postman Collection:
- [ ] All new endpoints in Postman
- [ ] Test scenarios for each endpoint
- [ ] Environment variables setup
- [ ] Authentication flow examples

## Deployment Requirements

### Database Migration:
- [ ] Migration scripts for new tables
- [ ] Data migration for existing moms
- [ ] Index creation scripts
- [ ] Rollback procedures

### Configuration:
- [ ] Payment gateway configuration
- [ ] Review moderation settings
- [ ] Points system configuration
- [ ] Circle matching parameters

## Success Criteria

### Functional Success:
- [ ] Moms can complete enhanced registration
- [ ] Circles are created and managed effectively
- [ ] Points system works correctly
- [ ] Review system functions properly
- [ ] Payment processing is secure and reliable

### Technical Success:
- [ ] All tests pass (unit, integration, e2e)
- [ ] Performance requirements met
- [ ] Security requirements satisfied
- [ ] Documentation complete and accurate
- [ ] Code follows established patterns

### Business Success:
- [ ] Mom engagement increases
- [ ] Circle participation improves
- [ ] E-commerce conversion increases
- [ ] Platform quality maintained
- [ ] User satisfaction improves

## Notes
- This acceptance criteria document should be referenced in the current tasks instruction file
- Each story should be implemented incrementally
- Regular review and updates of this document as requirements evolve
- All stories must pass quality gates before completion
- Integration testing is critical for cross-story functionality
