import apiClient from '@/services/api'; 
import { jwtDecode } from 'jwt-decode';

export default {
  namespaced: true,
  state: {
    token: localStorage.getItem('jwtToken') || null,
    user: null,
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token;
      if (token) {
        localStorage.setItem('jwtToken', token);
      } else {
        localStorage.removeItem('jwtToken'); // Ensure removal if token is null/undefined
      }
    },
    SET_USER(state, userData) {
      state.user = userData;
    },
    CLEAR_AUTH(state) {
      state.token = null;
      state.user = null;
      localStorage.removeItem('jwtToken');
      delete apiClient.defaults.headers.common['Authorization'];
    },
  },
  actions: {
    async processLoginToken({ commit, dispatch }, token) {
      commit('SET_TOKEN', token);
      try {
        const response = await apiClient.get('/auth/authenticate');
        const isTokenValidByBackend = response.data;

        if (isTokenValidByBackend) {
          const decodedToken = jwtDecode(token);
          let rolesArray = [];
          if (decodedToken.roles) {
            if (Array.isArray(decodedToken.roles)) {
              rolesArray = decodedToken.roles;
            } else if (typeof decodedToken.roles === 'string') {
              
              rolesArray = decodedToken.roles.includes(',') ? decodedToken.roles.split(',').map(r => r.trim()) : [decodedToken.roles.trim()];
            }
          }

          const userData = {
            roles: rolesArray, 
            userID: decodedToken.userID,
            email: decodedToken.email, 
          };
          commit('SET_USER', userData);
          console.log("User data set in store:", userData);
          return true; 
        } else {
          console.warn('Backend invalidated the token during processLoginToken.');
          dispatch('logout'); 
          return false; 
        }
      } catch (error) {
        console.error("Error during token authentication or decoding (processLoginToken):", error);
        dispatch('logout'); 
        return false; 
      }
    },

    logout({ commit }) {
      commit('CLEAR_AUTH');
      
    },

    async tryAutoLogin({ commit, dispatch, state }) { 
      const token = state.token; 
      if (!token) {
        return false;
      }
      
      if (!apiClient.defaults.headers.common['Authorization']) {
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      }
      
      try {
        const response = await apiClient.get('/auth/authenticate');
        const isTokenValidByBackend = response.data;

        if (isTokenValidByBackend) {
          const decodedToken = jwtDecode(token);
          let rolesArray = [];
          if (decodedToken.roles) {
            if (Array.isArray(decodedToken.roles)) {
              rolesArray = decodedToken.roles;
            } else if (typeof decodedToken.roles === 'string') {
              rolesArray = decodedToken.roles.includes(',') ? decodedToken.roles.split(',').map(r => r.trim()) : [decodedToken.roles.trim()];
            }
          }
          const userData = {
            email: decodedToken.email, 
            roles: rolesArray,
            userID: decodedToken.userID,

          };
          commit('SET_TOKEN', token); 
          commit('SET_USER', userData);
          console.log("User data restored in store (tryAutoLogin):", userData);
          return true;
        } else {
          console.warn('Backend invalidated the token during tryAutoLogin.');
          dispatch('logout'); 
          return false;
        }
      } catch (error) {
        console.error("Auto-login authentication failed (tryAutoLogin):", error.response || error);
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
             dispatch('logout');
        }
        return false;
      }
    },
  },
  getters: {
    isLoggedIn: (state) => !!state.token && !!state.user,
    userRoles: (state) => (state.user && state.user.roles ? state.user.roles : []),
    isOrganizer: (state, getters) => getters.userRoles.includes('ORGANIZER'),
    isParticipant: (state, getters) => getters.userRoles.includes('PARTICIPANT'),
    isAdmin: (state, getters) => getters.userRoles.includes('ADMIN'), 
    currentUser: (state) => state.user, 
    getToken: (state) => state.token, 
  },
};