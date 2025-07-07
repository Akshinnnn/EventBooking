<template>
  <div class="my-reviews-container">
    <h2>My Reviews</h2>
    <div v-if="isLoading" class="loading-message">Loading your reviews...</div>
    <div v-if="pageError" class="error-message">{{ pageError }}</div>
    <div v-if="deleteSuccessMessage" class="success-message">{{ deleteSuccessMessage }}</div>

    <div v-if="!isLoading && !pageError && reviews.length === 0" class="info-message">
      You have not submitted any reviews yet.
    </div>

    <ul v-if="!isLoading && !pageError && reviews.length > 0" class="reviews-list">
      <li v-for="review in reviews" :key="review.id" class="review-item">
        <div class="review-content">
          <div class="review-header">
            <h4 class="review-subject">{{ review.title || 'Review' }}</h4>
            <span class="review-date">{{ formatDate(review.createdAt) }}</span>
          </div>
          <div class="review-rating" v-if="review.rating !== undefined && review.rating !== null">
            Rating:
            <span v-for="n in 5" :key="`star-${review.id}-${n}`" class="star">
              {{ n <= review.rating ? '★' : '☆' }}
            </span>
            ({{ review.rating }}/5)
          </div>
          <p class="review-comment" v-if="review.description">{{ review.description }}</p>
          <div v-if="review.eventID" class="review-meta">
            <span class="meta-item">Related Event ID: {{ review.eventID }}</span>
            <span class="meta-item">Related Event ID: <router-link :to="{ name: 'event-detail', params: { id: review.eventID } }">{{ review.eventID }}</router-link></span>
            <span class="meta-item">Related Event ID: <router-link :to="{ name: 'event-detail', params: { id: review.eventID } }">{{ review.eventID }}</router-link></span>
          </div>
        </div>
        <div class="review-actions">
          <button @click="confirmDeleteReview(review.id)" class="delete-button" :disabled="isDeleting === review.id">
            <span v-if="isDeleting === review.id">Deleting...</span>
            <span v-else>Delete</span>
          </button>
          <div v-if="deleteError && deletingReviewId === review.id" class="error-message item-error-message">{{ deleteError }}</div>
        </div>
      </li>
    </ul>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import { mapState, mapGetters } from 'vuex';

export default {
  name: 'MyReviewListView',
  data() {
    return {
      reviews: [],
      isLoading: false,
      pageError: null,
      isDeleting: null, 
      deletingReviewId: null, 
      deleteError: null,   
      deleteSuccessMessage: '', 
    };
  },
  computed: {
    ...mapState('auth', ['user']),
    ...mapGetters('auth', ['isLoggedIn']),
  },
  methods: {
    formatDate(dateTimeString) {
      if (!dateTimeString) return 'N/A';
      const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
      return new Date(dateTimeString).toLocaleDateString(undefined, options);
    },
    async fetchUserReviews() {
      if (!this.isLoggedIn) {
        this.pageError = 'You must be logged in to view your reviews.';
        if (this.$router.currentRoute.path !== '/login') {
            this.$router.push('/login');
        }
        return;
      }
      this.isLoading = true;
      this.pageError = null;
      this.deleteSuccessMessage = ''; 
      try {
        const response = await apiClient.get('/account/reviews');
        this.reviews = response.data || [];
      } catch (err) {
        console.error('Failed to fetch user reviews:', err);
        this.pageError = `Error loading reviews: ${err.response?.data?.message || err.response?.data || err.message || 'Unknown error'}`;
        if (err.response?.status === 401) {
          this.pageError = 'Your session may have expired. Please log in again.';
          if (this.$router.currentRoute.path !== '/login') {
            this.$store.dispatch('auth/logout'); 
            this.$router.push('/login');
          }
        }
      } finally {
        this.isLoading = false;
      }
    },
    confirmDeleteReview(reviewID) {
      if (window.confirm('Are you sure you want to delete this review? This action cannot be undone.')) {
        this.deleteReview(reviewID);
      }
    },
    async deleteReview(reviewID) {
      if (!this.isLoggedIn) {
        this.pageError = 'You must be logged in to perform this action.';
        this.$router.push('/login');
        return;
      }

      this.isDeleting = reviewID;
      this.deletingReviewId = reviewID;
      this.deleteError = null;
      this.deleteSuccessMessage = '';

      try {
        await apiClient.delete(`/reviews/${reviewID}`);

        this.reviews = this.reviews.filter(review => review.id !== reviewID);
        this.deleteSuccessMessage = 'Review deleted successfully.';
        setTimeout(() => this.deleteSuccessMessage = '', 1000);

      } catch (err) {
        console.error('Failed to delete review:', err);
        const errorMessage = err.response?.data?.message || err.response?.data || err.message || 'Could not delete the review.';
        this.deleteError = errorMessage;

        if (err.response?.status === 401) {
          this.pageError = 'Your session may have expired. Please log in again.';
          this.$store.dispatch('auth/logout');
          this.$router.push('/login');
        } else if (err.response?.status === 403) {
          this.deleteError = "You are not authorized to delete this review.";
        } else if (err.response?.status === 404) {
          this.deleteError = "Review not found. It might have already been deleted.";
        }
        setTimeout(() => {
            this.deleteError = null;
            this.deletingReviewId = null;
        }, 5000);
      } finally {
        this.isDeleting = null;
        if (!this.deleteError) {
            this.deletingReviewId = null;
        }
      }
    },
  },
  created() {
    this.fetchUserReviews();
  },
  watch: {
    isLoggedIn(newVal, oldVal) {
      if (newVal && !oldVal) { 
        this.fetchUserReviews();
      } else if (!newVal && oldVal) { 
        this.reviews = [];
        this.pageError = 'You have been logged out.';
        if (this.$router.currentRoute.path !== '/login') {
            this.$router.push('/login');
        }
      }
    }
  }
};
</script>

<style scoped>
.my-reviews-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  font-family: sans-serif;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.my-reviews-container h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.reviews-list {
  list-style-type: none;
  padding: 0;
}

.review-item {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  padding: 15px 20px;
  margin-bottom: 15px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.review-item:last-child {
  margin-bottom: 0;
}

.review-content {
  flex-grow: 1; 
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.review-subject {
  margin: 0;
  font-size: 1.1em;
  color: #333;
  font-weight: 600;
}

.review-date {
  font-size: 0.8em;
  color: #666;
}

.review-rating {
  margin-bottom: 10px;
  font-size: 0.95em;
  color: #444;
}

.review-rating .star {
  color: #fd7e14;
  font-size: 1.1em;
  margin-right: 1px;
}

.review-comment {
  margin: 0 0 10px 0;
  font-size: 0.95rem;
  color: #555;
  line-height: 1.6;
  white-space: pre-wrap;
}

.review-meta {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  font-size: 0.85em;
  color: #777;
}

.review-meta .meta-item {
  display: block;
  margin-bottom: 3px;
}

.review-actions {
  margin-left: 15px; 
  display: flex;
  flex-direction: column; 
  align-items: flex-end; 
}

.delete-button {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9em;
  transition: background-color 0.2s ease;
  min-width: 80px; 
  text-align: center;
}

.delete-button:hover {
  background-color: #c82333;
}

.delete-button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

.loading-message, .error-message, .info-message, .success-message {
  text-align: center;
  padding: 15px;
  margin-top: 20px;
  margin-bottom: 20px;
  border-radius: 4px;
  font-size: 1em; 
}
.loading-message { color: #004085; background-color: #cce5ff; border: 1px solid #b8daff; }
.error-message { color: #721c24; background-color: #f8d7da; border: 1px solid #f5c6cb; }
.info-message { color: #0c5460; background-color: #d1ecf1; border: 1px solid #bee5eb; }
.success-message { color: #155724; background-color: #d4edda; border: 1px solid #c3e6cb; }

.item-error-message {
  font-size: 0.8em;
  padding: 5px 0 0 0; 
  margin-top: 5px;    
  margin-bottom: 0;
  text-align: right; 
  border: none;       
  background-color: transparent; 
}
</style>