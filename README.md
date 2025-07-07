### run project using ``` make run ``` command, check ``` Makefile ``` for other options

# The Online Event Booking Platform

The Online Event Booking Platform is a web application where users can discover events, book tickets, and manage their reservations, while admins and event coordinators have their own roles to manage the platform. Users start by registering or logging in, and once authenticated, they can browse events like concerts, workshops, or conferences, with details such as date, location, price, and ticket availability. They can search or filter events, book tickets, and receive confirmation notifications after a simulated payment process. Users can also view their booking history and details in their accounts.Admins have access to a dashboard where they can manage users, events, and overall platform settings. They can add, update, or remove events, view all bookings. Event coordinators, on the other hand, can create and manage events they are responsible for.

## Diagram of the architecture
![Picture3](https://github.com/user-attachments/assets/bdc926ab-557a-41af-80df-d49596ab668a)

## Simplified version of the diagram
![Picture1](https://github.com/user-attachments/assets/34c1c5ab-58df-425f-817f-2de00cbfa98f)


## DB Diagram
![Picture2](https://github.com/user-attachments/assets/0bd686a4-30c2-44b6-b5c1-c735da0ef93d)


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
