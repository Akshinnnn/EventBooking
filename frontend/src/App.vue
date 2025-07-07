<template>
  <div id="app-container">
    <AppNavbar />
    <main class="main-content">
      <div class="container">
        <router-view :key="$route.fullPath" />
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<script>
import AppNavbar from './components/layout/AppNavbar.vue';
import AppFooter from './components/layout/AppFooter.vue';

export default {
  name: 'App',
  components: {
    AppNavbar,
    AppFooter
  },
  created() {
    console.log('App component created. Attempting to restore login session.');

    // Dispatch tryAutoLogin to rehydrate auth state from localStorage
    // This action should set the Vuex state (token, user, isAuthenticated)
    // but should generally NOT perform navigations itself.
    this.$store.dispatch('auth/tryAutoLogin').then(isLoggedIn => {
      if (isLoggedIn) {
        console.log('App.vue: User session restored from localStorage.');
        // No automatic navigation here. Let the current route load.
        // If the current route requires auth and the user is now logged in, it will proceed.
        // If the user was on a specific page, they should remain there.
      } else {
        console.log('App.vue: No valid session found in localStorage.');
        // No automatic navigation here either.
        // If the user is on a protected route, the router's navigation guards
        // will handle redirecting them to login if necessary.
        // If they are on a public page, they will remain there.
      }
    }).catch(error => {
        console.error('App.vue: Error during tryAutoLogin:', error);
        // Still no navigation, let router guards handle it.
    });
  }
};
</script>

<style>
body {
  margin: 0;
  font-family: 'Arial', sans-serif;
  background-color: #f4f4f4;
  color: #333;
}

#app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex-grow: 1;
  padding-top: 20px;
  padding-bottom: 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

a {
  color: #007bff;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}

button {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background-color: #007bff;
  color: white;
  font-size: 1rem;
}

button:hover {
  background-color: #0056b3;
}
</style>