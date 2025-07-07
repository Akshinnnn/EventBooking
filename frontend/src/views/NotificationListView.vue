<template>
  <div class="notifications-container">
    <h2>My Notifications</h2>
    <div v-if="isLoading" class="loading-message">Loading notifications...</div>
    <div v-if="pageError" class="error-message">{{ pageError }}</div>

    <div v-if="!isLoading && !pageError && notifications.length === 0" class="info-message">
      You have no new notifications.
    </div>

    <ul v-if="!isLoading && !pageError && notifications.length > 0" class="notifications-list">
      <li v-for="notification in notifications" :key="notification.id" class="notification-item">
        <div class="notification-header">
          <h4 class="notification-title">{{ notification.title }}</h4>
          <span class="notification-date">{{ formatDate(notification.createdAt) }}</span>
        </div>
        <p class="notification-message">{{ notification.message }}</p>
        <div v-if="notification.eventID || notification.bookingID || notification.paymentID" class="notification-meta">
          <span v-if="notification.eventID" class="meta-item">Event ID: {{ notification.eventID }}</span>
          <span v-if="notification.eventID" class="meta-item">Event ID: <router-link :to="{ name: 'event-detail', params: { id: notification.eventID } }">{{ notification.eventID }}</router-link></span>
          <span v-if="notification.eventID" class="meta-item">Event ID: <router-link :to="{ name: 'event-detail', params: { id: notification.eventID } }">{{ notification.eventID }}</router-link></span>
          <span v-if="notification.bookingID" class="meta-item">Booking ID: {{ notification.bookingID }}</span>
          <span v-if="notification.paymentID" class="meta-item">Payment ID: {{ notification.paymentID }}</span>
        </div>
      </li>
    </ul>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import { mapState, mapGetters } from 'vuex';

export default {
  name: 'NotificationListView',
  data() {
    return {
      notifications: [],
      isLoading: false,
      pageError: null,
    };
  },
  computed: {
    ...mapState('auth', ['user']),
    ...mapGetters('auth', ['isLoggedIn']),
  },
  methods: {
    formatDate(dateTimeString) {
      if (!dateTimeString) return 'N/A';
      const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' };
      return new Date(dateTimeString).toLocaleDateString(undefined, options);
    },
    async fetchNotifications() {
      if (!this.isLoggedIn) {
        this.pageError = 'You must be logged in to view notifications.';
        this.$router.push('/login');
        return;
      }
      this.isLoading = true;
      this.pageError = null;
      try {
        const response = await apiClient.get('/account/notifications');
        this.notifications = response.data || [];
      } catch (err) {
        console.error('Failed to fetch notifications:', err);
        this.pageError = `Error loading notifications: ${err.response?.data?.message || err.response?.data || err.message || 'Unknown error'}`;
        if (err.response?.status === 401) {
            this.pageError = 'Your session may have expired. Please log in again.';
            this.$router.push('/login');
        }
      } finally {
        this.isLoading = false;
      }
    },
  },
  created() {
    this.fetchNotifications();
  },
  watch: {
    isLoggedIn(newVal) {
      if (newVal) {
        this.fetchNotifications();
      } else {
        this.notifications = [];
        this.pageError = 'You have been logged out.';
        this.$router.push('/login');
      }
    }
  }
};
</script>

<style scoped>
.notifications-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  font-family: sans-serif;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.notifications-container h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}

.notifications-list {
  list-style-type: none;
  padding: 0;
}

.notification-item {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  padding: 15px 20px;
  margin-bottom: 15px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.notification-item:last-child {
  margin-bottom: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title {
  margin: 0;
  font-size: 1.15em;
  color: #333;
  font-weight: 600;
}

.notification-date {
  font-size: 0.8em;
  color: #666;
}

.notification-message {
  margin: 0 0 10px 0;
  font-size: 0.95rem;
  color: #555;
  line-height: 1.6;
}

.notification-meta {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  font-size: 0.85em;
  color: #777;
}

.notification-meta .meta-item {
  display: block;
  margin-bottom: 3px;
}

.notification-meta .meta-item:last-child {
  margin-bottom: 0;
}


.loading-message, .error-message, .info-message {
  text-align: center;
  padding: 15px;
  margin-top: 20px;
  margin-bottom: 20px;
  border-radius: 4px;
  font-size: 1.1em;
}
.loading-message { color: #004085; background-color: #cce5ff; border: 1px solid #b8daff; }
.error-message { color: #721c24; background-color: #f8d7da; border: 1px solid #f5c6cb; }
.info-message { color: #0c5460; background-color: #d1ecf1; border: 1px solid #bee5eb; }
</style>