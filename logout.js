// Clear client-side session variables
localStorage.removeItem('user_logged_in');
localStorage.removeItem('user_name');
localStorage.removeItem('user_loyalty');
sessionStorage.clear();

// Redirect to login page
window.location.href = 'login.html';