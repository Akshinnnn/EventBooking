<template>
  <div class="home-view">
    <h1>Welcome to the Event Booking Platform!</h1>
    <p>Find and book amazing events happening near you.</p>
    <router-link to="/events" class="cta-button">Explore Events</router-link>
  </div>
</template>

<script>
export default {
  name: 'HomeView'
};
</script>

<style scoped>
.home-view {
  text-align: center;
  padding: 40px 20px;
}
.cta-button {
  display: inline-block;
  margin-top: 20px;
  padding: 12px 25px;
  background-color: #28a745;
  color: white;
  text-decoration: none;
  font-size: 1.1rem;
  border-radius: 5px;
  transition: background-color 0.3s ease;
}
.cta-button:hover {
  background-color: #218838;
}
</style>