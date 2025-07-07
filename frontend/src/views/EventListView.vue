<template>
  <div class="event-list-container">
    <div class="filters-container">
      <div class="search-box-container">
        <input
          type="text"
          v-model.trim="searchQuery"
          placeholder="Search all events..."
          @input="debouncedFetchEvents"
          class="search-input"
        />
      </div>
      <div class="category-filter-container">
        <select v-model="selectedCategoryID" @change="applyFilters" class="category-select">
          <option value="">All Categories</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </div>
    </div>

    <div v-if="isLoading" class="loading-message">
      <p>Loading events...</p>
    </div>

    <div v-if="error" class="error-message">
      <p>Sorry, we couldn't fetch the events. Please try again later.</p>
      <p class="error-details"><i>{{ error }}</i></p>
    </div>

    <div v-if="!isLoading && !error && allFilteredEvents.length === 0" class="no-events-message">
      <p>No events match your criteria or none are currently available. Check back soon!</p>
    </div>

    <div v-if="!isLoading && !error && allFilteredEvents.length > 0">
      <section class="event-section upcoming-events">
        <h2>Upcoming Events</h2>
        <div v-if="upcomingEvents.length > 0" class="events-grid">
          <div v-for="event in upcomingEvents" :key="event.id" class="event-card" :class="{ 'cancelled-event-card': isEventCancelled(event) }">
            <div class="event-card-content">
              <div class="event-title-container">
                <h3 class="event-title" :class="{ 'cancelled-event-title': isEventCancelled(event) }">
                  {{ event.title }}
                </h3>
                <span v-if="isEventCancelled(event)" class="event-status-badge cancelled">
                  CANCELLED
                </span>
              </div>
              <p class="event-category"><strong>Category:</strong> {{ formatCategoryName(event.categoryName || event.category) }}</p>
              <p class="event-date">
                <strong>Date:</strong> {{ formatDate(event.startDateTime) }}
              </p>
              <p class="event-location">
                <strong>Location:</strong> {{ event.city }}, {{ event.country }}
              </p>
              <p class="event-price">
                <strong>Price:</strong> {{ formatPrice(event.price) }}
              </p>
              <p class="event-capacity" v-if="event.capacity && event.capacity > 0">
                <strong>Capacity:</strong> {{ event.capacity }}
              </p>
              <router-link :to="{ name: 'event-detail', params: { id: event.id } }" class="details-button">
                View Details
              </router-link>
            </div>
          </div>
        </div>
        <div v-else class="no-events-in-section-message">
          <p>No upcoming events match your criteria.</p>
        </div>
      </section>

      <section class="event-section previous-events">
        <h2>Previous Events</h2>
        <div v-if="previousEvents.length > 0" class="events-grid">
          <div v-for="event in previousEvents" :key="event.id" class="event-card" :class="{ 'cancelled-event-card': isEventCancelled(event), 'past-event-card': true }">
            <div class="event-card-content">
              <div class="event-title-container">
                <h3 class="event-title" :class="{ 'cancelled-event-title': isEventCancelled(event) }">
                  {{ event.title }}
                </h3>
                <span v-if="isEventCancelled(event)" class="event-status-badge cancelled">
                  CANCELLED
                </span>
                 <span v-else class="event-status-badge past">
                  PAST
                </span>
              </div>
              <p class="event-category"><strong>Category:</strong> {{ formatCategoryName(event.categoryName || event.category) }}</p>
              <p class="event-date">
                <strong>Date:</strong> {{ formatDate(event.startDateTime) }}
              </p>
              <p class="event-location">
                <strong>Location:</strong> {{ event.city }}, {{ event.country }}
              </p>
              <p class="event-price">
                <strong>Price:</strong> {{ formatPrice(event.price) }}
              </p>
              <router-link :to="{ name: 'event-detail', params: { id: event.id } }" class="details-button">
                View Details
              </router-link>
            </div>
          </div>
        </div>
        <div v-else class="no-events-in-section-message">
          <p>No previous events match your criteria.</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import _ from 'lodash';

export default {
  name: 'EventListView',
  data() {
    return {
      allFetchedEvents: [],
      isLoading: false,
      error: null,
      searchQuery: '',
      selectedCategoryID: '',
      categories: [],
    };
  },
  computed: {
    allFilteredEvents() {
        if (!this.allFetchedEvents) return [];
        let events = this.allFetchedEvents;
        if (this.searchQuery) {
            const searchTerm = this.searchQuery.toLowerCase();
            events = events.filter(event =>
                (event.title && event.title.toLowerCase().includes(searchTerm)) ||
                (event.description && event.description.toLowerCase().includes(searchTerm))
            );
        }
        return events;
    },
    upcomingEvents() {
      return this.allFilteredEvents.filter(event => !this.isEventPast(event)).sort((a,b) => new Date(a.startDateTime) - new Date(b.startDateTime));
    },
    previousEvents() {
      return this.allFilteredEvents.filter(event => this.isEventPast(event)).sort((a,b) => new Date(b.startDateTime) - new Date(a.startDateTime));
    }
  },
  methods: {
    async fetchEvents() {
      this.isLoading = true;
      this.error = null;
      try {
        const params = {};
        if (this.searchQuery && !this.selectedCategoryID) { 
             params.search = this.searchQuery;
        }
        if (this.selectedCategoryID) {
          params.categoryID = this.selectedCategoryID;
           if(this.searchQuery) params.search = this.searchQuery; 
        } else if (this.searchQuery) {
             params.search = this.searchQuery;
        }


        const response = await apiClient.get('/events', { params });
        this.allFetchedEvents = Array.isArray(response.data) ? response.data : [];
      } catch (err) {
        console.error('Failed to fetch events:', err.response || err);
        this.error = (err.response?.data?.message || err.response?.data || err.message) || 'An unknown error occurred while fetching events.';
        this.allFetchedEvents = [];
      } finally {
        this.isLoading = false;
      }
    },
    async fetchCategories() {
      try {
        const response = await apiClient.get('/categories');
        this.categories = Array.isArray(response.data) ? response.data : [];
      } catch (err) {
        console.error('Failed to fetch categories:', err.response || err);
      }
    },
    applyFilters() {
      this.fetchEvents();
    },
    debouncedFetchEvents: _.debounce(function() {
      this.fetchEvents();
    }, 500),
    formatDate(dateTimeString) {
      if (!dateTimeString) return 'N/A';
      const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: true };
      try {
        return new Date(dateTimeString).toLocaleString(undefined, options);
      } catch(e) { return dateTimeString; }
    },
    formatPrice(price) {
      if (price === null || price === undefined || price === '') return 'Free or TBD';
      const numPrice = Number(price);
      return isNaN(numPrice) ? 'Invalid Price' : `$${numPrice.toFixed(2)}`;
    },
    formatCategoryName(category) {
      if (!category) return 'Uncategorized';
      if (typeof category === 'object' && category.name) {
        return category.name.charAt(0).toUpperCase() + category.name.slice(1).toLowerCase().replace(/_/g, ' ');
      }
      if (typeof category === 'string') {
         return category.charAt(0).toUpperCase() + category.slice(1).toLowerCase().replace(/_/g, ' ');
      }
      return 'Uncategorized';
    },
    isEventCancelled(event) {
      return event && event.status && event.status.toUpperCase() === 'CANCELLED';
    },
    isEventPast(event) {
      if (!event) return false;
      const dateToCompare = event.endDateTime || event.startDateTime;
      if (!dateToCompare) return false;
      try {
        return new Date(dateToCompare) < new Date();
      } catch (e) {
        return false;
      }
    }
  },
  created() {
    this.fetchEvents();
    this.fetchCategories();
  },
  beforeUnmount() {
    if (this.debouncedFetchEvents && typeof this.debouncedFetchEvents.cancel === 'function') {
      this.debouncedFetchEvents.cancel();
    }
  }
};
</script>

<style scoped>
.event-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Arial', sans-serif;
}

.filters-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  flex-wrap: wrap;
  gap: 15px;
}

.search-box-container {
  flex-grow: 1;
  min-width: 250px;
}

.search-input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 1rem;
  box-sizing: border-box;
}
.search-input:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
  outline: none;
}

.category-filter-container {
  min-width: 200px;
}

.category-select {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  background-color: #fff;
  font-size: 1rem;
  box-sizing: border-box;
  cursor: pointer;
}
.category-select:focus {
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
  outline: none;
}

.loading-message,
.error-message,
.no-events-message,
.no-events-in-section-message {
  text-align: center;
  padding: 20px;
  font-size: 1.2em;
  margin-top: 10px;
  border-radius: 5px;
}
.error-message {
  color: #D8000C;
  background-color: #FFD2D2;
  border: 1px solid #D8000C;
}
.error-details {
  font-size: 0.9em;
  color: #555;
}
.no-events-message, .no-events-in-section-message {
    background-color: #eef7ff;
    color: #0056b3;
    border: 1px solid #b8d4ef;
}
.no-events-in-section-message {
    padding: 15px;
    font-size: 1em;
    margin-top: 0;
}


.event-section {
    margin-bottom: 40px;
}
.event-section h2 {
    font-size: 1.8em;
    color: #333;
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 2px solid #007bff;
}


.events-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 25px;
}

.event-card {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.07);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}
.event-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}
.cancelled-event-card {
    border-left: 5px solid #c0392b; 
    background-color: #fff7f7;
}
.past-event-card {
    opacity: 0.85;
    border-left: 5px solid #7f8c8d; 
}
.cancelled-event-card.past-event-card { 
    border-left-color: #c0392b;
}


.event-card-content {
  padding: 20px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.event-title-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 10px;
}

.event-title {
  font-size: 1.3em;
  margin-top: 0;
  margin-bottom: 0;
  color: #007bff;
  font-weight: 600;
  flex-grow: 1;
  word-break: break-word;
}

.cancelled-event-title {
  color: #c0392b; 
}

.event-status-badge {
  font-size: 0.8em;
  font-weight: bold;
  padding: 4px 8px;
  border-radius: 12px;
  color: white;
  white-space: nowrap;
  flex-shrink: 0;
}

.event-status-badge.cancelled {
  background-color: #c0392b; 
}
.event-status-badge.past {
  background-color: #7f8c8d; 
}


.event-card-content p {
  font-size: 0.9em;
  margin-bottom: 10px;
  color: #555;
  line-height: 1.5;
}
.event-card-content p strong {
  color: #333;
}

.details-button {
  display: inline-block;
  margin-top: auto;
  padding: 10px 15px;
  background-color: #007bff;
  color: white;
  text-align: center;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
  align-self: flex-start;
}
.details-button:hover {
  background-color: #0056b3;
}

@media (max-width: 768px) {
    .filters-container {
        flex-direction: column;
        align-items: stretch;
    }
    .search-box-container, .category-filter-container {
        min-width: 100%;
    }
}
</style>