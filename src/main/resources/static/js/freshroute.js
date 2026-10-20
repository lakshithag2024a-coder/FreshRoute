/**
 * FreshRoute Main Client Scripts
 */

document.addEventListener('DOMContentLoaded', function () {
    // 1. Auto-dismiss flash alerts after 6 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 6000);
    });

    // 2. Initialize Bootstrap Tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // 3. Quick Demo Login Helper
    window.fillDemoCredentials = function(username, password) {
        const userInput = document.getElementById('username');
        const passInput = document.getElementById('password');
        if (userInput && passInput) {
            userInput.value = username;
            passInput.value = password;
            userInput.classList.add('is-valid');
            passInput.classList.add('is-valid');
        }
    };

    // 4. Client-side Realtime Countdown Timer for Listings
    const countdownElements = document.querySelectorAll('[data-deadline]');
    countdownElements.forEach(function (el) {
        const deadlineStr = el.getAttribute('data-deadline');
        if (!deadlineStr) return;

        const deadlineTime = new Date(deadlineStr).getTime();

        function updateTimer() {
            const now = new Date().getTime();
            const distance = deadlineTime - now;

            if (distance <= 0) {
                el.innerHTML = '<span class="text-danger fw-bold"><i class="bi bi-clock-history me-1"></i>Expired</span>';
                return;
            }

            const days = Math.floor(distance / (1000 * 60 * 60 * 24));
            const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));

            if (days > 0) {
                el.innerHTML = `<i class="bi bi-clock me-1"></i>${days}d ${hours}h left`;
            } else if (hours > 0) {
                el.innerHTML = `<i class="bi bi-clock me-1"></i>${hours}h ${minutes}m left`;
            } else {
                el.innerHTML = `<span class="text-danger fw-bold animate-pulse"><i class="bi bi-exclamation-triangle-fill me-1"></i>${minutes}m left!</span>`;
            }
        }

        updateTimer();
        setInterval(updateTimer, 60000); // refresh every minute
    });
});
