// Comportamiento de los paneles. Va en un archivo aparte (sin onclick inline)
// para que la política de seguridad de contenido pueda bloquear scripts inline.
document.addEventListener('DOMContentLoaded', function () {

    // Acordeones: se abre uno a la vez; los muy largos usan scroll interno
    document.querySelectorAll('[data-accordion]').forEach(function (header) {
        header.setAttribute('role', 'button');
        header.setAttribute('tabindex', '0');
        header.setAttribute('aria-expanded', 'false');

        function toggle() {
            var content = header.nextElementSibling;
            var abierto = content.classList.contains('active') || content.classList.contains('active-with-scroll');

            document.querySelectorAll('.accordion-content').forEach(function (c) {
                c.classList.remove('active', 'active-with-scroll');
            });
            document.querySelectorAll('.accordion-icon').forEach(function (i) { i.classList.remove('active'); });
            document.querySelectorAll('[data-accordion]').forEach(function (h) { h.setAttribute('aria-expanded', 'false'); });

            if (!abierto) {
                content.classList.add(content.scrollHeight > 1000 ? 'active-with-scroll' : 'active');
                header.querySelector('.accordion-icon').classList.add('active');
                header.setAttribute('aria-expanded', 'true');
            }
        }

        header.addEventListener('click', toggle);
        header.addEventListener('keydown', function (e) {
            if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); toggle(); }
        });
        header._toggle = toggle;
    });

    // Si la URL apunta a un acordeón (#planes-section, tarjetas de resumen, volver tras guardar), se abre solo
    function abrirDesdeHash() {
        if (!location.hash) return;
        var destino = document.getElementById(location.hash.slice(1));
        if (!destino || !destino.classList.contains('accordion')) return;
        var header = destino.querySelector('[data-accordion]');
        var content = header.nextElementSibling;
        if (!content.classList.contains('active') && !content.classList.contains('active-with-scroll')) {
            header._toggle();
        }
        destino.scrollIntoView({ block: 'start' });
    }
    abrirDesdeHash();
    window.addEventListener('hashchange', abrirDesdeHash);

    // Confirmación antes de eliminar
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            if (!window.confirm(form.dataset.confirm)) e.preventDefault();
        });
    });

    // Botón "volver arriba"
    document.querySelectorAll('[data-scroll-top]').forEach(function (link) {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    });

    // Calculadora de IMC de la portada (misma fórmula y rangos que el panel del cliente)
    var formImc = document.getElementById('imc-form');
    if (formImc) {
        formImc.addEventListener('submit', function (e) {
            e.preventDefault();
            var peso = parseFloat(document.getElementById('imc-peso').value);
            var alturaCm = parseFloat(document.getElementById('imc-altura').value);
            var resultado = document.getElementById('imc-result');
            if (!(peso >= 20 && peso <= 400) || !(alturaCm >= 100 && alturaCm <= 250)) {
                resultado.hidden = false;
                resultado.className = 'imc-result';
                document.getElementById('imc-valor').textContent = '—';
                document.getElementById('imc-categoria').textContent = 'Revisa los datos: peso entre 20 y 400 kg, estatura entre 100 y 250 cm.';
                document.getElementById('imc-ideal').textContent = '';
                return;
            }
            var altura = alturaCm / 100;
            var imc = peso / (altura * altura);
            var categoria, clase;
            if (imc < 18.5) { categoria = 'Bajo peso'; clase = 'imc-bajo'; }
            else if (imc < 25) { categoria = 'Peso normal'; clase = 'imc-normal'; }
            else if (imc < 30) { categoria = 'Sobrepeso'; clase = 'imc-sobrepeso'; }
            else { categoria = 'Obesidad'; clase = 'imc-obesidad'; }
            resultado.hidden = false;
            resultado.className = 'imc-result ' + clase;
            document.getElementById('imc-valor').textContent = imc.toFixed(1).replace('.', ',');
            document.getElementById('imc-categoria').textContent = categoria;
            document.getElementById('imc-ideal').textContent =
                'Tu peso de referencia (IMC 22,5) sería de ' + (22.5 * altura * altura).toFixed(1).replace('.', ',') + ' kg.';
        });
    }

    var busqueda = document.getElementById('searchDocumento');
    if (busqueda && busqueda.value === '') busqueda.focus();
});
