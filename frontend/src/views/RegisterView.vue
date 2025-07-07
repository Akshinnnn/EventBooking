<template>
  <div class="register-container">
    <div class="register-card">
      <h2>Create Account</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">Username:</label>
          <input type="text" id="username" v-model="formData.username" required />
        </div>
        <div class="form-group">
          <label for="name">Full Name:</label>
          <input type="text" id="name" v-model="formData.name" required />
        </div>
        <div class="form-group">
          <label for="email">Email:</label>
          <input type="email" id="email" v-model="formData.email" required />
        </div>
        <div class="form-group">
          <label for="phone">Phone Number:</label>
          <input type="tel" id="phone" v-model="formData.phone" />
        </div>
        <div class="form-group">
          <label for="password">Password:</label>
          <input type="password" id="password" v-model="formData.password" required />
        </div>
        <div class="form-group">
          <label for="confirmPassword">Confirm Password:</label>
          <input type="password" id="confirmPassword" v-model="formData.confirmPassword" required />
        </div>
        <div class="form-group">
          <label for="role">Register as:</label>
          <select id="role" v-model="formData.role" required>
            <option value="PARTICIPANT">Participant</option>
            <option value="ORGANIZER">Organizer</option>
          </select>
        </div>

        <p v-if="error" class="error-message">{{ error }}</p>
        <p v-if="successMessage" class="success-message">{{ successMessage }}</p>

        <button type="submit" :disabled="isLoading">
          {{ isLoading ? 'Registering...' : 'Register' }}
        </button>
      </form>
      <p class="login-link">
        Already have an account? <router-link to="/login">Login here</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import apiClient from '@/services/api';

export default {
  name: 'RegisterView',
  data() {
    return {
      formData: {
        username: '',
        name: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: '', 
        role: 'PARTICIPANT',  
      },
      isLoading: false,
      error: null,
      successMessage: '',
    };
  },
  methods: {
    async handleRegister() {
      this.error = null;
      this.successMessage = '';

      // Frontend Validations
      if (!this.formData.username.trim()) {
        this.error = 'Username is required.';
        this.isLoading = false;
        return;
      }
      if (!this.formData.name.trim()) {
        this.error = 'Full Name is required.';
        this.isLoading = false;
        return;
      }
      if (!this.formData.email.trim()) {
        this.error = 'Email is required.';
        this.isLoading = false;
        return;
      }
      if (this.formData.password !== this.formData.confirmPassword) {
        this.error = 'Passwords do not match.';
        this.isLoading = false;
        return;
      }
      if (this.formData.password.length < 6) {
        this.error = 'Password must be at least 6 characters long.';
        this.isLoading = false;
        return;
      }
      if (!this.formData.role) { 
        this.error = 'Please select a role.';
        this.isLoading = false;
        return;
      }

      this.isLoading = true;

      try {
        const { ...registrationDataToSend } = this.formData;

        const response = await apiClient.post('/auth/register', registrationDataToSend);
        const jwtToken = response.data; 

        if (jwtToken) {
          console.log('JWT Token received after registration:', jwtToken);
          this.successMessage = 'Registration successful! Logging you in...';

          const loginSuccess = await this.$store.dispatch('auth/processLoginToken', jwtToken);

          if (loginSuccess) {
            this.formData = {
              username: '', name: '', email: '', phone: '',
              password: '', confirmPassword: '', role: 'PARTICIPANT' 
            };

            setTimeout(() => {
              this.$router.push('/'); 
            }, 1000);
          } else {
            this.error = 'Registration succeeded, but automatic login failed. The token might be invalid or session setup failed. Please try logging in manually.';
            localStorage.setItem('jwtToken', jwtToken);
            setTimeout(() => {
  
              this.$router.push('/login'); 
            }, 1000);
          }
        } else {
          this.error = 'Registration successful, but no token was received from the server.';
        }
      } catch (err) {
        console.error('Registration error object:', err.response || err);
        if (err.response && err.response.data) {
          if (err.response.data.message) {
            this.error = err.response.data.message;
          } else if (typeof err.response.data === 'string') {
             if (err.response.data.toLowerCase().includes("duplicate key value violates unique constraint")) {
                if (err.response.data.toLowerCase().includes("users_username_key")) {
                    this.error = "This username is already taken. Please choose another one.";
                } else if (err.response.data.toLowerCase().includes("users_email_key")) {
                    this.error = "This email is already registered. Please use a different email or login.";
                } else {
                    this.error = "A user with some of this information already exists.";
                }
            } else {
                 this.error = err.response.data;
            }
          } else if (err.response.status === 409) {
            this.error = "Username or email already exists.";
          }
           else {
            this.error = `Registration failed: ${err.response.statusText || 'An error occurred.'}`;
          }
        } else if (err.message) {
          this.error = `Registration failed: ${err.message}`;
        } else {
          this.error = 'Registration failed. An unexpected error occurred.';
        }
      } finally {
        this.isLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.register-card {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
}

h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #555;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 12px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s ease;
}

button:disabled {
  background-color: #aaa;
  cursor: not-allowed;
}

button:hover:not(:disabled) {
  background-color: #218838;
}

.error-message {
  color: red;
  margin-bottom: 15px;
  text-align: center;
}
.success-message {
  color: green;
  margin-bottom: 15px;
  text-align: center;
}

.login-link {
  text-align: center;
  margin-top: 20px;
}
.login-link a {
  color: #007bff;
  text-decoration: none;
}
.login-link a:hover {
  text-decoration: underline;
}
</style>
