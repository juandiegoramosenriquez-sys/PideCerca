const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const CLAVE_TOKEN = 'pidecerca_token'

export function obtenerToken() {
  try {
    return localStorage.getItem(CLAVE_TOKEN)
  } catch {
    return null
  }
}

export function guardarToken(token) {
  try {
    if (token) localStorage.setItem(CLAVE_TOKEN, token)
    else localStorage.removeItem(CLAVE_TOKEN)
  } catch {
    return
  }
}

let alExpirarSesion = () => {}

export function configurarExpiracion(funcion) {
  alExpirarSesion = funcion
}

export async function api(ruta, { metodo = 'GET', cuerpo, consulta } = {}) {
  const url = new URL(API_URL + ruta)
  if (consulta) {
    Object.entries(consulta).forEach(([clave, valor]) => {
      if (valor !== undefined && valor !== null && valor !== '') url.searchParams.set(clave, valor)
    })
  }

  const cabeceras = { Accept: 'application/json' }
  const token = obtenerToken()
  if (token) cabeceras.Authorization = `Bearer ${token}`
  if (cuerpo !== undefined) cabeceras['Content-Type'] = 'application/json'

  let respuesta
  try {
    respuesta = await fetch(url, {
      method: metodo,
      headers: cabeceras,
      body: cuerpo !== undefined ? JSON.stringify(cuerpo) : undefined
    })
  } catch {
    throw new Error('No hay conexión con el servidor. Revisa tu internet')
  }

  if (respuesta.status === 401 && token) {
    alExpirarSesion()
  }

  if (respuesta.status === 204) return null

  const texto = await respuesta.text()
  let datos = null
  if (texto) {
    try {
      datos = JSON.parse(texto)
    } catch {
      datos = null
    }
  }

  if (!respuesta.ok) {
    throw new Error(datos?.mensaje || 'Algo salió mal. Inténtalo otra vez')
  }

  return datos
}
