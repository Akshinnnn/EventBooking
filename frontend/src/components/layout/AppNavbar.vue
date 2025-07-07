<template>
  <nav class="navbar">
    <div class="navbar-brand">
      <router-link to="/" class="brand-link">EventBooker</router-link>
    </div>
    <ul class="navbar-links">
      <li><router-link to="/" exact-active-class="active-link">Home</router-link></li>
      <li><router-link to="/events" active-class="active-link">Events</router-link></li>

      <template v-if="isLoggedIn">
        <li v-if="isOrganizer || isAdmin"><router-link to="/events/new" active-class="active-link">Create Event</router-link></li>
        <li v-if="isParticipant"><router-link to="/my-bookings" active-class="active-link">My Bookings</router-link></li>
        
        <li v-if="isAdmin" class="admin-menu-item">
          <router-link to="/admin/users" active-class="active-link">User Management</router-link>
        </li>
        <li><router-link v-if="isAdmin" :to="{ name: 'categories-list' }">Manage Categories</router-link></li>
        <li><router-link to="/profile" active-class="active-link">Profile</router-link></li>
        <li><button @click="handleLogout" class="logout-button">Logout</button></li>
      </template>
      <template v-else>
        <li><router-link to="/login" active-class="active-link"><button class="login-button">Login</button></router-link></li>
      </template>
    </ul>
  </nav>
</template>

<script>
import { mapState, mapActions, mapGetters } from 'vuex';
export default {
  name: 'AppNavbar',
  computed: {
    ...mapState('auth', ['user']), 
    ...mapGetters('auth', ['isLoggedIn', 'isOrganizer', 'isParticipant', 'isAdmin']), 
  },
  methods: {
    ...mapActions('auth', ['logout']),

    async handleLogout() {
      try {
        await this.logout();
        this.$router.push('/login');
      } catch (error) {
        console.error("Failed to logout:", error);
      }
    }
  }
};
</script>

<style scoped>
.navbar {
  background-color: #333;
  color: white;
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center; 
}

.navbar-brand .brand-link {
  color: white;
  font-size: 1.5rem;
  font-weight: bold;
  text-decoration: none;
}

.navbar-links {
  list-style: none;
  display: flex;  
  align-items: center;   
  margin: 0;
  padding: 0;
  flex-wrap: wrap; 
}

.navbar-links li {
  margin-left: 15px;    
  display: flex;         
  align-items: center;   
}


.navbar-links li:first-child {
  margin-left: 0;
}

.navbar-links a {
  color: white;
  text-decoration: none;
  padding: 0.5rem; 
  transition: color 0.3s ease, font-weight 0.3s ease; 
  border-radius: 4px; 
}

.navbar-links a:hover,
.navbar-links a.active-link { 
  color: #00bcd4; 
  font-weight: bold;
}

.logout-button, .login-button, .register-button { 
  color: white;
  border: none;
  padding: 0.6rem 1.1rem; 
  border-radius: 4px;
  cursor: pointer;
  font-family: inherit; 
  font-size: 0.9rem;   
  transition: background-color 0.3s ease;
  font-weight: 500;
}

.logout-button {
  background-color: #d9534f;
}
.logout-button:hover {
  background-color: #c9302c;
}

.login-button {
  background-color: #5cb85c; 
}
.login-button:hover {
  background-color: #4cae4c;
}


@media (max-width: 768px) {
  .navbar {
    flex-direction: column;
    align-items: flex-start;
  }
  .navbar-links {
    flex-direction: column;
    width: 100%;
    margin-top: 1rem;
  }
  .navbar-links li {
    margin-left: 0;
    width: 100%;
    margin-bottom: 0.5rem; 
  }
   .navbar-links li a, .navbar-links li button {
    width: 100%; 
    box-sizing: border-box; 
    text-align: left; 
  }
  .navbar-links li button {
    justify-content: flex-start; 
  }
}
</style>