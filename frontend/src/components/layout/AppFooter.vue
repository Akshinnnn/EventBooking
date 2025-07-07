<template>
  <footer class="app-footer">
    <p>&copy; {{ currentYear }} Event Booking Platform. All rights reserved.</p>
    <p>Team 2: Adil Musali, Akshin Mashadiyev, Tarlan Soltanov</p>
  </footer>
</template>

<script>
export default {
  name: 'AppFooter',
  data() {
    return {
      currentYear: new Date().getFullYear()
    };
  }
};
</script>

<style scoped>
.app-footer {
  background-color: #222;
  color: #ccc;
  text-align: center;
  padding: 1.5rem;
}

.app-footer p {
  margin: 0.25rem 0;
}
</style>