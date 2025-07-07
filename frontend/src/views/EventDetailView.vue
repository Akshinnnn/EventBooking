<template>
  <div class="event-detail-page-container">
    <div class="main-content-area">
      <div class="event-detail-container">
        <div v-if="isLoading && !event" class="loading-message">
          <p>Loading event details...</p>
        </div>

        <div v-if="error" class="error-message">
          <p>Sorry, we couldn't fetch the event details.</p>
          <p class="error-details"><i>{{ error }}</i></p>
        </div>

        <div v-if="!isLoading && !error && !event" class="not-found-message">
          <p>Event not found.</p>
          <router-link to="/events" class="back-link">Back to Events List</router-link>
        </div>

        <div v-if="!isLoading && !error && event" class="event-details-card">
          <h1 class="event-title">{{ event.title }}</h1>

          <div class="event-info">
            <p><strong>Category:</strong> {{ formatCategory(event.categoryName || event.category) }}</p>
            <p>
              <strong>Date:</strong> {{ formatDate(event.startDateTime) }}
              <span v-if="event.endDateTime"> - {{ formatDate(event.endDateTime) }}</span>
            </p>
            <p><strong>Location:</strong> {{ event.city }}, {{ event.country }}</p>
            <p><strong>Price:</strong> {{ formatPrice(event.price) }}</p>
            <p v-if="event.capacity && event.capacity > 0"><strong>Capacity:</strong> {{ event.capacity }}</p>
            <p><strong>Status:</strong> <span :class="`status-${String(event.status || '').toLowerCase()}`">{{ event.status || 'N/A' }}</span></p>
            <p><strong>Description:</strong> {{ event.description || 'No description available.' }}</p>
          </div>

          <p v-if="cancellError" class="error-message small-error">{{ cancellError }}</p>
          <p v-if="cancelSuccess" class="success-message small-success">{{ cancelSuccess }}</p>

          <div v-if="bookingCancellationErrors.length > 0" class="error-message small-error">
            <p>Could not cancel some bookings:</p>
            <ul>
              <li v-for="(err, index) in bookingCancellationErrors" :key="'cancel-err-' + index">{{ err }}</li>
            </ul>
          </div>
            <p v-if="bookingCancellationSuccessCount > 0 && event.status === 'CANCELLED'" class="success-message small-success">
            Successfully processed {{ bookingCancellationSuccessCount }} booking(s) for cancellation.
          </p>

          <div v-if="((isAdmin || isOrganizer) && isCurrentUserOrgOfThisEvent && !isEventExpired)" class="event-actions">
            <button @click="handleCancelEventAndBookings" type="button" :disabled="isCancelling || event.status === 'CANCELLED'" class="action-button cancel-button">
              {{ isCancelling ? 'Processing...' : (event.status === 'CANCELLED' ? 'Event Cancelled' : 'Cancel Event & Bookings') }}
            </button>
            <router-link
              v-if="event && event.id && event.status !== 'CANCELLED'"
              :to="{ name: 'event-edit', params: { id: event.id } }"
              class="action-button update-link">
              Update Event
            </router-link>
            <router-link
              v-if="event && event.id"
              :to="{ name: 'event-bookings', params: { id: event.id } }"
              class="action-button view-bookings-link">
              View Event Bookings
            </router-link>
          </div>

          <div class="event-actions" v-if="event && isEventExpired">
             <button @click="toggleReviewsVisibility" class="action-button toggle-reviews-button">
                {{ showReviewsSection ? 'Hide Reviews' : 'Show Reviews' }} ({{ eventReviews.length }})
            </button>
          </div>

        </div>
      </div>

      <div v-if="event && isEventExpired && showReviewsSection" class="reviews-section-container">
        <div class="reviews-list-card">
          <h2 class="reviews-title">Event Reviews</h2>

          <div v-if="reviewDeleteSuccessMessage" class="success-message small-success">{{ reviewDeleteSuccessMessage }}</div>
          <div v-if="reviewDeleteError" class="error-message small-error">{{ reviewDeleteError }}</div>

          <div v-if="isLoadingReviews" class="loading-message">
            <p>Loading reviews...</p>
          </div>
          <div v-if="reviewsError && !isLoadingReviews" class="error-message small-error">
            <p><i>{{ reviewsError }}</i></p>
          </div>
          <div v-if="!isLoadingReviews && !reviewsError && eventReviews.length === 0" class="no-reviews-message">
            <p>No reviews yet for this event.</p>
          </div>
          <ul v-if="!isLoadingReviews && eventReviews.length > 0" class="reviews-list">
            <li v-for="review in sortedEventReviews" :key="review.id" class="review-item">
              <div class="review-content-wrapper">
                <div class="review-header">
                  <strong class="review-author">{{ review.username || 'User ' + String(review.userID || '').substring(0,6) }}</strong>
                  <span class="review-date">{{ review.createdAt ? formatDate(review.createdAt) : 'Recently' }}</span>
                </div>
                <div class="review-rating">Rating: {{ review.rating }}/5</div>
                <h4 class="review-title-display" v-if="review.title">{{ review.title }}</h4>
                <p class="review-comment">{{ review.comment || review.description }}</p>
              </div>
              <div class="review-actions-wrapper" v-if="isLoggedIn && (String(review.userID) === String(currentUserID)) || isAdmin">
                <button @click="confirmDeleteMyReview(review.id)"
                        :disabled="isDeletingReviewId === review.id"
                        class="delete-my-review-button">
                  <span v-if="isDeletingReviewId === review.id">Deleting...</span>
                  <span v-else>Delete My Review</span>
                </button>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <div v-if="isLoggedIn && event && event.status !== 'CANCELLED' && !isCurrentUserOrgOfThisEvent && !isEventExpired" class="form-container booking-form-container">
      <form @submit.prevent="handleBookEvent">
        <h1>Booking</h1>
        <div class="form-group">
          <label for="name">Full Name:</label>
          <input type="text" id="name" name="name" v-model="bookingData.fullName" required />
        </div>
        <div class="form-group">
          <label for="email">Email:</label>
          <input type="email" id="email" name="email" v-model="bookingData.email" required />
        </div>
        <p v-if="bookingError" class="error-message small-error">{{ bookingError }}</p>
        <p v-if="bookingSuccess" class="success-message small-success">{{ bookingSuccess }}</p>
        <div class="event-actions-form">
          <button type="submit" class="action-button book-button" :disabled="isBooking">
            {{ isBooking ? 'Booking...' : 'Book Event' }}
          </button>
        </div>
      </form>
    </div>
      <div v-if="isLoggedIn && event && event.status === 'CANCELLED' && !isCurrentUserOrgOfThisEvent && !isEventExpired" class="form-container">
        <p class="error-message">This event has been cancelled and is no longer available for booking.</p>
    </div>
      <div v-if="isLoggedIn && event && event.status !== 'CANCELLED' && !isCurrentUserOrgOfThisEvent && isEventExpired && !showReviewsSection" class="form-container info-message small-info">
        <p>This event has already passed. You may be able to leave a review if you attended.</p>
    </div>

    <div v-if="!isLoggedIn && event && event.status !== 'CANCELLED' && !isEventExpired" class="form-container login-prompt-container">
      <p class="login-link">
        Login to book an event! <router-link to="/login">Login here</router-link>
      </p>
    </div>

    <div v-if="event && isEventExpired && showReviewsSection">
      <div v-if="isLoadingUserBookingStatus && isLoggedIn" class="loading-message small-loading form-container">
        <p>Checking your booking status...</p>
      </div>
      <div v-else>
        <div v-if="userBookingError" class="error-message small-error form-container">
            <p>{{ userBookingError }}</p>
        </div>

        <div v-if="canAddReview" class="add-review-container form-container">
          <h3>Add Your Review</h3>
          <form @submit.prevent="handleAddReview">
            <div class="form-group">
                <label for="review-title">Title:</label>
                <input type="text" id="review-title" name="title" v-model="newReview.title" required placeholder="Brief title for your review" />
            </div>
            <div class="form-group">
                <label for="review-rating">Rating:</label>
                <select id="review-rating" v-model.number="newReview.rating" required>
                  <option :value="null" disabled>Select a rating (1-5)</option>
                  <option value="1">1 Star (Poor)</option>
                  <option value="2">2 Stars (Fair)</option>
                  <option value="3">3 Stars (Good)</option>
                  <option value="4">4 Stars (Very Good)</option>
                  <option value="5">5 Stars (Excellent)</option>
                </select>
            </div>
            <div class="form-group">
              <label for="review-comment">Comment (Description):</label>
              <textarea id="review-comment" v-model.trim="newReview.comment" rows="4" required placeholder="Share your thoughts..."></textarea>
            </div>
            <p v-if="submitReviewError" class="error-message small-error">{{ submitReviewError }}</p>
            <p v-if="submitReviewSuccess" class="success-message small-success">{{ submitReviewSuccess }}</p>
            <button type="submit" :disabled="isSubmittingReview || !newReview.rating || !newReview.title.trim() || !newReview.comment.trim()" class="action-button submit-review-button">
              {{ isSubmittingReview ? 'Submitting...' : 'Submit Review' }}
            </button>
          </form>
        </div>
        <div v-else-if="isLoggedIn && event.status !== 'CANCELLED'">
            <div v-if="isCurrentUserOrgOfThisEvent" class="form-container info-message small-info">
                <p>Organizers cannot review their own events.</p>
            </div>
            <div v-else-if="hasUserReviewedThisEvent" class="form-container info-message small-info">
                <p>You have already submitted a review for this event.</p>
            </div>
            <div v-else-if="!hasUserBookedEvent" class="form-container info-message small-info">
                <p>You can only add a review if you had a confirmed booking for this event.</p>
            </div>
        </div>
        <div v-else-if="!isLoggedIn && event.status !== 'CANCELLED'" class="form-container info-message small-info">
              <p class="login-link">
                <router-link to="/login">Login</router-link> to add a review if you attended this event.
            </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import { mapGetters, mapState } from 'vuex';
import { jwtDecode } from 'jwt-decode';

export default {
  name: 'EventDetailView',
  data() {
    return {
      event: null,
      isLoading: true,
      error: null,
      isCancelling: false,
      cancellError: null,
      cancelSuccess: null,
      bookingData: {
        fullName: '',
        email: ''
      },
      isBooking: false,
      bookingError: null,
      bookingSuccess: null,
      isLoadingEventBookingsForCancellation: false,
      bookingCancellationErrors: [],
      bookingCancellationSuccessCount: 0,
      eventReviews: [],
      isLoadingReviews: false,
      reviewsError: null,
      newReview: {
        title: '',
        rating: null,
        comment: ''
      },
      isSubmittingReview: false,
      submitReviewError: null,
      submitReviewSuccess: null,
      isDeletingReviewId: null,
      reviewDeleteError: null,
      reviewDeleteSuccessMessage: null,
      hasUserBookedEvent: false,
      isLoadingUserBookingStatus: false,
      userBookingError: null,
      showReviewsSection: false,
    };
  },
  computed: {
    ...mapState('auth', {
        currentUserFromStore: state => state.user
    }),
    ...mapGetters('auth', ['isLoggedIn', 'isAdmin', 'isOrganizer', 'isParticipant']),
    isEventExpired() {
      if (!this.event) {
        return false;
      }
      const eventDateToCompare = this.event.endDateTime || this.event.startDateTime;
      if (!eventDateToCompare) {
        return false;
      }
      try {
        const eventEndDate = new Date(eventDateToCompare);
        if (isNaN(eventEndDate.getTime())) {
            return false;
        }
        return eventEndDate.getTime() < Date.now();
      } catch (e) {
        return false;
      }
    },
    isCurrentUserOrgOfThisEvent() {
      if (!this.isLoggedIn || !this.event || !this.event.organizerID) return false;
      const token = localStorage.getItem('jwtToken');
      if (!token) return false;
      try {
        const decodedToken = jwtDecode(token);
        const currentUserID = decodedToken.userID || decodedToken.sub;
        return !!currentUserID && String(currentUserID).toUpperCase() === String(this.event.organizerID).toUpperCase();
      } catch (e) { return false; }
    },
    currentUserID() {
        if (!this.isLoggedIn) return null;
        const token = localStorage.getItem('jwtToken');
        if (!token) return null;
        try {
            const decodedToken = jwtDecode(token);
            return decodedToken.userID || decodedToken.sub;
        } catch (e) {
            return null;
        }
    },
    hasUserReviewedThisEvent() {
        const currentId = this.currentUserID;
        if (!currentId || !this.eventReviews || this.eventReviews.length === 0) {
            return false;
        }
        return this.eventReviews.some(review => String(review.userID) === String(currentId));
    },
    canAddReview() {
        return this.isLoggedIn &&
              this.event &&
              this.event.status !== 'CANCELLED' &&
              !this.isCurrentUserOrgOfThisEvent &&
              !this.hasUserReviewedThisEvent &&
              this.isEventExpired &&
              this.hasUserBookedEvent;
    },
    sortedEventReviews() {
      return [...this.eventReviews].sort((a, b) => {
          const dateA = a.createdAt ? new Date(a.createdAt) : 0;
          const dateB = b.createdAt ? new Date(b.createdAt) : 0;
          return dateB - dateA;
      });
    }
  },
  methods: {
    async fetchEventDetails() {
      this.isLoading = true;
      this.error = null;
      this.event = null;
      this.eventReviews = [];
      this.reviewsError = null;
      this.reviewDeleteError = null;
      this.reviewDeleteSuccessMessage = null;
      this.hasUserBookedEvent = false;
      this.userBookingError = null;
      this.showReviewsSection = false;


      const eventId = this.$route.params.id;
      if (!eventId) {
        this.error = 'Event ID is missing.';
        this.isLoading = false;
        return;
      }
      try {
        const response = await apiClient.get(`/events/${eventId}`);
        this.event = response.data;
        if (this.event && this.event.id) {
          if (this.isEventExpired) {
            await this.fetchEventReviews(this.event.id);
          }
          if (this.isLoggedIn && this.currentUserID) {
              await this.checkCurrentUserBooking(this.event.id);
          }
        } else if (!this.event) {
            this.error = 'Event not found or data is empty.';
        }
      } catch (err) {
        if (err.response && err.response.status === 404) this.error = 'Event not found.';
        else this.error = `Failed to fetch event: ${err.response?.data?.message || err.message || 'An error occurred.'}`;
        this.event = null;
      } finally {
        this.isLoading = false;
      }
    },
    async checkCurrentUserBooking(eventId) {
        if (!this.isLoggedIn || !this.currentUserID) {
          this.hasUserBookedEvent = false;
          return;
        }
        this.isLoadingUserBookingStatus = true;
        this.hasUserBookedEvent = false;
        this.userBookingError = null;
        const token = localStorage.getItem('jwtToken');
        if (!token) {
            this.isLoadingUserBookingStatus = false;
            return;
        }

        try {
          const response = await apiClient.get(`/account/bookings?eventID=${eventId}`, {
            headers: { Authorization: `Bearer ${token}` }
          });
          const bookings = response.data || [];
          if (bookings.some(booking => String(booking.userID) === String(this.currentUserID) && (booking.status === 'CONFIRMED' || booking.status === 'COMPLETED'))) {
            this.hasUserBookedEvent = true;
          } else {
            this.hasUserBookedEvent = false;
          }
        } catch (err) {
          this.userBookingError = "Could not verify your booking status for this event.";
          this.hasUserBookedEvent = false;
        } finally {
          this.isLoadingUserBookingStatus = false;
        }
    },
    async fetchEventReviews(eventId) {
        if (!eventId) return;
        this.isLoadingReviews = true;
        this.reviewsError = null;
        this.reviewDeleteError = null;
        this.reviewDeleteSuccessMessage = null;
        try {
            const response = await apiClient.get(`/events/${eventId}/reviews`);
            this.eventReviews = Array.isArray(response.data) ? response.data : [];
        } catch (err) {
            this.reviewsError = (err.response?.data?.message || err.response?.data || err.message) || 'Could not load reviews.';
        } finally {
            this.isLoadingReviews = false;
        }
    },
    async handleAddReview() {
      if (!this.event || !this.event.id) {
        this.submitReviewError = "Cannot add review: Event details missing.";
        return;
      }
      if (!this.newReview.rating || this.newReview.rating < 1 || this.newReview.rating > 5) {
        this.submitReviewError = "Rating must be between 1 and 5.";
        return;
      }
      if (!this.newReview.title || !this.newReview.title.trim()) {
        this.submitReviewError = "Review title cannot be empty.";
        return;
      }
      if (!this.newReview.comment || !this.newReview.comment.trim()) {
        this.submitReviewError = "Comment cannot be empty.";
        return;
      }

      this.isSubmittingReview = true;
      this.submitReviewError = null;
      this.submitReviewSuccess = null;
      const token = localStorage.getItem('jwtToken');
      if (!token) {
        this.submitReviewError = 'Login required to add a review.';
        this.isSubmittingReview = false;
        return;
      }

      try {
        const reviewPayload = {
          title: this.newReview.title,
          rating: this.newReview.rating,
          description: this.newReview.comment,
        };
        await apiClient.post(`/events/${this.event.id}/reviews`, reviewPayload, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.submitReviewSuccess = 'Review added successfully!';
        this.newReview.title = '';
        this.newReview.rating = null;
        this.newReview.comment = '';
        await this.fetchEventReviews(this.event.id);
      } catch (err) {
        this.submitReviewError = `Failed to add review: ${err.response?.data?.message || err.response?.data || err.message || 'Error.'}`;
      } finally {
        this.isSubmittingReview = false;
        setTimeout(() => {
          this.submitReviewError = null;
          this.submitReviewSuccess = null;
        }, 7000);
      }
    },
    confirmDeleteMyReview(reviewID) {
      if (window.confirm('Are you sure you want to delete your review? This action cannot be undone.')) {
        this.deleteMyReview(reviewID);
      }
    },
    async deleteMyReview(reviewID) {
      if (!this.isLoggedIn) {
        this.reviewDeleteError = 'You must be logged in to delete a review.';
        return;
      }

      this.isDeletingReviewId = reviewID;
      this.reviewDeleteError = null;
      this.reviewDeleteSuccessMessage = null;
      const token = localStorage.getItem('jwtToken');

      if (!token) {
        this.reviewDeleteError = 'Authentication token not found. Please log in again.';
        this.isDeletingReviewId = null;
        return;
      }

      try {
        await apiClient.delete(`/reviews/${reviewID}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        this.eventReviews = this.eventReviews.filter(review => review.id !== reviewID);
        this.reviewDeleteSuccessMessage = 'Your review has been deleted successfully.';
      } catch (err) {
        if (err.response) {
          if (err.response.status === 401) {
            this.reviewDeleteError = 'Your session may have expired. Please log in again.';
          } else if (err.response.status === 403) {
            this.reviewDeleteError = "You are not authorized to delete this review.";
          } else if (err.response.status === 404) {
            this.reviewDeleteError = "Review not found. It might have already been deleted.";
          } else {
            this.reviewDeleteError = `Error deleting review: ${err.response.data?.message || err.response.data || err.message || 'Server error'}`;
          }
        } else {
          this.reviewDeleteError = `Error deleting review: ${err.message || 'Network error or unknown issue'}`;
        }
      } finally {
        this.isDeletingReviewId = null;
        setTimeout(() => {
          this.reviewDeleteError = null;
          this.reviewDeleteSuccessMessage = null;
        }, 7000);
      }
    },
    async handleBookEvent() {
      if (!this.event || !this.event.id) { this.bookingError = "Cannot book: Event details missing."; return; }
      if (this.event.status === 'CANCELLED') { this.bookingError = "Event cancelled, cannot book."; return; }
      if (!this.bookingData.fullName || !this.bookingData.email) { this.bookingError = "Full Name and Email required."; return; }
      if (!/\S+@\S+\.\S+/.test(this.bookingData.email)) { this.bookingError = "Valid email required."; return; }
      this.isBooking = true; this.bookingError = null; this.bookingSuccess = null;
      const token = localStorage.getItem('jwtToken');
      if (!token) { this.bookingError = 'Login required.'; this.isBooking = false; return; }
      try {
        const response = await apiClient.post(`/events/${this.event.id}/bookings`, this.bookingData, { headers: { Authorization: `Bearer ${token}` } });
        this.bookingSuccess = `Booking successful! ID: ${response.data?.id || 'N/A'}. Status: ${response.data?.status || 'PENDING'}.`;
        this.bookingData.fullName = ''; this.bookingData.email = '';
        await this.checkCurrentUserBooking(this.event.id);
      } catch (err) {
        this.bookingError = `Booking failed: ${err.response?.data?.message || err.response?.data || err.message || 'Error.'}`;
      } finally {
        this.isBooking = false; setTimeout(() => { this.bookingError = null; this.bookingSuccess = null; }, 7000);
      }
    },
    async handleCancelEventAndBookings() {
      if (!(this.isAdmin || (this.isOrganizer && this.isCurrentUserOrgOfThisEvent))) {
        this.cancellError = "Permission denied to cancel this event.";
        return;
      }
      const eventId = this.event?.id;
      if (!eventId) {
        this.cancellError = "Event ID missing.";
        return;
      }
      if (this.event?.status === 'CANCELLED') {
        this.cancellError = "This event is already cancelled.";
        return;
      }
      if (!confirm('Are you sure you want to cancel this event? This action will attempt to cancel all associated bookings and cannot be undone.')) {
        return;
      }
      this.isCancelling = true;
      this.cancellError = null;
      this.cancelSuccess = null;
      this.bookingCancellationErrors = [];
      this.bookingCancellationSuccessCount = 0;
      this.isLoadingEventBookingsForCancellation = true;
      const token = localStorage.getItem('jwtToken');
      if (!token) {
        this.cancellError = 'Authentication token not found.';
        this.isCancelling = false;
        this.isLoadingEventBookingsForCancellation = false;
        return;
      }
      const config = { headers: { Authorization: `Bearer ${token}` } };
      let eventBookingsToCancel = [];
      try {
        const bookingsResponse = await apiClient.get(`/events/${eventId}/bookings`, config);
        eventBookingsToCancel = bookingsResponse.data || [];
      } catch (err) {
        this.cancellError = 'Failed to fetch bookings to cancel. Event itself not yet cancelled.';
        this.isLoadingEventBookingsForCancellation = false;
        this.isCancelling = false;
        return;
      }
      this.isLoadingEventBookingsForCancellation = false;
      if (eventBookingsToCancel.length > 0) {
        for (const booking of eventBookingsToCancel) {
          if (booking.status !== 'CANCELLED') {
            try {
              await apiClient.put(`/bookings/${booking.id}/cancel`, {}, config);
              this.bookingCancellationSuccessCount++;
            } catch (err) {
              const bookingErrorMsg = `Failed to cancel booking ID ${booking.id}: ${err.response?.data?.message || err.response?.data || err.message || 'Error'}`;
              this.bookingCancellationErrors.push(bookingErrorMsg);
            }
          } else {
            this.bookingCancellationSuccessCount++;
          }
        }
      }
      try {
        await apiClient.put(`/events/${eventId}/cancel`, null, config);
        this.cancelSuccess = 'Event successfully marked as cancelled.';
        if (this.event) {
          this.event.status = 'CANCELLED';
        }
        if (this.bookingCancellationErrors.length > 0) {
          this.cancellError = "Event marked as cancelled, but some bookings could not be cancelled (see details above/below). Please check manually.";
        } else if (eventBookingsToCancel.length > 0) {
          this.cancelSuccess += ` All ${this.bookingCancellationSuccessCount} associated bookings were processed for cancellation.`;
        }
      } catch (err) {
        this.cancellError = `Failed to cancel the event itself: ${err.response?.data?.message || err.response?.data || err.message || 'Error'}`;
        if (this.bookingCancellationSuccessCount > 0) {
          this.cancellError += ` However, ${this.bookingCancellationSuccessCount} bookings were processed for cancellation.`
        }
      } finally {
        this.isCancelling = false;
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
      if (price === null || price === undefined || price === '') return 'Free or TBD';
      const numPrice = Number(price);
      return isNaN(numPrice) ? 'Invalid Price' : `$${numPrice.toFixed(2)}`;
    },
    formatCategory(category) {
      if (!category) return 'Uncategorized';
      const catString = String(category);
      return catString.charAt(0).toUpperCase() + catString.slice(1).toLowerCase().replace(/_/g, ' ');
    },
    toggleReviewsVisibility() {
        this.showReviewsSection = !this.showReviewsSection;
        if (this.showReviewsSection && this.event && this.event.id && this.eventReviews.length === 0 && !this.reviewsError && !this.isLoadingReviews) {
            this.fetchEventReviews(this.event.id);
        }
    }
  },
  async created() {
    await this.fetchEventDetails();
    if (this.isLoggedIn && this.currentUserFromStore) {
        this.bookingData.fullName = this.currentUserFromStore.name || '';
        this.bookingData.email = this.currentUserFromStore.email || '';
    }
  },
  watch: {
    '$route.params.id': {
        async handler(newId, oldId) {
            if (newId !== oldId) {
                this.hasUserBookedEvent = false;
                await this.fetchEventDetails();
            }
        },
        immediate: false
    },
    isLoggedIn(newVal) {
        if (newVal && this.currentUserFromStore) {
            this.bookingData.fullName = this.currentUserFromStore.name || '';
            this.bookingData.email = this.currentUserFromStore.email || '';
            if (this.event && this.event.id && this.currentUserID) {
              this.checkCurrentUserBooking(this.event.id);
            }
        } else if (!newVal) {
            this.bookingData.fullName = '';
            this.bookingData.email = '';
            this.hasUserBookedEvent = false;
        }
    },
    currentUserFromStore(newCurrentUser){
        if (this.isLoggedIn && newCurrentUser) {
            this.bookingData.fullName = newCurrentUser.name || '';
            this.bookingData.email = newCurrentUser.email || '';
        }
    }
  }
};
</script>

<style scoped>
.event-detail-page-container {
  padding: 20px;
  max-width: 1200px;
  margin: 20px auto;
  font-family: 'Arial', sans-serif;
}

.main-content-area {
  display: flex;
  flex-wrap: wrap;
  gap: 30px; /* Increased gap */
}

.event-detail-container {
  flex: 2; /* Give more space to event details */
  min-width: 320px; /* Slightly increased min-width */
}

.reviews-section-container {
  flex: 1;
  min-width: 300px; /* Slightly increased min-width */
}

.loading-message,
.error-message,
.not-found-message,
.info-message,
.no-reviews-message {
  text-align: center;
  padding: 20px;
  font-size: 1.2em;
  border-radius: 5px;
  margin-bottom: 20px;
  border: 1px solid #eee;
}
.loading-message.small-loading {
    font-size: 1em;
    padding: 10px;
    margin-top: 10px;
    margin-bottom: 10px;
}

.error-message {
  color: #D8000C;
  background-color: #FFD2D2;
  border-color: #D8000C;
}
.error-details {
  font-size: 0.9em;
  color: #555;
  margin-top: 5px;
}

.success-message {
  text-align: center;
  padding: 15px;
  font-size: 1.1em;
  border-radius: 5px;
  margin-bottom: 20px;
  color: #270;
  background-color: #DFF2BF;
  border: 1px solid #4F8A10;
}
.success-message.small-success, .error-message.small-error, .info-message.small-info {
    font-size: 0.9em;
    padding: 10px;
    margin-top: 10px;
    margin-bottom: 10px;
}
.error-message ul {
    list-style: disc;
    margin-left: 20px;
    text-align: left;
}
.info-message.small-info {
    background-color: #eef7ff;
    color: #0056b3;
    border-color: #b8d4ef;
}


.not-found-message .back-link {
  display: inline-block;
  margin-top: 15px;
  padding: 10px 15px;
  background-color: #007bff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}
.not-found-message .back-link:hover {
  background-color: #0056b3;
}

.event-details-card, .reviews-list-card {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 25px;
  margin-bottom: 20px;
  height: 100%;
  box-sizing: border-box;
}

.event-title {
  text-align: center;
  font-size: 2.2em;
  margin-top: 0;
  margin-bottom: 25px;
  color: #333;
  font-weight: 600;
}

.event-info p {
  font-size: 1.1em;
  margin-bottom: 12px;
  color: #555;
  line-height: 1.6;
}
.event-info p strong {
  color: #2c3e50;
  margin-right: 8px;
}
.event-info .status-cancelled { color: #e74c3c; font-weight: bold; }
.event-info .status-active { color: #27ae60; font-weight: bold; }
.event-info .status- {color: #7f8c8d; font-style: italic;}


.event-actions {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.action-button {
  padding: 12px 20px;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1.0em;
  font-weight: 500;
  transition: background-color 0.2s ease, transform 0.1s ease, box-shadow 0.2s ease;
  text-decoration: none;
  display: inline-block;
  text-align: center;
  min-width: 150px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.action-button:hover:not(:disabled) {
    box-shadow: 0 4px 8px rgba(0,0,0,0.15);
    transform: translateY(-1px);
}
.action-button:active:not(:disabled) {
  transform: translateY(1px);
   box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}
.action-button:disabled {
    cursor: not-allowed;
    opacity: 0.7;
}


.cancel-button { background-color: #e74c3c; }
.cancel-button:hover:not(:disabled) { background-color: #c0392b; }
.cancel-button:disabled { background-color: #f5b7b1; }

.update-link { background-color: #f39c12; }
.update-link:hover { background-color: #d35400; }

.view-bookings-link { background-color: #3498db; }
.view-bookings-link:hover { background-color: #2980b9; }

.toggle-reviews-button {
    background-color: #17a2b8; /* Teal/Info color */
}
.toggle-reviews-button:hover:not(:disabled) {
    background-color: #138496;
}


.form-container {
  display: flex; flex-direction: column; align-items: center; gap: 1rem;
  padding: 1.5rem;
  border-radius: 8px; background-color: #f9f9f9;
  max-width: 500px; margin: 20px auto;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.booking-form-container, .add-review-container, .login-prompt-container {
    max-width: 600px;
    margin: 20px auto;
}


.form-container h1, .add-review-container h3 { margin-bottom: 1rem; font-size: 1.8em; color: #333; text-align: center; }
.add-review-container h3 { font-size: 1.5em; }

.form-group { display: flex; flex-direction: column; width: 100%; max-width: 400px; margin-bottom: 1rem; }
.form-group label { margin-bottom: 0.5rem; font-weight: bold; color: #333; text-align: left; font-size: 0.9rem; }
.form-group input[type="text"],
.form-group input[type="email"],
.form-group input[type="number"],
.form-group select,
.form-group textarea {
  padding: 0.8rem 1rem; border: 1px solid #ccc; border-radius: 5px; font-size: 1rem;
  box-sizing: border-box; width: 100%; transition: border-color 0.3s ease, box-shadow 0.3s ease;
}
.form-group input[type="text"]:focus,
.form-group input[type="email"]:focus,
.form-group input[type="number"]:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #007bff; box-shadow: 0 0 0 3px rgba(0,123,255,0.25); outline: none;
}
.form-group textarea { resize: vertical; min-height: 80px; }

.event-actions-form { width: 100%; max-width: 400px; margin-top: 1rem; }

.book-button { background-color: #2ecc71; }
.submit-review-button { background-color: #3498db; margin-top: 10px;}

.book-button:hover:not(:disabled) { background-color: #27ae60; }
.submit-review-button:hover:not(:disabled) { background-color: #2980b9; }



.login-link { font-size: 1.1em; color: #555; text-align: center;}
.login-link a { color: #007bff; text-decoration: none; font-weight: bold; }
.login-link a:hover { text-decoration: underline; }

.status-confirmed, .status-active { color: #27ae60; font-weight: bold; }
.status-pending { color: #f39c12; font-weight: bold; }
.status-cancelled, .status-rejected { color: #e74c3c; font-weight: bold; }

.reviews-title {
  text-align: center;
  font-size: 1.8em;
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}
.no-reviews-message {
  text-align: center;
  color: #777;
  font-style: italic;
  padding: 15px 0;
}
.reviews-list {
  list-style-type: none;
  padding: 0;
  margin: 0;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}
.review-item {
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 15px;
  background-color: #fdfdfd;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.review-content-wrapper {
  flex-grow: 1;
}
.review-actions-wrapper {
  margin-left: 15px;
  flex-shrink: 0;
}
.delete-my-review-button {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 6px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85em;
  transition: background-color 0.2s ease;
}
.delete-my-review-button:hover:not(:disabled) {
  background-color: #c0392b;
}
.delete-my-review-button:disabled {
  background-color: #f5b7b1;
  cursor: not-allowed;
}
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
  gap: 5px;
}
.review-author {
  font-weight: bold;
  color: #2c3e50;
  font-size: 0.95em;
}
.review-date {
  font-size: 0.8em;
  color: #777;
}
.review-rating {
  font-size: 0.9em;
  color: #f39c12;
  margin-bottom: 8px;
  font-weight: bold;
}
.review-title-display {
    font-size: 1.1em;
    font-weight: 600;
    color: #444;
    margin-bottom: 5px;
}
.review-comment {
  font-size: 1em;
  color: #555;
  line-height: 1.5;
  white-space: pre-wrap;
}

.add-review-container h3 {
    font-size: 1.5em;
}

@media (max-width: 768px) {
  .main-content-area {
    flex-direction: column;
  }
  .event-detail-container, .reviews-section-container {
    flex: 1 1 100%;
  }
  .event-details-card, .reviews-list-card {
    height: auto;
  }
  .review-item {
    flex-direction: column;
    align-items: stretch;
  }
  .review-actions-wrapper {
    margin-left: 0;
    margin-top: 10px;
    align-self: flex-end;
  }
}
</style>