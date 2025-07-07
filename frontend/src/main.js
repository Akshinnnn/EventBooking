import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';   

const mountApp = () => {
  const app = createApp(App);
  app.use(store);
  app.use(router);
  app.mount('#app');
};

store.dispatch('auth/tryAutoLogin')
  .then(() => {
    console.log("tryAutoLogin finished in main.js. App is now mounting.");
  })
  .catch(error => {
    console.error("Critical error during initial auth/tryAutoLogin in main.js:", error);
  })
  .finally(() => {
    mountApp();
  });