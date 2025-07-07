<template>
  <div class="event-bookings-container">
    <div v-if="isLoadingEventName && !event" class="loading-message">
      <p>Loading event information...</p>
    </div>
    <div v-else-if="event" class="page-header">
      <h1>Bookings for: {{ event.title }}</h1>
      <router-link :to="{ name: 'event-detail', params: { id: eventId } }" class="back-to-event-link">
        &laquo; Back to Event Details
      </router-link>
    </div>

    <div v-if="authError" class="error-message">
      <p>{{ authError }}</p>
      <p v-if="!isLoggedIn">Please <router-link to="/login">login</router-link> to view bookings.</p>
    </div>

    <div v-if="isLoggedIn && !authError">
      <div v-if="isLoadingBookings" class="loading-message">
        <p>Loading bookings...</p>
      </div>
      <div v-if="!isLoadingBookings && bookingsError" class="error-message">
        <p>{{ bookingsError }}</p>
      </div>

      <p v-if="cancelError" class="error-message small-error">{{ cancelError }}</p>
      <p v-if="cancelSuccess" class="success-message small-success">{{ cancelSuccess }}</p>

      <div v-if="!isLoadingBookings && !bookingsError && (!eventBookings || eventBookings.length === 0)" class="info-message">
        <p>No bookings have been made for this event yet.</p>
      </div>
      <div v-if="!isLoadingBookings && !bookingsError && eventBookings && eventBookings.length > 0" class="bookings-list-container">
        <ul class="bookings-list">
          <li v-for="booking in eventBookings" :key="booking.id" class="booking-item">
            <p><strong>Booking ID:</strong> {{ booking.id }}</p>
            <p><strong>Booked by:</strong> {{ booking.fullName }} ({{ booking.email }})</p>
            <p><strong>User ID:</strong> {{ booking.userID }}</p>
            <p><strong>Status:</strong> <span :class="`status-${String(booking.status || '').toLowerCase()}`">{{ booking.status || 'N/A' }}</span></p>
            <p><strong>Price Paid:</strong> {{ formatPrice(booking.price) }}</p>
            <p><strong>Booked on:</strong> {{ formatDate(booking.createdAt) }}</p>
            <div class="booking-actions" v-if="isAdmin && booking.status !== 'CANCELLED'">
              <button
                @click="handleAdminCancelBooking(booking.id)"
                :disabled="cancellingBookingId === booking.id"
                class="btn-admin-cancel"
              >
                {{ cancellingBookingId === booking.id ? 'Cancelling...' : 'Cancel Booking' }}
              </button>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import { mapGetters, mapState } from 'vuex';
import { jwtDecode } from 'jwt-decode';

export default {
  name: 'EventBookingsView',
  props: {},
  data() {
    return {
      event: null,
      eventId: this.$route.params.id,
      eventBookings: [],
      isLoadingBookings: false,
      isLoadingEventName: false,
      bookingsError: null,
      authError: null,
      cancellingBookingId: null,
      cancelError: null,
      cancelSuccess: null,
    };
  },
  computed: {
    ...mapState('auth', ['user']),
    ...mapGetters('auth', ['isLoggedIn', 'isOrganizer', 'isAdmin']),
    isCurrentUserOrgOfThisFetchedEvent() {
      if (!this.isLoggedIn || !this.event || !this.event.organizerID || !this.isOrganizer) {
        return false;
      }
      const token = localStorage.getItem('jwtToken');
      if (token) {
        try {
          const decodedToken = jwtDecode(token);
          const currentUserID = decodedToken.userID;
          if (!currentUserID) return false;
          return String(currentUserID).toUpperCase() === String(this.event.organizerID).toUpperCase();
        } catch (e) {
          return false;
        }
      }
      return false;
    }
  },
  methods: {
    async fetchMinimalEventDetails() {
      this.isLoadingEventName = true;
      this.authError = null;
      this.event = null;
      try {
        const response = await apiClient.get(`/events/${this.eventId}`);
        this.event = response.data;

        if (this.isAdmin) {
          await this.fetchEventBookingsForAdminOrOrganizer();
        } else if (this.isLoggedIn && this.isOrganizer && this.isCurrentUserOrgOfThisFetchedEvent) {
          await this.fetchEventBookingsForAdminOrOrganizer();
        } else if (!this.isLoggedIn) {
          this.authError = "You must be logged in to view event bookings.";
        } else if (!this.isOrganizer && !this.isAdmin) {
          this.authError = "You must be an organizer or admin to view event bookings.";
        } else if (this.isOrganizer && this.event && !this.isCurrentUserOrgOfThisFetchedEvent) {
          this.authError = "You are not authorized to view bookings for this specific event.";
        } else {
           this.authError = "Could not verify authorization to view bookings.";
        }

      } catch (err) {
        if (err.response && err.response.status === 404) {
          this.authError = 'Event not found.';
        } else {
          this.authError = 'Could not load event information.';
        }
        this.event = null;
      } finally {
        this.isLoadingEventName = false;
      }
    },

    async fetchEventBookingsForAdminOrOrganizer() {
      this.isLoadingBookings = true;
      this.bookingsError = null;
      this.eventBookings = [];

      const token = localStorage.getItem('jwtToken');
      if (!token) {
        this.bookingsError = "Authentication token not found. Please log in again.";
        this.isLoadingBookings = false;
        return;
      }
      const config = {
        headers: { Authorization: `Bearer ${token}` },
      };

      try {
        const response = await apiClient.get(`/events/${this.eventId}/bookings`, config);
        if (response && Array.isArray(response.data)) {
          this.eventBookings = response.data;
        } else {
          this.eventBookings = [];
          this.bookingsError = "Received unexpected data format for bookings.";
        }
      } catch (err) {
        if (err.response) {
          this.bookingsError = `Failed to load bookings: ${err.response.data?.message || err.response.data?.error || err.response.statusText || 'Server error'}`;
            if(err.response.status === 403) this.bookingsError = 'You are not authorized to view these bookings (API denied).';
            if(err.response.status === 401) this.bookingsError = 'Authentication failed for fetching bookings.';
        } else {
          this.bookingsError = 'An unexpected error occurred while fetching event bookings.';
        }
      } finally {
        this.isLoadingBookings = false;
      }
    },

    async handleAdminCancelBooking(bookingId) {
      if (!confirm('Are you sure you want to cancel this booking as an admin?')) {
        return;
      }

      this.cancellingBookingId = bookingId;
      this.cancelError = null;
      this.cancelSuccess = null;

      const token = localStorage.getItem('jwtToken');
      if (!token) {
        this.cancelError = "Authentication token not found. Please log in again.";
        this.cancellingBookingId = null;
        return;
      }
      const config = {
        headers: { Authorization: `Bearer ${token}` },
      };

      try {
        const response = await apiClient.put(`/bookings/${bookingId}/cancel`, {}, config);
        const bookingIndex = this.eventBookings.findIndex(b => b.id === bookingId);
        if (bookingIndex !== -1) {
          this.eventBookings[bookingIndex].status = response.data.status || 'CANCELLED';
        }
        this.cancelSuccess = `Booking ${bookingId} has been successfully processed. New status: ${response.data.status || 'CANCELLED'}.`;
      } catch (err) {
        console.error(`Admin failed to cancel booking ${bookingId}:`, err);
        this.cancelError = err.response?.data?.message || err.message || 'Could not cancel booking.';
      } finally {
        this.cancellingBookingId = null;
        setTimeout(() => {
          this.cancelError = null;
          this.cancelSuccess = null;
        }, 7000);
      }
    },

    formatDate(dateTimeString) {
      if (!dateTimeString) return 'N/A';
      try {
        const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: true };
        return new Date(dateTimeString).toLocaleString(undefined, options);
      } catch (e) {
        return "Invalid Date";
      }
    },
    formatPrice(price) {
      if (price === null || price === undefined || price === '') return 'N/A';
      const numPrice = Number(price);
      return isNaN(numPrice) ? 'Invalid Price' : `$${numPrice.toFixed(2)}`;
    }
  },
  created() {
    if (!this.eventId) {
      this.authError = "Event ID is missing from the route.";
      return;
    }
    if (!this.isLoggedIn) {
        this.authError = "Please login to view event bookings.";
        return;
    }
    if (!this.isOrganizer && !this.isAdmin) {
        this.authError = "You must be an organizer or an admin to access this page.";
        return;
    }
    this.fetchMinimalEventDetails();
  },
    watch: {
    '$route.params.id': {
      immediate: false,
      handler(newId, oldId) {
        if (newId && newId !== oldId) {
          this.eventId = newId;
          this.event = null;
          this.eventBookings = [];
          this.authError = null;
          this.bookingsError = null;
          this.cancelError = null;
          this.cancelSuccess = null;
          if (!this.isLoggedIn) {
              this.authError = "Please login to view event bookings.";
              return;
          }
          if (!this.isOrganizer && !this.isAdmin) {
              this.authError = "You must be an organizer or an admin to access this page.";
              return;
          }
          this.fetchMinimalEventDetails();
        }
      }
    },
    isLoggedIn(newVal) {
        if (newVal && this.eventId) {
             if (!this.isOrganizer && !this.isAdmin) {
                this.authError = "You must be an organizer or an admin to access this page.";
                this.eventBookings = [];
            } else {
                this.authError = null;
                this.fetchMinimalEventDetails();
            }
        } else if (!newVal) {
            this.authError = "Please login to view event bookings.";
            this.event = null;
            this.eventBookings = [];
        }
    }
  }
};
</script>

<style scoped>
.event-bookings-container {
  padding: 20px;
  max-width: 900px;
  margin: 20px auto;
  font-family: 'Arial', sans-serif;
}

.page-header {
  text-align: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}
.page-header h1 {
  font-size: 2em;
  color: #333;
  margin: 0 0 10px 0;
}
.back-to-event-link {
  color: #007bff;
  text-decoration: none;
  font-size: 0.95em;
}
.back-to-event-link:hover {
  text-decoration: underline;
}

.loading-message,
.error-message,
.info-message {
  text-align: center;
  padding: 20px;
  font-size: 1.2em;
  border-radius: 5px;
  margin: 20px auto;
  max-width: 600px;
}
.info-message { color: #4F8A10; background-color: #DFF2BF; border: 1px solid #4F8A10; }
.error-message { color: #D8000C; background-color: #FFD2D2; border: 1px solid #D8000C; }
.loading-message { color: #00529B; background-color: #BDE5F8; border: 1px solid #00529B; }
.success-message { color: #28a745; background-color: #e9f7ef; border: 1px solid #28a745; text-align: center; padding: 15px; margin-bottom: 20px; border-radius: 4px; font-size: 1.1em;}
.small-error, .small-success { font-size: 0.95em; padding: 10px; margin-top: 10px; margin-bottom:10px; }


.bookings-list-container {
    margin-top: 20px;
}

.bookings-list {
  list-style: none;
  padding: 0;
}

.booking-item {
  background-color: #fdfdfd;
  border: 1px solid #e9e9e9;
  border-radius: 6px;
  padding: 18px;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.04);
  transition: box-shadow 0.2s ease-in-out;
}
.booking-item:hover {
    box-shadow: 0 4px 8px rgba(0,0,0,0.08);
}

.booking-item p {
  margin: 6px 0;
  font-size: 0.95em;
  color: #555;
  line-height: 1.5;
}

.booking-item p strong {
  color: #333;
  min-width: 100px;
  display: inline-block;
}

.booking-actions {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.btn-admin-cancel {
  padding: 8px 15px;
  background-color: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s ease;
}
.btn-admin-cancel:hover:not(:disabled) {
  background-color: #c0392b;
}
.btn-admin-cancel:disabled {
  background-color: #BDC3C7;
  cursor: not-allowed;
}

.status-confirmed { color: #27ae60; font-weight: bold; }
.status-pending { color: #f39c12; font-weight: bold; }
.status-cancelled, .status-rejected { color: #e74c3c; font-weight: bold; }
</style>