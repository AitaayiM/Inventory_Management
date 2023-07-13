$(document).ready(function() {
    if (localStorage.getItem("loggedIn")==false) {
        window.location.href = "/user/login"; // Rediriger vers la page de connexion
    }
});