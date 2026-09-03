// Replaces cancel-order.php
function cancelActiveOrder() {
    localStorage.removeItem('active_order');
    window.location.href = 'menu.html';
}

// Automatically execute if navigated to directly
if (window.location.pathname.endsWith('cancel-order.html')) {
    cancelActiveOrder();
}