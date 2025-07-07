<template>
  <div class="category-form-view">
    <h1>{{ isEditing ? 'Edit Category' : 'Create New Category' }}</h1>
    <form @submit.prevent="handleSubmit" class="category-form">
      <div v-if="error" class="error-message">{{ error }}</div>
      <div v-if="successMessage" class="success-message">{{ successMessage }}</div>

      <div class="form-group">
        <label for="name">Category Name:</label>
        <input type="text" id="name" v-model="category.name" required />
      </div>

      <div class="form-group">
        <label for="description">Description (Optional):</label>
        <textarea id="description" v-model="category.description"></textarea>
      </div>

      <div class="form-actions">
        <button type="submit" :disabled="isSubmitting" class="btn btn-primary">
          {{ isSubmitting ? 'Saving...' : (isEditing ? 'Update Category' : 'Create Category') }}
        </button>
        <router-link :to="{ name: 'categories-list' }" class="btn btn-secondary">Cancel</router-link>
      </div>
    </form>
  </div>
</template>

<script>
import apiClient from '@/services/api';
import { mapGetters, mapState } from 'vuex';

export default {
  name: 'CategoryFormView',
  props: {
    categoryId: {
      type: String,
      required: false,
    },
  },
  data() {
    return {
      category: {
        id: null,
        name: '',
        description: '',
      },
      isEditing: false,
      isLoadingData: false,
      isSubmitting: false,
      error: null,
      successMessage: null,
    };
  },
  computed: {
    ...mapGetters('auth', ['getToken', 'isAdmin']),
    ...mapState('auth', ['user']),
  },
  methods: {
    async fetchCategoryDetails() {
      if (!this.categoryId) return;
      this.isEditing = true;
      this.isLoadingData = true;
      this.error = null;
      try {
        const response = await apiClient.get(`/categories`); // Get all categories
        const foundCategory = response.data.find(cat => cat.id === this.categoryId);
        if (foundCategory) {
            this.category = { ...foundCategory };
        } else {
            this.error = "Category not found for editing.";
            // Optionally redirect or handle
             setTimeout(() => {
                this.$router.push({ name: 'categories-list' });
            }, 2000);
        }
      } catch (err) {
        this.error = 'Failed to load category details.';
         if (err.response && err.response.data && typeof err.response.data === 'string') {
            this.error = err.response.data;
        } else if (err.response && err.response.data && err.response.data.message) {
            this.error = err.response.data.message;
        }
        console.error('Error fetching category details:', err);
      } finally {
        this.isLoadingData = false;
      }
    },
    async handleSubmit() {
      this.isSubmitting = true;
      this.error = null;
      this.successMessage = null;

      if (!this.category.name.trim()) {
          this.error = "Category name cannot be empty.";
          this.isSubmitting = false;
          return;
      }

      const payload = {
        name: this.category.name,
        description: this.category.description,
      };

      try {
        const config = {
          headers: {
            'Authorization': `Bearer ${this.getToken}`
          }
        };

        if (this.isEditing) {
          await apiClient.put(`/categories/${this.category.id}`, payload, config);
          this.successMessage = 'Category updated successfully!';
        } else {
          await apiClient.post('/categories', payload, config);
          this.successMessage = 'Category created successfully!';
        }
        
        if (!this.isEditing) {
            this.category.name = '';
            this.category.description = '';
        }
        setTimeout(() => {
            this.$router.push({ name: 'categories-list' });
        }, 1500);

      } catch (err) {
        this.error = 'Failed to save category.';
         if (err.response && err.response.data && typeof err.response.data === 'string') {
            this.error = err.response.data;
        } else if (err.response && err.response.data && err.response.data.message) {
            this.error = err.response.data.message;
        } else if (err.response && err.response.data && err.response.data.errors) {
            this.error = Object.values(err.response.data.errors).join(', ');
        }
        console.error('Error saving category:', err.response || err);
      } finally {
        this.isSubmitting = false;
      }
    },
    resetForm() {
        this.category = { id: null, name: '', description: '' };
        this.isEditing = false;
        this.error = null;
        this.successMessage = null;
        this.isLoadingData = false;
        this.isSubmitting = false;
    }
  },
  created() {
    if (this.categoryId) {
      this.fetchCategoryDetails();
    } else {
        this.resetForm();
    }
  },
  watch: {
    categoryId(newId, oldId) {
      if (newId) {
        this.fetchCategoryDetails();
      } else if (oldId && !newId) { // Navigated away from edit to create
        this.resetForm();
      }
    },
     '$route.name'(newRouteName) {
      // Reset form if navigating to create from edit or vice-versa via direct URL change but same component
      if (newRouteName === 'category-create' && this.isEditing) {
        this.resetForm();
      }
    }
  }
};
</script>

<style scoped>
.category-form-view {
  max-width: 600px;
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

.category-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
}

.form-group input[type="text"],
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.form-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.btn {
  padding: 10px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1em;
  margin-left: 10px;
  text-decoration: none;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}
.btn-primary:hover {
  background-color: #0056b3;
}
.btn-primary:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}
.btn-secondary:hover {
  background-color: #545b62;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  padding: 10px;
  border: 1px solid #f5c6cb;
  border-radius: 4px;
  margin-bottom: 15px;
  text-align: center;
}

.success-message {
  background-color: #d4edda;
  color: #155724;
  padding: 10px;
  border: 1px solid #c3e6cb;
  border-radius: 4px;
  margin-bottom: 15px;
  text-align: center;
}
</style>