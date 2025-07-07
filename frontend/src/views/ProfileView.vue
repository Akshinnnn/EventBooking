<template>
  <div class="user-profile-container">
    <div v-if="isLoading && !profileData" class="loading-message">Loading profile...</div>
    <div v-if="pageError" class="error-message">{{ pageError }}</div>

    <div v-if="profileData && !pageError" class="profile-card">
      <h2>User Profile</h2>

      <div class="balance-section section">
        <h3>Account Balance</h3>
        <div v-if="isFetchingBalance" class="loading-message small-loading">Loading balance...</div>
        <div v-if="balanceError" class="error-message small-error">{{ balanceError }}</div>
        <div v-if="!isFetchingBalance && currentBalance !== null">
          <p><strong>Current Balance:</strong> ${{ currentBalance.toFixed(2) }}</p>
          <div v-if="!showAddFundsInput">
            <button @click="toggleAddFunds" class="btn-add-funds-toggle">Add Funds</button>
          </div>
          <div v-if="showAddFundsInput" class="add-funds-form">
            <div class="form-group">
              <label for="fundsToAdd">Amount to Add ($):</label>
              <input type="number" id="fundsToAdd" v-model.number="fundsToAdd" placeholder="e.g., 50" min="0.01" step="0.01" required />
            </div>
            <div class="form-actions">
              <button @click="addFundsHandler" :disabled="isAddingFunds || fundsToAdd <= 0" class="btn-add-funds">
                {{ isAddingFunds ? 'Adding...' : 'Add' }}
              </button>
              <button @click="cancelAddFunds" class="btn-cancel-funds" :disabled="isAddingFunds">Cancel</button>
            </div>
          </div>
        </div>
        <div v-if="!isFetchingBalance && currentBalance === null && !balanceError" class="info-message small-info">
          Could not load balance information.
        </div>
      </div>

      <div v-if="!editMode" class="profile-details section">
        <h3>Profile Details</h3>
        <p><strong>Name:</strong> {{ profileData.name }}</p>
        <p><strong>Username:</strong> {{ profileData.username }}</p>
        <p><strong>Email:</strong> {{ profileData.email }}</p>
        <p><strong>Phone:</strong> {{ profileData.phone || 'Not provided' }}</p>
        <p><strong>Role:</strong> {{ profileData.role }}</p>
        <button @click="toggleEditMode" class="btn-edit">Edit Profile</button>
      </div>

      <form v-if="editMode" @submit.prevent="handleProfileUpdate" class="profile-form section">
        <h3>Edit Profile</h3>
        <div class="form-group">
          <label for="name">Full Name:</label>
          <input type="text" id="name" v-model="editableProfileData.name" required />
        </div>
        <div class="form-group">
          <label for="email">Email:</label>
          <input type="email" id="email" v-model="editableProfileData.email" required />
        </div>
        <div class="form-group">
          <label for="phone">Phone:</label>
          <input type="tel" id="phone" v-model="editableProfileData.phone" />
        </div>
        <div v-if="updateError" class="error-message small-error">{{ updateError }}</div>
        <div v-if="updateSuccess" class="success-message small-success">{{ updateSuccess }}</div>
        <div class="form-actions">
          <button type="submit" :disabled="isUpdating" class="btn-save">
            {{ isUpdating ? 'Saving...' : 'Save Changes' }}
          </button>
          <button type="button" @click="cancelEditMode" class="btn-cancel">Cancel</button>
        </div>
      </form>

      <div v-if="isOrganizer && profileData.id" class="organizer-events section">
        <h3>My Created Events</h3>
        <div v-if="organizerEventsLoading">Loading your events...</div>
        <div v-if="organizerEventsError" class="error-message small-error">{{ organizerEventsError }}</div>
        <ul v-if="organizerEvents.length > 0">
          <li v-for="eventItem in organizerEvents" :key="eventItem.id">
            <router-link :to="{ name: 'event-detail', params: { id: eventItem.id } }">{{ eventItem.title }}</router-link>
            <span> ({{ eventItem.status }})</span>
          </li>
        </ul>
        <p v-if="!organizerEventsLoading && !organizerEventsError && organizerEvents.length === 0">You have not created any events yet.</p>
        <router-link to="/events/new"><button class="btn-create-event">Create New Event</button></router-link>
      </div>

      <div v-if="profileData.id" class="participant-bookings section">
        <h3>My Bookings</h3>
        <div v-if="bookingsLoading">Loading your bookings...</div>
        <div v-if="bookingsError" class="error-message small-error">{{ bookingsError }}</div>
        <ul v-if="userBookings.length > 0">
          <li v-for="booking in userBookings" :key="booking.id">
            Booking for Event ID: {{ booking.eventID }} - Status: {{ booking.status }}
            <span class="booking-date">(Booked on: {{ formatDate(booking.createdAt) }})</span>
          </li>
        </ul>
        <p v-if="!bookingsLoading && !bookingsError && userBookings.length === 0">You have no bookings yet.</p>
      </div>

      <div class="navigation-links section">
        <router-link to="/my-payments" class="btn-view-payments btn-nav-link">View Payment History</router-link>
        <router-link to="/notifications" class="btn-view-notifications btn-nav-link">View Notifications</router-link>
        <router-link to="/my-reviews" class="btn-view-reviews btn-nav-link">View My Reviews</router-link>
      </div>
    </div>
    <div v-if="!isLoading && !pageError && !profileData" class="info-message">
      Could not load profile data. You might need to log in again.
    </div>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import { mapGetters, mapState } from 'vuex';

export default {
  name: 'UserProfileView',
  data() {
    return {
      profileData: null,
      editableProfileData: {},
      isLoading: false,
      pageError: null,
      editMode: false,
      isUpdating: false,
      updateError: null,
      updateSuccess: null,
      currentBalance: null,
      isFetchingBalance: false,
      balanceError: null,
      showAddFundsInput: false,
      fundsToAdd: null,
      isAddingFunds: false,
      organizerEvents: [],
      organizerEventsLoading: false,
      organizerEventsError: null,
      userBookings: [],
      bookingsLoading: false,
      bookingsError: null,
    };
  },
  computed: {
    ...mapState('auth', ['user']),
    ...mapGetters('auth', ['isLoggedIn', 'isOrganizer', 'isParticipant']),
  },
  methods: {
    formatDate(dateTimeString) {
      if (!dateTimeString) return 'N/A';
      const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
      return new Date(dateTimeString).toLocaleDateString(undefined, options);
    },
    async fetchUserProfile() {
      if (!this.isLoggedIn || !this.user || !this.user.userID) {
        this.pageError = 'User not authenticated. Cannot load profile.';
        this.$router.push('/login');
        return;
      }
      this.isLoading = true;
      this.pageError = null;
      try {
        const response = await apiClient.get(`/account`);
        this.profileData = response.data;
        this.resetEditableProfileData();
        await this.fetchBalance();
        if (this.isOrganizer) {
          this.fetchOrganizerEvents();
        }
        if (this.isParticipant || this.isOrganizer) {
          this.fetchUserBookings();
        }
      } catch (err) {
        console.error('Failed to fetch user profile:', err);
        this.pageError = `Error loading profile: ${err.response?.data?.message || err.message || 'Unknown error'}`;
         if (err.response?.status === 401) {
            this.$router.push('/login');
        }
      } finally {
        this.isLoading = false;
      }
    },
    async fetchBalance() {
        this.isFetchingBalance = true;
        this.balanceError = null;
        try {
            const response = await apiClient.get('/account/balance');
            this.currentBalance = typeof response.data === 'number' ? response.data : response.data.balance;
            if (this.currentBalance === undefined || this.currentBalance === null) {
                console.warn('Balance data received is undefined or null:', response.data);
                this.currentBalance = 0;
            }
        } catch (err) {
            console.error('Failed to fetch balance:', err);
            this.balanceError = `Could not load balance: ${err.response?.data?.message || err.message || 'Network error'}`;
            this.currentBalance = null;
             if (err.response?.status === 401) {
                this.balanceError = 'Session expired. Please log in again to see your balance.';
            }
        } finally {
            this.isFetchingBalance = false;
        }
    },
    toggleAddFunds() {
        this.showAddFundsInput = true;
        this.fundsToAdd = null;
        this.balanceError = null;
    },
    cancelAddFunds() {
        this.showAddFundsInput = false;
        this.fundsToAdd = null;
        this.balanceError = null;
    },
    async addFundsHandler() {
        if (this.fundsToAdd === null || this.fundsToAdd <= 0) {
            this.balanceError = "Please enter a valid positive amount.";
            return;
        }
        if (isNaN(this.fundsToAdd)) {
            this.balanceError = "Please enter a numeric value for the amount.";
            return;
        }
        this.isAddingFunds = true;
        this.balanceError = null;
        try {
            const response = await apiClient.post('/account/balance', this.fundsToAdd, {
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            console.log('Payment successful:', response.data);
            this.showAddFundsInput = false;
            this.fundsToAdd = null;
            await this.fetchBalance();
        } catch (err) {
            console.error('Failed to add funds:', err);
            this.balanceError = `Failed to add funds: ${err.response?.data?.message || err.response?.data || err.message || 'Network error'}`;
            if (err.response?.status === 401) {
                this.balanceError = 'Your session may have expired. Please log in again to add funds.';
            }
        } finally {
            this.isAddingFunds = false;
        }
    },
    toggleEditMode() {
      this.editMode = !this.editMode;
      if (this.editMode) {
        this.resetEditableProfileData();
        this.updateError = null;
        this.updateSuccess = null;
      } else {
          this.updateError = null;
          this.updateSuccess = null;
      }
    },
    resetEditableProfileData() {
      if (this.profileData) {
        this.editableProfileData = JSON.parse(JSON.stringify({
          name: this.profileData.name,
          email: this.profileData.email,
          phone: this.profileData.phone || '',
        }));
      }
    },
    cancelEditMode() {
        this.editMode = false;
        this.updateError = null;
        this.updateSuccess = null;
    },
    async handleProfileUpdate() {
      if (!this.user || !this.user.userID) {
        this.updateError = "Cannot update profile: User ID is missing.";
        return;
      }
      this.isUpdating = true;
      this.updateError = null;
      this.updateSuccess = null;
      const updatePayload = {
        name: this.editableProfileData.name,
        email: this.editableProfileData.email,
        phone: this.editableProfileData.phone,
      };
      try {
        const response = await apiClient.put(`/account`, updatePayload);
        this.profileData = response.data;
        this.updateSuccess = 'Profile updated successfully!';
        this.editMode = false;
      } catch (err) {
        console.error('Failed to update profile:', err);
        this.updateError = `Update failed: ${err.response?.data?.message || err.message || 'Unknown error'}`;
        if (err.response?.status === 401) {
            this.updateError = 'Your session may have expired. Please log in again to update your profile.';
        }
      } finally {
        this.isUpdating = false;
      }
    },
    async fetchOrganizerEvents() {
      if (!this.profileData || !this.profileData.id) return;
      this.organizerEventsLoading = true;
      this.organizerEventsError = null;
      try {
        const response = await apiClient.get(`/events?organizerID=${this.profileData.id}`);
        this.organizerEvents = response.data || [];
      } catch (err) {
        console.error('Failed to fetch organizer events:', err);
        this.organizerEventsError = `Error loading your events: ${err.response?.data?.message || err.message}`;
      } finally {
        this.organizerEventsLoading = false;
      }
    },
    async fetchUserBookings() {
      this.bookingsLoading = true;
      this.bookingsError = null;
      try {
        const response = await apiClient.get('/account/bookings');
        this.userBookings = response.data || [];
      } catch (err) {
        console.error('Failed to fetch user bookings:', err);
        this.bookingsError = `Error loading your bookings: ${err.response?.data?.message || err.message}`;
         if (err.response?.status === 401) {
            this.bookingsError = 'Your session may have expired. Please log in again to view your bookings.';
        }
      } finally {
        this.bookingsLoading = false;
      }
    },
  },
  created() {
    if (this.isLoggedIn) {
      this.fetchUserProfile();
    } else {
      this.$router.push('/login');
    }
  },
  watch: {
    isLoggedIn(newVal) {
      if (!newVal) {
        this.profileData = null;
        this.currentBalance = null;
        this.$router.push('/login');
      } else {
          if (!this.profileData) {
            this.fetchUserProfile();
          }
      }
    }
  }
};
</script>

<style scoped>
.user-profile-container {
  max-width: 900px;
  margin: 20px auto;
  padding: 20px;
  font-family: sans-serif;
}
.profile-card {
  background-color: #fff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
.profile-card h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
}
.section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
.section:first-of-type {
    margin-top: 0;
    padding-top: 0;
    border-top: none;
}
.section h3 {
  margin-bottom: 15px;
  color: #444;
  font-size: 1.3em;
}
.balance-section p strong {
  color: #333;
  min-width: 100px;
  display: inline-block;
}
.balance-section p {
    font-size: 1.1em;
    margin-bottom: 12px;
    color: #555;
    line-height: 1.6;
}
.btn-add-funds-toggle {
    padding: 8px 15px;
    background-color: #17a2b8;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.95rem;
    transition: background-color 0.3s ease;
    margin-top: 5px;
}
.btn-add-funds-toggle:hover {
    background-color: #138496;
}
.add-funds-form {
    margin-top: 15px;
    padding: 15px;
    border: 1px solid #e0e0e0;
    border-radius: 5px;
    background-color: #f9f9f9;
}
.add-funds-form .form-group {
    margin-bottom: 10px;
}
.add-funds-form label {
    display: block;
    margin-bottom: 5px;
    font-weight: bold;
    color: #555;
    font-size: 0.95em;
}
.add-funds-form input[type="number"] {
    width: 100%;
    max-width: 200px;
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-sizing: border-box;
}
.btn-add-funds {
    padding: 10px 20px;
    background-color: #28a745;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1rem;
    transition: background-color 0.3s ease;
    margin-right: 10px;
}
.btn-add-funds:hover:not(:disabled) {
    background-color: #218838;
}
.btn-add-funds:disabled {
    background-color: #aaa;
    cursor: not-allowed;
}
.btn-cancel-funds {
    padding: 10px 20px;
    background-color: #6c757d;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1rem;
    transition: background-color 0.3s ease;
}
.btn-cancel-funds:hover:not(:disabled) {
    background-color: #5a6268;
}
.btn-cancel-funds:disabled {
    background-color: #bbb;
    cursor: not-allowed;
}
.profile-details p {
  font-size: 1.1em;
  margin-bottom: 12px;
  color: #555;
  line-height: 1.6;
}
.profile-details p strong {
  color: #333;
  min-width: 100px;
  display: inline-block;
}
.btn-edit, .btn-save, .btn-cancel, .btn-create-event, .btn-nav-link {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
  margin-right: 10px;
  margin-top: 10px;
  text-decoration: none;
  display: inline-block;
  color: white;
}
.btn-edit { background-color: #007bff; }
.btn-edit:hover { background-color: #0056b3; }
.profile-form .form-group { margin-bottom: 15px; }
.profile-form label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; }
.profile-form input[type="text"],
.profile-form input[type="email"],
.profile-form input[type="tel"] {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}
.profile-form .form-actions { margin-top: 20px; display: flex; justify-content: flex-start; }
.btn-save { background-color: #28a745; }
.btn-save:hover:not(:disabled) { background-color: #218838; }
.btn-save:disabled { background-color: #aaa; cursor: not-allowed;}
.btn-cancel { background-color: #6c757d; }
.btn-cancel:hover { background-color: #5a6268; }
.loading-message, .error-message, .success-message, .info-message {
  text-align: center;
  padding: 10px;
  margin-bottom: 15px;
  border-radius: 4px;
  border: 1px solid transparent;
}
.loading-message { color: #004085; background-color: #cce5ff; border-color: #b8daff; }
.error-message { color: #721c24; background-color: #f8d7da; border-color: #f5c6cb; }
.success-message { color: #155724; background-color: #d4edda; border-color: #c3e6cb; }
.info-message { color: #0c5460; background-color: #d1ecf1; border-color: #bee5eb; }
.small-error, .small-success, .small-loading, .small-info {
  font-size: 0.9em;
  padding: 8px;
  margin-top: 10px;
  text-align: left;
}
.small-loading { margin-top: 5px; margin-bottom: 10px; }
.section ul { list-style-type: none; padding-left: 0; }
.section li { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.section li:last-child { border-bottom: none; }
.section a { color: #007bff; text-decoration: none; }
.section a:hover { text-decoration: underline; }
.booking-date { font-size: 0.85em; color: #777; margin-left: 10px; }
.btn-create-event { background-color: #17a2b8; margin-top: 15px;}
.btn-create-event:hover { background-color: #138496; }

.navigation-links {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #eee;
    display: flex;
    flex-wrap: wrap;
    gap: 10px; /* Adds space between buttons */
}
.btn-nav-link {
  /* margin-right: 10px; removed as gap is used now */
  /* margin-top: 10px; removed as gap handles vertical spacing in wrap */
  margin-bottom: 0; /* Adjusted if buttons are on single line mostly */
}
.btn-view-payments {
    background-color: #ffc107;
    color: #212529;
}
.btn-view-payments:hover {
    background-color: #e0a800;
}
.btn-view-notifications {
    background-color: #2ce296;
    color: #fff;
}
.btn-view-notifications:hover {
    background-color: #1fda38;
}
.btn-view-reviews {
    background-color: #b5eb38;
    color: white;
}
.btn-view-reviews:hover {
    background-color: #d3ea44;
}
</style>