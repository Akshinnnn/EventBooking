<template>
  <div class="payment-list-container">
    <h1 class="page-title">My Payment History</h1>

    <div v-if="isLoading" class="loading-message">
      <p>Loading your payments...</p>
    </div>

    <div v-if="error" class="error-message">
      <p>Sorry, we couldn't fetch your payment history.</p>
      <p class="error-details"><i>{{ error }}</i></p>
    </div>

    <div v-if="!isLoading && !error && payments.length === 0" class="no-payments-message">
      <p>You have no payment history yet.</p>
      <router-link to="/events" class="action-link">Browse Events</router-link>
    </div>

    <div v-if="!isLoading && !error && payments.length > 0" class="payments-table-container">
      <table class="payments-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Booking ID</th>
            </tr>
        </thead>
        <tbody>
          <tr v-for="payment in sortedPayments" :key="payment.id">
            <td>{{ formatDate(payment.createdAt) }}</td>
            <td :class="getAmountClass(payment.amount)">{{ formatCurrency(payment.amount) }}</td>
            <td><span :class="['status-badge', payment.status ? payment.status.toLowerCase() : '']">{{ formatStatus(payment.status) }}</span></td>
            <td>{{ payment.bookingID || 'N/A (Balance Top-up)' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import apiClient from '@/services/api'; 
import { mapGetters } from 'vuex'; 

export default {
  name: 'PaymentListView',
  data() {
    return {
      payments: [],
      isLoading: true,
      error: null,
    };
  },
  computed: {
    ...mapGetters('auth', ['isLoggedIn']), 
    sortedPayments() {
      return [...this.payments].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    }
  },
  methods: {
    async fetchUserPayments() {
      this.isLoading = true;
      this.error = null;
      try {
        const response = await apiClient.get('/account/payments');
        this.payments = response.data || [];
      } catch (err) {
        console.error("Failed to fetch user payments:", err.response || err);
        if (err.response) {
          this.error = (err.response.data && (err.response.data.message || err.response.data)) || `Error ${err.response.status}: Could not load payments.`;
        } else if (err.request) {
          this.error = 'Network error. Could not connect to the server.';
        } else {
          this.error = 'An unexpected error occurred while fetching payments.';
        }
      } finally {
        this.isLoading = false;
      }
    },
    formatDate(dateTimeString) {
      if (!dateTimeString) return 'N/A';
      const options = { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
      try {
        return new Date(dateTimeString).toLocaleDateString(undefined, options);
      } catch (e) {
        return dateTimeString; 
      }
    },
    formatCurrency(amount) {
      if (amount === null || amount === undefined) return 'N/A';
      const formatter = new Intl.NumberFormat(undefined, { 
        style: 'currency',
        currency: 'USD', 
      });
      return formatter.format(amount);
    },
    getAmountClass(amount) {
        if (amount === null || amount === undefined) return '';
        return amount > 0 ? 'amount-positive' : (amount < 0 ? 'amount-negative' : '');
    },
    formatStatus(status) {
        if (!status) return 'Unknown';
        return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase();
    }
  },
  created() {
    if (this.isLoggedIn) { 
      this.fetchUserPayments();
    } else {
      this.error = "You must be logged in to view your payments.";
      this.isLoading = false;
    }
  },
};
</script>

<style scoped>
.payment-list-container {
  max-width: 900px;
  margin: 2rem auto;
  padding: 2rem;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
}

.page-title {
  font-size: 2rem;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  text-align: center;
  border-bottom: 2px solid #eaecef;
  padding-bottom: 0.5rem;
}

.loading-message,
.error-message,
.no-payments-message {
  text-align: center;
  padding: 2rem;
  margin-top: 1rem;
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-size: 1.1rem;
}

.error-message {
  color: #721c24;
  background-color: #f8d7da;
  border-color: #f5c6cb;
}
.error-details {
  font-size: 0.9em;
  color: #5a161b;
  margin-top: 0.5rem;
}

.no-payments-message .action-link {
    display: inline-block;
    margin-top: 1rem;
    padding: 0.6rem 1.2rem;
    background-color: #007bff;
    color: white;
    text-decoration: none;
    border-radius: 5px;
    transition: background-color 0.2s ease;
}
.no-payments-message .action-link:hover {
    background-color: #0056b3;
}

.payments-table-container {
  margin-top: 1.5rem;
  overflow-x: auto; 
}

.payments-table {
  width: 100%;
  border-collapse: collapse;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  overflow: hidden; 
}

.payments-table th,
.payments-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eaecef;
}

.payments-table th {
  background-color: #f0f2f5; 
  color: #333;
  font-weight: 600;
  font-size: 0.9rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.payments-table tbody tr:last-child td {
  border-bottom: none;
}

.payments-table tbody tr:hover {
  background-color: #f9f9f9;
}

.amount-positive {
    color: #28a745; 
    font-weight: 500;
}
.amount-negative {
    color: #dc3545;
    font-weight: 500;
}


.status-badge {
    padding: 0.3em 0.7em;
    border-radius: 12px; 
    font-weight: 500;
    font-size: 0.8em;
    color: white;
    text-transform: capitalize;
    display: inline-block;
}

.status-badge.approved { background-color: #28a745; } 
.status-badge.rejected { background-color: #dc3545; } 
.status-badge.refunded { background-color: #17a2b8; } 
.status-badge.pending { background-color: #ffc107; color: #212529;} 

</style>