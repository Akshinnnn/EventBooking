import { createRouter, createWebHistory } from 'vue-router';
import HomeView from '../views/HomeView.vue';
import EventListView from '../views/EventListView.vue';
import EventDetailView from '../views/EventDetailView.vue';
import RegisterView from '../views/RegisterView.vue';
import LoginView from '../views/LoginView.vue';
import EventFormView from '../views/EventFormView.vue';
import UserProfileView from '../views/ProfileView.vue';
import BookingListView from '../views/BookingListView.vue';
import PaymentListView from '../views/PaymentListView.vue';
import store from '../store'; 
import EventBookingView from '@/views/EventBookingView.vue';
import UserListView from '../views/UserListView.vue';
import NotificationListView from '@/views/NotificationListView.vue';
import ReviewListView from '@/views/ReviewListView.vue';
import CategoryListView from '../views/CategoryListView.vue';
import CategoryFormView from '../views/CategoryFormView.vue';

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/events',
    name: 'event-list',
    component: EventListView
  },
  {
    path: '/events/:id',
    name: 'event-detail',
    component: EventDetailView,
    props: true, // Make sure ID from path is passed as prop
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { guest: true } // For guest only routes
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guest: true } // For guest only routes
  },
  {
    path: '/events/new',
    name: 'event-create',
    component: EventFormView,
    meta: { requiresAuth: true, roles: ['ORGANIZER', 'ADMIN'] }
  },
  {
    path: '/events/edit/:id',
    name: 'event-edit',
    component: EventFormView,
    props: true,
    meta: { requiresAuth: true, roles: ['ORGANIZER', 'ADMIN'] }
  },
  {
    path: '/events/:id/bookings', 
    name: 'event-bookings',
    component: EventBookingView, 
    props: true,
    meta: { requiresAuth: true, roles: ['ORGANIZER', 'ADMIN'] }
  },
  {
    path: '/profile',
    name: 'user-profile',
    component: UserProfileView,
    meta: { requiresAuth: true } // All logged-in users can see their profile
  },
  {
    path: '/my-bookings',
    name: 'booking-list',
    component: BookingListView,
    meta: { requiresAuth: true, roles: ['PARTICIPANT', 'ORGANIZER', 'ADMIN'] }
  },
  {
    path: '/my-payments',
    name: 'payment-list',
    component: PaymentListView,
    meta: { requiresAuth: true, roles: ['PARTICIPANT', 'ORGANIZER', 'ADMIN'] } 
  },
  {
    path: '/admin/users',
    name: 'admin-user-list',
    component: UserListView,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/notifications',
    name: 'notifications',
    component: NotificationListView,
    meta: { requiresAuth: true }
  },
  {
    path: '/my-reviews',
    name: 'my-reviews',
    component: ReviewListView,
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/categories',
    name: 'categories-list',
    component: CategoryListView,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/categories/new',
    name: 'category-create',
    component: CategoryFormView,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/categories/edit/:categoryId', 
    name: 'category-edit',
    component: CategoryFormView,
    props: true, 
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

router.beforeEach(async (to, from, next) => { // Made async to await tryAutoLogin if needed
  // Ensure auto-login attempt is complete before checking auth status
  // This is often handled by having tryAutoLogin dispatch an action that sets a "loading" state,
  // or by ensuring it resolves before the app mounts and router is fully active.
  // For simplicity, let's assume tryAutoLogin in main.js has run or is running.

  const isLoggedIn = store.getters['auth/isLoggedIn'];
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const guestOnly = to.matched.some(record => record.meta.guest);
  const requiredRoles = to.meta.roles;

  if (requiresAuth && !isLoggedIn) {
    console.log(`Router Guard: '${to.path}' requires auth. User NOT logged in. Redirecting to login.`);
    next({ name: 'login', query: { redirect: to.fullPath } });
  } else if (guestOnly && isLoggedIn) {
    console.log(`Router Guard: '${to.path}' is guest-only. User IS logged in. Redirecting to home.`);
    next({ name: 'home' }); // Or 'user-profile'
  } else if (requiresAuth && isLoggedIn && requiredRoles && requiredRoles.length > 0) {
    const userRoles = store.getters['auth/userRoles'];
    const hasRequiredRole = requiredRoles.some(role => userRoles.includes(role));

    if (hasRequiredRole) {
      console.log(`Router Guard: User has required role for '${to.path}'. Proceeding.`);
      next();
    } else {
      console.warn(`Router Guard: User does NOT have required role for '${to.path}'. User roles: ${userRoles}. Required: ${requiredRoles}. Redirecting to home.`);
      next({ name: 'home' }); // Or a dedicated "Access Denied" page
    }
  } else {
    console.log(`Router Guard: No specific auth/role conditions or conditions met for '${to.path}'. Proceeding.`);
    next();
  }
});

export default router;