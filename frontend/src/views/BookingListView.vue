<template>
  <div class="booking-list-container">
    <h2>My Bookings</h2>

    <div v-if="isLoading" class="loading-message">Loading your bookings...</div>
    <div v-if="error" class="error-message">
      <p>Could not load bookings: {{ error }}</p>
    </div>

    <div v-if="!isLoading && !error && bookings.length === 0" class="info-message">
      You haven't booked any events yet. <router-link to="/events">Explore events</router-link>!
    </div>

    <div v-if="!isLoading && !error && bookings.length > 0" class="bookings-list">
      <BookingCard
        v-for="booking in bookings"
        :key="booking.id"
        :booking="booking"
        :is-cancelling="cancellingId === booking.id"
        @cancel-booking="handleCancelBooking"
      />
    </div>

    <p v-if="cancelError" class="error-message small-error">Cancellation failed: {{ cancelError }}</p>
    <p v-if="cancelSuccess" class="success-message small-success">{{ cancelSuccess }}</p>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import { mapGetters, mapState } from 'vuex';
import BookingCard from '@/components/BookingCard.vue';

export default {
  name: 'BookingListView', 
  components: {
    BookingCard, 
  },
  data() {
    return {
      bookings: [],
      isLoading: false,
      error: null,
      cancellingId: null, 
      cancelError: null,
      cancelSuccess: null,
    };
  },
  computed: {
    ...mapState('auth', ['user']),
    ...mapGetters('auth', ['isLoggedIn']),
  },
  methods: {

    async fetchBookings() {
      if (!this.isLoggedIn) {
        this.error = "You must be logged in to view your bookings.";
        return;
      }
      this.isLoading = true;
      this.error = null;
      this.bookings = [];

      try {
        const response = await apiClient.get('/account/bookings');
        this.bookings = response.data;
      } catch (err) {
        console.error('Failed to fetch bookings:', err);
        this.error = err.response?.data?.message || err.message || 'An unknown error occurred.';
      } finally {
        this.isLoading = false;
      }
    },
    async handleCancelBooking(bookingId) { 
      if (!bookingId) return;

      const bookingToCancel = this.bookings.find(b => b.id === bookingId);
      if (bookingToCancel && !['PENDING', 'CONFIRMED'].includes(bookingToCancel.status)) {
          this.cancelError = "This booking can no longer be cancelled.";
           setTimeout(() => { this.cancelError = null; }, 5000);
          return;
      }

      if (!confirm('Are you sure you want to cancel this booking?')) {
        return;
      }

      this.cancellingId = bookingId;
      this.cancelError = null;
      this.cancelSuccess = null;

      try {
        const response = await apiClient.put(`/bookings/${bookingId}/cancel`);

        const index = this.bookings.findIndex(b => b.id === bookingId);
        if (index !== -1 && response.data) {
          // Update the status of the booking in the local array
          // This will reactively update the BookingCard component thrpugh its prop
          this.bookings[index].status = response.data.status || 'CANCELLED';
          this.cancelSuccess = `Booking ${bookingId} cancelled successfully.`;
        } else {
          // If for some reason the booking isnt found or response is unexpected, refresh list
          this.fetchBookings();
          this.cancelSuccess = `Booking ${bookingId} cancelled. List refreshed.`;
        }
      } catch (err) {
        console.error(`Failed to cancel booking ${bookingId}:`, err);
        this.cancelError = err.response?.data?.message || err.message || 'Could not cancel booking.';
        this.cancelSuccess = null; // Clear any success message
      } finally {
        this.cancellingId = null; // Reset cancellingId
        // Clear messages after a delay
        setTimeout(() => {
          this.cancelError = null;
          this.cancelSuccess = null;
        }, 5000);
      }
    },
  },
  created() {
    if (this.isLoggedIn) {
      this.fetchBookings();
    } else {
      // Redirect to login, preserving the current path for redirection after login
      this.$router.push({ name: 'login', query: { redirect: this.$route.fullPath } });
    }
  },
  watch: {
    isLoggedIn(newVal, oldVal) {
      if (newVal && !oldVal) { 
        this.fetchBookings();
      } else if (!newVal) { 
        this.bookings = []; 
        this.error = null; 
        // Redirect to login if not already on a public page or login page itself
        if (this.$route.name !== 'login' && this.$route.meta?.requiresAuth !== false) { // Check if route is not already login and isnt explicitly public
             this.$router.push({ name: 'login', query: { redirect: this.$route.fullPath } });
        }
      }
    }
  }
};
</script>

<style scoped>
.booking-list-container {
  max-width: 900px;
  margin: 20px auto;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
}

.loading-message, .error-message, .info-message, .success-message {
  text-align: center;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 4px;
  font-size: 1.1em;
}

.loading-message { color: #00529B; background-color: #BDE5F8; border: 1px solid #00529B; }
.error-message { color: #D8000C; background-color: #FFD2D2; border: 1px solid #D8000C; }
.info-message { color: #4F8A10; background-color: #DFF2BF; border: 1px solid #4F8A10; }
.success-message { color: #28a745; background-color: #e9f7ef; border: 1px solid #28a745; }
.small-error, .small-success { font-size: 0.9em; padding: 8px; margin-top: 15px; }

@media (max-width: 600px) {
  .booking-list-container {
    padding: 10px;
  }
}
</style>