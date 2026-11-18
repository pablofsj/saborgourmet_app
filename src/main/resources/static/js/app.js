(function () {
    'use strict';

    var raiz = document.documentElement;
    var boton = document.querySelector('[data-theme-toggle]');

    if (boton) {
        boton.addEventListener('click', function () {
            var actual = raiz.dataset.theme === 'dark' ? 'light' : 'dark';
            raiz.dataset.theme = actual;
            localStorage.setItem('sg-theme', actual);
        });
    }

    // Confirmación antes de acciones destructivas (cancelar reservas, desactivar mesas).
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (evento) {
            if (!window.confirm(form.dataset.confirm)) {
                evento.preventDefault();
            }
        });
    });
})();
