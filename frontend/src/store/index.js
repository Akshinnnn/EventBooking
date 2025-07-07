import { createStore } from 'vuex';
import authModule from './modules/auth.js';

export default createStore({
  modules: {
    auth: authModule
  },
  state: {
    appName: 'Event Booking Platform'
  },
  getters: {
    getAppName: (state) => state.appName
  },
  mutations: {},
  actions: {},
  strict: process.env.NODE_ENV !== 'production' 
});