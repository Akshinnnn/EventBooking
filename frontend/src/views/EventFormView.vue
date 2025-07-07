<template>
  <div class="event-form-container">
    <h1>{{ isEditMode ? 'Update Event' : 'Create New Event' }}</h1>

    <div v-if="loadingError" class="error-message">{{ loadingError }}</div>
    <div v-if="isLoading || isLoadingCategories" class="loading-message"> {{ isLoadingCategories ? 'Loading categories...' : 'Loading event data...' }}
    </div>

    <form @submit.prevent="handleSubmit" class="event-form" v-if="!isLoadingCategories && !loadingErrorCategories">
      <div class="form-grid">
        <div class="form-group full-width">
          <label for="title">Event Title:</label>
          <input type="text" id="title" v-model="eventData.title" required />
        </div>

        <div class="form-group full-width">
          <label for="description">Description:</label>
          <textarea id="description" v-model="eventData.description" rows="5" required></textarea>
        </div>

        <div class="form-group">
          <label for="categoryID">Category:</label> <select id="categoryID" v-model="eventData.categoryID" required> <option disabled value="">Please select one</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id"> {{ cat.name }} </option>
          </select>
          <div v-if="loadingErrorCategories" class="error-message small-error">Failed to load categories.</div>
        </div>
        <div class="form-group">
          <label for="price">Price (USD):</label>
          <input type="number" id="price" v-model.number="eventData.price" min="0" step="0.01" /> </div>

        <div class="form-group">
          <label for="startDateTime">Start Date & Time:</label>
          <input type="datetime-local" id="startDateTime" v-model="eventData.startDateTime" required />
        </div>
        <div class="form-group">
          <label for="capacity">Capacity:</label>
          <input type="number" id="capacity" v-model.number="eventData.capacity" min="0" step="1" /> </div>

        <h3 class="form-section-title full-width">Location</h3>
        <div class="form-group">
          <label for="street">Street:</label>
          <input type="text" id="street" v-model="eventData.street" />
        </div>
        <div class="form-group">
          <label for="city">City:</label>
          <input type="text" id="city" v-model="eventData.city" required />
        </div>
        <div class="form-group">
          <label for="state">State/Province:</label>
          <input type="text" id="state" v-model="eventData.state" />
        </div>
        <div class="form-group">
          <label for="zipCode">Zip/Postal Code:</label>
          <input type="text" id="zipCode" v-model="eventData.zipCode" />
        </div>
        <div class="form-group full-width">
          <label for="country">Country:</label>
          <input type="text" id="country" v-model="eventData.country" required />
        </div>
      </div>

      <p v-if="submitError" class="error-message">{{ submitError }}</p>
      <p v-if="submitSuccess" class="success-message">{{ submitSuccess }}</p>

      <div class="form-actions">
        <button type="submit" :disabled="isSubmitting">
          {{ isSubmitting ? 'Saving...' : (isEditMode ? 'Update Event' : 'Create Event') }}
        </button>
        <router-link :to="isEditMode && id ? { name: 'event-detail', params: { id: id } } : { name: 'event-list' }" class="cancel-link"> Cancel
        </router-link>
      </div>
    </form>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import { jwtDecode } from 'jwt-decode'; 

export default {
  name: 'EventFormView',
  props: {
    id: { 
      type: String,
      default: null,
    },
  },
  data() {
    return {
      eventData: {
        title: '',
        description: '',
        categoryID: '', 
        price: null, 
        street: '',
        city: '',
        state: '',
        zipCode: '',
        country: '',
        startDateTime: '',
        capacity: null, 
        
      },
      categories: [],
      isLoading: false, 
      loadingError: null, 
      isLoadingCategories: false,
      loadingErrorCategories: null,
      isSubmitting: false,
      submitError: null,
      submitSuccess: null,
    };
  },
  computed: {
    isEditMode() {
      return !!this.id;
    },
  },
  methods: {
    resetFormState() {
      this.eventData = {
        title: '',
        description: '',
        categoryID: '', 
        price: null,
        street: '',
        city: '',
        state: '',
        zipCode: '',
        country: '',
        startDateTime: this.formatDateTimeForInput(new Date().toISOString()), // Default to now
        capacity: null,
      };
      this.submitError = null;
      this.submitSuccess = null;
      this.isSubmitting = false;
      this.isLoading = false;
      this.loadingError = null;
    },

    formatDateTimeForInput(dateTimeString) {
      if (!dateTimeString) return '';
      try {
        const date = new Date(dateTimeString);
        if (isNaN(date.getTime())) return '';
        const year = date.getFullYear();
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `${year}-${month}-${day}T${hours}:${minutes}`;
      } catch (e) {
        console.error("Error formatting date for input:", dateTimeString, e);
        return '';
      }
    },

    async fetchCategories() {
      console.log('Fetching categories...');
      this.isLoadingCategories = true;
      this.loadingErrorCategories = null;
      try {
        const response = await apiClient.get('/categories'); 
        this.categories = response.data; 
        console.log('Fetched categories:', this.categories);
      } catch (err) {
        console.error('Failed to fetch categories:', err.response || err);
        this.loadingErrorCategories = 'Could not load categories. Please try again.';
        this.categories = []; 
      } finally {
        this.isLoadingCategories = false;
      }
    },

    async fetchEventDetailsForEdit() {
      if (!this.id) return;
      console.log(`Edit mode: Fetching event with ID: ${this.id}`);
      this.isLoading = true;
      this.loadingError = null;
      try {
        const response = await apiClient.get(`/events/${this.id}`);
        const fetchedData = response.data;
        console.log("Fetched event data for edit:", fetchedData);

        this.eventData = {
          title: fetchedData.title || '',
          description: fetchedData.description || '',
          categoryID: fetchedData.categoryID || '', 
          price: fetchedData.price === null || fetchedData.price === undefined ? null : Number(fetchedData.price),
          street: fetchedData.street || '',
          city: fetchedData.city || '',
          state: fetchedData.state || '',
          zipCode: fetchedData.zipCode || '',
          country: fetchedData.country || '',
          startDateTime: this.formatDateTimeForInput(fetchedData.startDateTime),
          capacity: fetchedData.capacity === null || fetchedData.capacity === undefined ? null : Number(fetchedData.capacity),
        };
        console.log("Populated eventData for edit:", this.eventData);
      } catch (err) {
        console.error(`Failed to fetch event details for ID ${this.id}:`, err.response || err);
        this.loadingError = `Error loading event data: ${err.response?.data?.message || err.message || 'Please try again.'}`;
        this.eventData = {}; 
      } finally {
        this.isLoading = false;
      }
    },

    getCurrentUserRole() {
      const token = localStorage.getItem('jwtToken');
      if (token) {
        try {
          const decodedToken = jwtDecode(token);
          return decodedToken.roles;
        } catch (e) {
          console.error("Error decoding JWT from localStorage:", e);
          return null;
        }
      }
      return null;
    },

    async handleSubmit() {
      this.isSubmitting = true;
      this.submitError = null;
      this.submitSuccess = null;

      const userRole = this.getCurrentUserRole();
      const isOrganizer = Array.isArray(userRole) ? userRole.includes('ORGANIZER') : String(userRole).toUpperCase() === 'ORGANIZER';
      const isAdmin = Array.isArray(userRole) ? userRole.includes('ADMIN') : String(userRole).toUpperCase() === 'ADMIN';

      if (!isOrganizer && !isAdmin) {
        this.submitError = 'Only organizers or admins can create or update events.';
        this.isSubmitting = false;
        return;
      }

      const token = localStorage.getItem('jwtToken');
      if (!token) {
        this.submitError = 'Authentication required. Please log in.';
        this.isSubmitting = false;
        return;
      }

      const dataToSubmit = {
        ...this.eventData,
        price: this.eventData.price === null || this.eventData.price === '' ? 0 : Number(this.eventData.price),
        capacity: this.eventData.capacity === null || this.eventData.capacity === '' ? 0 : Number(this.eventData.capacity), 
        categoryID: this.eventData.categoryID, 
      };

      console.log('Submitting data:', dataToSubmit);

      const config = {
        headers: { Authorization: `Bearer ${token}` },
      };

      try {
        let response;
        let successMessage = '';
        let navigateToId = null;

        if (this.isEditMode) {
          console.log(`Updating event ID: ${this.id}`);
          response = await apiClient.put(`/events/${this.id}`, dataToSubmit, config);
          successMessage = 'Event updated successfully!';
          navigateToId = this.id;
        } else {
          console.log('Creating new event');
          response = await apiClient.post('/events', dataToSubmit, config);
          successMessage = 'Event created successfully!';
          navigateToId = response.data?.id; 
        }

        this.submitSuccess = successMessage;
        console.log('API Response:', response.data);

        if (navigateToId) {
          setTimeout(() => {
            this.$router.push({ name: 'event-detail', params: { id: navigateToId } });
          }, 1000); 
        } else {
          console.warn("Event ID not available after save. Navigating to list.");
          setTimeout(() => { this.$router.push({ name: 'EventList' }); }, 1000);
        }
      } catch (err) {
        console.error(`Failed to ${this.isEditMode ? 'update' : 'create'} event:`, err.response || err);
        let errorMessage = `Failed to ${this.isEditMode ? 'update' : 'create'} event.`;
        if (err.response && err.response.data) {
          if (typeof err.response.data === 'string') {
            errorMessage += ` ${err.response.data}`;
          } else {
            errorMessage += ` ${err.response.data.message || err.response.data.error || JSON.stringify(err.response.data)}`;
          }
        } else if (err.message) {
          errorMessage += ` ${err.message}`;
        } else {
          errorMessage += ' An unknown error occurred.';
        }
        this.submitError = errorMessage;
      } finally {
        this.isSubmitting = false;
      }
    },
  },
  async created() {
    console.log('EventFormView created. Edit mode:', this.isEditMode, 'ID:', this.id);
    await this.fetchCategories(); 

    if (this.isEditMode) {
      await this.fetchEventDetailsForEdit();
    } else {
      this.resetFormState(); 
    }
  },
};
</script>

<style scoped>
.event-form-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 25px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

h1 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
  font-size: 1.8em; 
}
.loading-message { 
    text-align: center;
    padding: 15px;
    color: #555;
    font-style: italic;
}
.error-message, .success-message {
    text-align: center;
    padding: 10px;
    margin: 15px 0; 
    border-radius: 4px;
}
.error-message.small-error { 
    font-size: 0.9em;
    padding: 5px;
    margin-top: 5px;
    margin-bottom: 0;
    background-color: transparent;
    color: #D8000C;
    border: none;
}

.error-message { color: #D8000C; background-color: #FFD2D2; border: 1px solid #D8000C; }
.success-message { color: #155724; background-color: #d4edda; border: 1px solid #c3e6cb;}


.event-form .form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px 25px; 
}

.form-group {
  display: flex;
  flex-direction: column;
}
.form-group.full-width {
  grid-column: 1 / -1; 
}

.form-section-title {
    grid-column: 1 / -1;
    font-size: 1.2em;
    color: #444; 
    margin-top: 25px; 
    margin-bottom: 5px; 
    padding-bottom: 8px;
    border-bottom: 1px solid #eee;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: bold;
  color: #555;
  font-size: 0.9rem;
}

.form-group input[type="text"],
.form-group input[type="number"],
.form-group input[type="email"],
.form-group input[type="tel"],
.form-group input[type="datetime-local"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-sizing: border-box;
  font-size: 1rem;
  transition: border-color 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
    border-color: #007bff;
    box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
    outline: none;
}

.form-group textarea {
    resize: vertical;
    min-height: 100px; 
}

.form-actions {
  grid-column: 1 / -1;
  margin-top: 35px; 
  display: flex;
  justify-content: flex-end; 
  gap: 15px;
}

.form-actions button,
.form-actions .cancel-link { 
  padding: 12px 28px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: background-color 0.2s ease, opacity 0.2s ease;
  text-decoration: none; 
  display: inline-flex; 
  align-items: center;
  justify-content: center;
}

.form-actions button[type="submit"] {
  background-color: #28a745; 
  color: white;
}
.form-actions button[type="submit"]:hover:not(:disabled) {
  background-color: #218838; 
}
.form-actions button:disabled {
    background-color: #ccc; 
    color: #666;
    cursor: not-allowed;
}

.form-actions .cancel-link { 
  background-color: #6c757d; 
  color: white;
}
.form-actions .cancel-link:hover {
  background-color: #5a6268; 
}

@media (max-width: 768px) {
  .event-form .form-grid {
    grid-template-columns: 1fr;
  }
  .form-actions {
      justify-content: center; 
      flex-direction: column-reverse; 
  }
  .form-actions button,
  .form-actions .cancel-link {
      width: 100%;
  }
}
</style>