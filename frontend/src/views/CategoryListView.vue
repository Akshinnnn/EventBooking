<template>
  <div class="categories-list-view">
    <h1>Categories</h1>
    <div v-if="isLoading" class="loading">Loading categories...</div>
    <div v-if="error" class="error-message">{{ error }}</div>

    <div v-if="isAdmin" class="admin-actions">
      <router-link :to="{ name: 'category-create' }" class="btn btn-primary">Add New Category</router-link>
    </div>

    <ul v-if="!isLoading && !error && categories.length" class="category-list">
      <li v-for="category in categories" :key="category.id" class="category-item">
        <div class="category-details">
          <h2>{{ category.name }}</h2>
          <p v-if="category.description">{{ category.description }}</p>
        </div>
        <div v-if="isAdmin" class="category-actions">
          <router-link :to="{ name: 'category-edit', params: { categoryId: category.id } }" class="btn btn-secondary">Edit</router-link>
          <button @click="confirmDeleteCategory(category.id)" class="btn btn-danger">Delete</button>
        </div>
      </li>
    </ul>
    <div v-if="!isLoading && !error && !categories.length" class="no-categories">
      No categories found.
    </div>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import { mapGetters, mapState } from 'vuex';

export default {
  name: 'CategoriesListView',
  data() {
    return {
      categories: [],
      isLoading: false,
      error: null,
    };
  },
  computed: {
    ...mapGetters('auth', ['isAdmin', 'getToken']),
    ...mapState('auth', ['user']),
  },
  methods: {
    async fetchCategories() {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.get('/categories');
        this.categories = response.data;
      } catch (err) {
        this.error = 'Failed to load categories.';
        if (err.response && err.response.data && typeof err.response.data === 'string') {
            this.error = err.response.data;
        } else if (err.response && err.response.data && err.response.data.message) {
            this.error = err.response.data.message;
        }
        console.error('Error fetching categories:', err);
      } finally {
        this.isLoading = false;
      }
    },
    async deleteCategory(categoryId) {
      this.isLoading = true;
      this.error = null;
      try {
        await apiClient.delete(`/categories/${categoryId}`, {
          headers: {
            'Authorization': `Bearer ${this.getToken}`
          }
        });
        this.categories = this.categories.filter(cat => cat.id !== categoryId);
      } catch (err) {
        this.error = 'Failed to delete category.';
        if (err.response && err.response.data && typeof err.response.data === 'string') {
            this.error = err.response.data;
        } else if (err.response && err.response.data && err.response.data.message) {
            this.error = err.response.data.message;
        }
        console.error('Error deleting category:', err);
      } finally {
        this.isLoading = false;
      }
    },
    confirmDeleteCategory(categoryId) {
      if (window.confirm('Are you sure you want to delete this category? This might affect existing events.')) {
        this.deleteCategory(categoryId);
      }
    }
  },
  created() {
    this.fetchCategories();
  },
};
</script>

<style scoped>
.categories-list-view {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
}

.admin-actions {
  margin-bottom: 20px;
  text-align: right;
}

.btn {
  padding: 10px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1em;
  margin-left: 10px;
  text-decoration: none;
  display: inline-block;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}
.btn-primary:hover {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}
.btn-secondary:hover {
  background-color: #545b62;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}
.btn-danger:hover {
  background-color: #b02a37;
}

.loading, .error-message, .no-categories {
  text-align: center;
  padding: 20px;
  font-size: 1.1em;
}

.error-message {
  color: red;
  background-color: #fdd;
  border: 1px solid red;
  border-radius: 4px;
  padding: 10px;
}

.category-list {
  list-style: none;
  padding: 0;
}

.category-item {
  background-color: white;
  padding: 15px;
  margin-bottom: 10px;
  border-radius: 5px;
  border: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-details h2 {
  margin: 0 0 5px 0;
  color: #007bff;
}

.category-details p {
  margin: 0;
  color: #555;
  font-size: 0.9em;
}

.category-actions button, .category-actions .btn {
  margin-left: 10px;
}
</style>