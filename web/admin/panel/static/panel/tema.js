(function () {
  var colores = {
    morado: ['#7C4DFF', '#6236FF', '#FFFFFF'],
    verde: ['#00E676', '#00A152', '#00210F'],
    amarillo: ['#FFD600', '#E6B800', '#2A2300'],
    rojo: ['#FF1744', '#E0103A', '#FFFFFF'],
    azul: ['#2979FF', '#1E5FE0', '#FFFFFF']
  };

  function leer(clave, porDefecto) {
    try {
      return localStorage.getItem(clave) || porDefecto;
    } catch (e) {
      return porDefecto;
    }
  }

  function guardar(clave, valor) {
    try {
      localStorage.setItem(clave, valor);
    } catch (e) {}
  }

  function aplicar() {
    var modo = leer('pidecerca_admin_modo', 'oscuro');
    var color = colores[leer('pidecerca_admin_color', 'morado')] || colores.morado;
    var raiz = document.documentElement;
    raiz.dataset.tema = modo;
    var marca = modo === 'oscuro' ? color[0] : color[1];
    raiz.style.setProperty('--marca', marca);
    raiz.style.setProperty('--marca-texto', color[2]);
    raiz.style.setProperty('--marca-suave', marca + '24');
    document.querySelectorAll('[data-color]').forEach(function (b) {
      b.classList.toggle('activo', b.getAttribute('data-color') === leer('pidecerca_admin_color', 'morado'));
    });
    document.querySelectorAll('[data-icono-modo]').forEach(function (el) {
      el.textContent = modo === 'oscuro' ? '☀' : '☾';
    });
  }

  aplicar();

  document.addEventListener('DOMContentLoaded', function () {
    aplicar();
    document.querySelectorAll('[data-alternar-modo]').forEach(function (boton) {
      boton.addEventListener('click', function () {
        guardar('pidecerca_admin_modo', leer('pidecerca_admin_modo', 'oscuro') === 'oscuro' ? 'claro' : 'oscuro');
        aplicar();
      });
    });
    document.querySelectorAll('[data-color]').forEach(function (boton) {
      boton.addEventListener('click', function () {
        guardar('pidecerca_admin_color', boton.getAttribute('data-color'));
        aplicar();
      });
    });
    document.querySelectorAll('form[data-confirmar]').forEach(function (formulario) {
      formulario.addEventListener('submit', function (evento) {
        if (!window.confirm(formulario.getAttribute('data-confirmar'))) evento.preventDefault();
      });
    });
    document.querySelectorAll('input[name="modo_propietario"]').forEach(function (radio) {
      radio.addEventListener('change', function () {
        document.querySelectorAll('[data-modo]').forEach(function (bloque) {
          bloque.classList.toggle('oculto', bloque.getAttribute('data-modo') !== radio.value);
        });
      });
    });
  });
})();
