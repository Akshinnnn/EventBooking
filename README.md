# The Online Event Booking Platform

## Resources

### User Service

| URI | HTTP Verb | Description |
|-----|-----------|-------------|
| /auth/authenticate | GET | Authenticate user based on token |
| /auth/login | POST | Check credentials based on email and phone |
| /auth/register | POST | Create a user |
| /users | GET | Retrieve all users |
| /users/{id} | GET | Retrieve a user |
| /account | GET | Get account details |
| /account | PUT | Update account details |

### Event Service

| URI | HTTP Verb | Description |
|-----|-----------|-------------|
| /events | GET | Retrieve all events (filter by organizer and/or category with query parameters) |
| /events | POST | Create an event |
| /events/{id} | GET | Retrieve an event |
| /events/{id} | PUT | Update an event |
| /events/{id}/cancel | PUT | Cancel an event |

### Booking Service

| URI | HTTP Verb | Description |
|-----|-----------|-------------|
| /account/bookings | GET | Get all bookings for a user |
| /account/bookings | POST | Create a booking |
| /events/{eventID}/bookings | GET | Get all bookings for an event |
| /bookings/{id} | GET | Retrieve a booking |
| /bookings/{id}/cancel | PUT | Cancel a booking |

### Payment Service

| URI | HTTP Verb | Description |
|-----|-----------|-------------|
| /account/payments | GET | Retrieve all payments for a user |
| /account/balance | POST | Increase user balance |
| /account/balance | GET | Retrieve user’s balance |

### Review Service

| URI | HTTP Verb | Description |
|-----|-----------|-------------|
| /account/reviews | GET | Retrieve all reviews (filter by event with query params) |
| /account/reviews | POST | Create a review |
| /reviews/{id} | DELETE | Delete a review |
| /events/{eventID}/reviews | GET | Retrieve reviews for the event |
| /events/{eventID}/reviews | POST | Create a review for the event |

### Notification Service

| URI | HTTP Verb | Description |
|-----|-----------|-------------|
| /account/notifications | GET | Retrieve notifications for the user |

---

## Events

| Topic Name | Producer | Consumer(s) | Object Exchanged |
|------------|----------|-------------|------------------|
| BookingCreated | BookingService | NotificationService, PaymentService | BookingDTO |
| BookingCancelled | BookingService | PaymentService, NotificationService | BookingDTO |
| PaymentCreated | PaymentService | BookingService, NotificationService | PaymentDTO |
| EventUpdated | EventService | NotificationService | EventDTO |
| EventCancelled | EventService | BookingService, NotificationService | EventDTO |
| ReviewCreated | ReviewService | NotificationService | ReviewDTO |

---

## Frontend Components / Views

| View/Component | Description | HTTP req + Resources (URI) |
|----------------|-------------|-----------------------------|
| HomeView.vue | Displays the main landing page or general information. | Static content |
| EventListView.vue | List of all available events. Filterable by category or organizer. | GET: `/api/events` |
| EventDetailView.vue | Details of a specific event. Booking, reviewing, or managing based on role. | GET: `/api/events/:id`<br>POST: `/api/events/:id/bookings`<br>PUT: `/api/events/:id/cancel`<br>GET: `/api/events/:id/bookings`<br>GET: `/api/events/:id/reviews`<br>POST: `/api/events/:id/reviews` |
| RegisterView.vue | Handles new user registration. | POST: `/api/auth/register` |
| LoginView.vue | Handles user login and token authentication. | POST: `/api/auth/login`<br>GET: `/api/auth/authenticate` |
| EventFormView.vue | Create or modify events. | POST: `/api/events`<br>PUT: `/api/events/:id`<br>GET: `/api/events/:id` |
| ProfileView.vue | View or update profile. Add balance. | PUT: `/api/account`<br>GET: `/api/account`<br>POST: `/api/account/balance` |
| BookingListView.vue | List of all user bookings. Cancel bookings. | GET: `/api/account/bookings`<br>PUT: `/api/account/bookings/{id}/cancel` |
| PaymentListView.vue | List of all payments. | GET: `/api/account/payments` |
| MyReviewsView.vue | List of user reviews. Delete reviews. | GET: `/api/account/reviews`<br>DELETE: `/api/reviews/:id` |
| NotificationListView.vue | List of user notifications. | GET: `/api/account/notifications` |
