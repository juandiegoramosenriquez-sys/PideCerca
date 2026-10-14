import { createContext, useContext, useEffect, useState } from 'react'

export const COLORES = {
  morado: { nombre: 'Morado', oscuro: '#7C4DFF', claro: '#6236FF', texto: '#FFFFFF' },
  verde: { nombre: 'Verde', oscuro: '#00E676', claro: '#00A152', texto: '#00210F' },
  amarillo: { nombre: 'Amarillo', oscuro: '#FFD600', claro: '#E6B800', texto: '#2A2300' },
  rojo: { nombre: 'Rojo', oscuro: '#FF1744', claro: '#E0103A', texto: '#FFFFFF' },
  azul: { nombre: 'Azul', oscuro: '#2979FF', claro: '#1E5FE0', texto: '#FFFFFF' }
}

const ThemeContext = createContext(null)

function leer(clave, porDefecto) {
  try {
    return localStorage.getItem(clave) || porDefecto
  } catch {
    return porDefecto
  }
}

function guardar(clave, valor) {
  try {
    localStorage.setItem(clave, valor)
  } catch {
    return
  }
}

function modoPorHora() {
  const hora = new Date().getHours()
  return hora >= 6 && hora < 18 ? 'claro' : 'oscuro'
}

export function ThemeProvider({ children }) {
  const [color, setColor] = useState(() => leer('pidecerca_color', 'morado'))
  const [modo, setModo] = useState(() => leer('pidecerca_modo', 'oscuro'))
  const [modoAuto, setModoAuto] = useState(modoPorHora)

  useEffect(() => {
    if (modo !== 'auto') return
    const intervalo = setInterval(() => setModoAuto(modoPorHora()), 60000)
    return () => clearInterval(intervalo)
  }, [modo])

  const modoReal = modo === 'auto' ? modoAuto : modo
  const paleta = COLORES[color] || COLORES.morado

  useEffect(() => {
    const raiz = document.documentElement
    raiz.dataset.tema = modoReal
    raiz.style.setProperty('--marca', modoReal === 'oscuro' ? paleta.oscuro : paleta.claro)
    raiz.style.setProperty('--marca-texto', paleta.texto)
    raiz.style.setProperty('--marca-suave', (modoReal === 'oscuro' ? paleta.oscuro : paleta.claro) + '24')
    const meta = document.querySelector('meta[name="theme-color"]')
    if (meta) meta.setAttribute('content', modoReal === 'oscuro' ? '#0D1014' : '#F5F6F8')
  }, [modoReal, paleta])

  function cambiarColor(nuevo) {
    setColor(nuevo)
    guardar('pidecerca_color', nuevo)
  }

  function cambiarModo(nuevo) {
    setModo(nuevo)
    guardar('pidecerca_modo', nuevo)
  }

  function alternarModo() {
    cambiarModo(modoReal === 'oscuro' ? 'claro' : 'oscuro')
  }

  return (
    <ThemeContext.Provider value={{ color, modo, modoReal, cambiarColor, cambiarModo, alternarModo }}>
      {children}
    </ThemeContext.Provider>
  )
}

export function useTema() {
  return useContext(ThemeContext)
}
