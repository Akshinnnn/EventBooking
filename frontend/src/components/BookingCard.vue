<template>
  <div class="booking-card">
    <div class="booking-details">
      <p><strong>Booking ID:</strong> {{ booking.id }}</p>
      <p><strong>Event ID:</strong> <router-link :to="{ name: 'event-detail', params: { id: booking.eventID } }">{{ booking.eventID }}</router-link></p>
      <p><strong>Status:</strong> <span :class="statusClass">{{ booking.status }}</span></p>
      <p><strong>Booked On:</strong> {{ formattedDate }}</p>
    </div>
    <div class="booking-actions">
      <button
        v-if="canCancelBooking"
        @click="$emit('cancel-booking', booking.id)"
        :disabled="isCancelling"
        class="btn-cancel"
      >
        {{ isCancelling ? 'Cancelling...' : 'Cancel Booking' }}
      </button>
      <span v-else-if="booking.status === 'CANCELLED'" class="status-cancelled-text">
        Cancelled
      </span>
      <span v-else class="status-non-cancellable-text">
        Cannot Cancel
      </span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BookingCard',
  props: {
    booking: {
      type: Object,
      required: true,
    },
    isCancelling: { 
      type: Boolean,
      default: false,
    }
  },
  computed: {
    formattedDate() {
      if (!this.booking.createdAt) return 'N/A';
      const options = { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' };
      return new Date(this.booking.createdAt).toLocaleDateString(undefined, options);
    },
    statusClass() {
      return `status-${this.booking.status?.toLowerCase()}`;
    },
    canCancelBooking() {
      return ['PENDING', 'CONFIRMED'].includes(this.booking.status);
    }
  },
  emits: ['cancel-booking'], 
};
</script>

<style scoped>
.booking-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
  flex-wrap: wrap; 
}

.booking-card:last-child {
  border-bottom: none;
}

.booking-details {
  flex-grow: 1;
  margin-right: 20px; 
}

.booking-details p {
  margin: 5px 0;
  color: #555;
}

.booking-details p strong {
  color: #333;
  min-width: 100px;
  display: inline-block;
}

.booking-actions {
  margin-top: 10px; 
}


.status-pending { font-weight: bold; color: #ffa500; }
.status-confirmed { font-weight: bold; color: #28a745; }
.status-cancelled { font-weight: bold; color: #dc3545; }
.status-rejected { font-weight: bold; color: #6c757d; } 

.status-cancelled-text, .status-non-cancellable-text {
  font-style: italic;
  color: #6c757d;
  font-size: 0.9em;
}

.btn-cancel {
  padding: 8px 15px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s ease;
}

.btn-cancel:hover:not(:disabled) {
  background-color: #c82333;
}

.btn-cancel:disabled {
  background-color: #aaa;
  cursor: not-allowed;
}

@media (max-width: 600px) {
  .booking-card {
    flex-direction: column;
    align-items: flex-start;
  }
  .booking-actions {
    width: 100%;
    text-align: left; 
    margin-top: 15px;
  }
   .booking-details p strong {
     min-width: 80px;
   }
}
</style>