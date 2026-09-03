document.addEventListener('DOMContentLoaded', function() {
    const workspaceForm = document.getElementById('workspace-form');
    
    if (workspaceForm) {
        workspaceForm.addEventListener('submit', function(e) {
            e.preventDefault();

            const name = document.getElementById('name').value.trim();
            const email = document.getElementById('email').value.trim();
            const plan = document.getElementById('plan_type').value;
            const date = document.getElementById('booking_date').value;

            const selectedDate = new Date(date);
            const today = new Date();
            today.setHours(0,0,0,0);

            if (selectedDate < today) {
                alert('You cannot book a workspace for a past date.');
                return;
            }

            if (!name || !email || !date) {
                alert('All required fields must be filled.');
                return;
            }

            // Save booking details and redirect
            localStorage.setItem('booking_success', `Your ${plan} reservation for ${date} is confirmed!`);
            window.location.href = 'booking-success.html';
        });
    }
});