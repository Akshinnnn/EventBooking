<template>
  <div class="login-container">
    <div class="login-card">
      <h2>Login</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">Username:</label>
          <input type="username" id="username" v-model="formData.username" required />
        </div>
        <div class="form-group">
          <label for="password">Password:</label>
          <input type="password" id="password" v-model="formData.password" required />
        </div>

        <p v-if="error" class="error-message">{{ error }}</p>

        <button type="submit" :disabled="isLoading">
          {{ isLoading ? 'Logging in...' : 'Login' }}
        </button>
      </form>
      <p class="register-link">
        Don't have an account? <router-link to="/register">Register here</router-link>
      </p>
    </div>
  </div>
</template>

<script>
import apiClient from '@/services/api';

export default {
  name: 'LoginView',
  data() {
    return {
      formData: {
        username: '',
        password: '',
      },
      isLoading: false,
      error: null,
    };
  },
  methods: {

    async handleLogin() {
      this.error = null;
      this.isLoading = true;
      try {
        const response = await apiClient.post('/auth/login', this.formData);
        const jwtToken = response.data;

        if (jwtToken) {
            console.log('Login successful:');
            this.$root.isLoggedIn = true;
            const loginSuccess = await this.$store.dispatch('auth/processLoginToken', jwtToken);
            if (loginSuccess) {
            this.formData = {
              username: '', 
              password: ''
            };

            setTimeout(() => {
              this.$router.push('/'); 
            });
          } else {
            this.error = 'Login failed: Invalid response from server.';
          }
        }

      } catch (err) {
        if (err.response && err.response.data && err.response.data.message) {
          this.error = err.response.data.message;
        } else if (err.response && err.response.status === 401) {
            this.error = 'Invalid username or password.';
        }
        else {
          this.error = 'Login failed. Please try again.';
        }
        console.error('Login error:', err);
      } finally {
        this.isLoading = false;
      }
    },
  },
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.login-card {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
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

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 12px;
  background-color: #007bff;
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
  background-color: #0056b3;
}

.error-message {
  color: red;
  margin-bottom: 15px;
  text-align: center;
}

.register-link {
  text-align: center;
  margin-top: 20px;
}
.register-link a {
  color: #28a745;
  text-decoration: none;
}
.register-link a:hover {
  text-decoration: underline;
}
</style>