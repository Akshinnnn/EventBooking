<template>
  <div class="admin-user-list-container">
    <h1>User Management</h1>

    <div class="controls-container" v-if="isAdmin">
      <input
        type="text"
        v-model="searchQuery"
        placeholder="Search by username..."
        @input="handleSearchInput"
        class="search-input"
      />
    </div>

    <div v-if="isLoading" class="loading-message">
      <p>Loading users...</p>
    </div>

    <div v-if="error" class="error-message">
      <p>Sorry, we couldn't fetch the user list.</p>
      <p class="error-details"><i>{{ error }}</i></p>
    </div>

    <div v-if="!isLoading && !error && filteredUsers.length === 0" class="no-users-message">
      <p>{{ searchQuery ? 'No users found matching your search.' : 'No users found.' }}</p>
    </div>

    <div v-if="!isLoading && !error && filteredUsers.length > 0" class="user-cards-container">
      <div v-for="user in filteredUsers" :key="user.id" class="user-card">
        <div class="user-card-header">
          <h3 class="user-name">{{ user.name }}</h3>
          <span class="user-role" :class="getRoleClass(user.role)">{{ user.role }}</span>
        </div>
        <div class="user-card-body">
          <p><strong>ID:</strong> {{ user.id }}</p>
          <p><strong>Username:</strong> {{ user.username }}</p>
          <p><strong>Email:</strong> {{ user.email }}</p>
          <p><strong>Phone:</strong> {{ user.phone || 'N/A' }}</p>
          <p><strong>Joined:</strong> {{ formatDate(user.createdAt) }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import { mapGetters } from 'vuex';

export default {
  name: 'AdminUserList',
  data() {
    return {
      users: [],
      isLoading: false,
      error: null,
      searchQuery: '',
      debounceTimer: null,
    };
  },
  computed: {
    ...mapGetters('auth', ['isLoggedIn', 'userRoles', 'getToken']),
    isAdmin() {
      return this.userRoles.includes('ADMIN');
    },
    filteredUsers() {
      if (!this.users) return [];
      if (!this.searchQuery) {
        return this.users;
      }
      const searchTerm = this.searchQuery.toLowerCase();
      return this.users.filter(user =>
        (user.username && user.username.toLowerCase().includes(searchTerm)) ||
        (user.name && user.name.toLowerCase().includes(searchTerm)) ||
        (user.email && user.email.toLowerCase().includes(searchTerm))
      );
    }
  },
  methods: {
    async fetchUsers(searchTerm = null) {
      if (!this.isAdmin) {
        this.error = "You are not authorized to view this page.";
        if (this.$route.name !== 'home') {
            this.$router.push({ name: 'home' });
        }
        return;
      }
      this.isLoading = true;
      this.error = null;
      try {
        let endpoint = '/users';
        const params = {};
        if (searchTerm && searchTerm.trim() !== '') {
            params.search = searchTerm; 
        }

        const response = await apiClient.get(endpoint, {
            headers: {
                'Authorization': `Bearer ${this.getToken}`
            },
            params: params
        });
        this.users = Array.isArray(response.data) ? response.data : [];
      } catch (err) {
        console.error('Failed to fetch users:', err.response || err);
        this.error = (err.response?.data?.message || err.response?.data || err.message) || 'An unknown error occurred while fetching users.';
        if (err.response?.status === 401 || err.response?.status === 403) {
          this.error = "Unauthorized: You do not have permission to view this list or your session has expired.";
        }
        this.users = [];
      } finally {
        this.isLoading = false;
      }
    },
    formatDate(dateTimeString) {
      if (!dateTimeString) return 'N/A';
      const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
      try {
        return new Date(dateTimeString).toLocaleString(undefined, options);
      } catch (e) {
        console.error("Error formatting date:", dateTimeString, e);
        return "Invalid Date";
      }
    },
    handleSearchInput() {
      clearTimeout(this.debounceTimer);
      this.debounceTimer = setTimeout(() => {
      }, 300);
    },
    getRoleClass(role) {
        if (!role) return '';
        return `role-${role.toLowerCase()}`;
    }
  },
  created() {
    if (this.isLoggedIn && this.isAdmin) {
      this.fetchUsers();
    } else if (!this.isLoggedIn) {
      this.$router.push({ name: 'login', query: { redirect: this.$route.fullPath } });
    } else if (!this.isAdmin) {
        this.error = "You are not authorized to view this page.";
         if (this.$route.name !== 'home') {
            this.$router.push({ name: 'home' });
        }
    }
  },
  watch: {
    isLoggedIn(newVal) {
      if (newVal && this.isAdmin) {
        this.fetchUsers(this.searchQuery);
      } else if (!newVal) {
        this.users = [];
        this.error = null;
        this.$router.push({ name: 'login', query: { redirect: this.$route.fullPath } });
      }
    },
    isAdmin(newIsAdmin) {
        if (newIsAdmin && this.isLoggedIn) {
            this.fetchUsers(this.searchQuery);
        } else if (!newIsAdmin && this.isLoggedIn) {
            this.error = "You are not authorized to view this page.";
            this.users = [];
            if (this.$route.name !== 'home') {
                this.$router.push({ name: 'home' });
            }
        }
    }
  }
};
</script>

<style scoped>
.admin-user-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 20px auto;
  font-family: 'Arial', sans-serif;
}

h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 2em;
}

.controls-container {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.search-input {
  padding: 12px 18px;
  width: 60%;
  max-width: 500px;
  border: 1px solid #ccc;
  border-radius: 25px;
  font-size: 1em;
  transition: all 0.3s ease-in-out;
}
.search-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 8px rgba(0,123,255,.3);
}

.loading-message,
.error-message,
.no-users-message {
  text-align: center;
  padding: 25px;
  font-size: 1.25em;
  border-radius: 8px;
  margin: 20px auto;
  max-width: 600px;
}

.error-message {
  color: #D8000C;
  background-color: #FFD2D2;
  border: 1px solid #D8000C;
}
.error-details {
  font-size: 0.9em;
  color: #555;
  margin-top: 8px;
}

.no-users-message {
  color: #00529B;
  background-color: #BDE5F8;
  border: 1px solid #00529B;
}

.user-cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
  margin-top: 20px;
}

.user-card {
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.user-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.12);
}

.user-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}

.user-name {
  margin: 0;
  font-size: 1.4em;
  color: #007bff;
  font-weight: 600;
}

.user-role {
  font-size: 0.85em;
  font-weight: bold;
  padding: 5px 10px;
  border-radius: 15px;
  color: white;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.role-admin {
  background-color: #dc3545; /* Red for Admin */
}
.role-organizer {
  background-color: #28a745; /* Green for Organizer */
}
.role-participant {
  background-color: #ffc107; /* Yellow for Participant */
  color: #333;
}
.role-default {
  background-color: #6c757d; /* Grey for others */
}


.user-card-body p {
  margin: 8px 0;
  font-size: 0.95em;
  color: #555;
  line-height: 1.6;
}

.user-card-body p strong {
  color: #333;
  margin-right: 5px;
}

@media (max-width: 768px) {
  .user-cards-container {
    grid-template-columns: 1fr;
  }
  .search-input {
    width: 80%;
  }
  h1 {
    font-size: 1.75em;
  }
  .user-name {
    font-size: 1.2em;
  }
}
</style>