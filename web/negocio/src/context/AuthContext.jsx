import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api, configurarExpiracion, guardarToken, obtenerToken } from '../api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [negocio, setNegocio] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [errorNegocio, setErrorNegocio] = useState('')

  const cerrarSesion = useCallback(() => {
    guardarToken(null)
    setUsuario(null)
    setNegocio(null)
    setErrorNegocio('')
  }, [])

  const cargarNegocio = useCallback(async () => {
    try {
      const datos = await api('/negocios/mio')
      setNegocio(datos)
      setErrorNegocio('')
      return datos
    } catch (error) {
      setNegocio(null)
      setErrorNegocio(error.message)
      return null
    }
  }, [])

  useEffect(() => {
    configurarExpiracion(cerrarSesion)
  }, [cerrarSesion])

  useEffect(() => {
    async function iniciar() {
      if (!obtenerToken()) {
        setCargando(false)
        return
      }
      try {
        const yo = await api('/auth/me')
        setUsuario(yo)
        if (yo.rol === 'NEGOCIO') await cargarNegocio()
      } catch {
        cerrarSesion()
      } finally {
        setCargando(false)
      }
    }
    iniciar()
  }, [cargarNegocio, cerrarSesion])

  async function iniciarSesion(correo, password) {
    const respuesta = await api('/auth/login', { metodo: 'POST', cuerpo: { correo, password } })
    if (respuesta.usuario.rol !== 'NEGOCIO') {
      throw new Error(
        respuesta.usuario.rol === 'ADMIN'
          ? 'Esta web es para negocios. El administrador entra por el panel de administración'
          : 'Esta web es para negocios. Los clientes piden desde la app PideCerca'
      )
    }
    guardarToken(respuesta.token)
    setUsuario(respuesta.usuario)
    await cargarNegocio()
  }

  return (
    <AuthContext.Provider
      value={{
        usuario,
        setUsuario,
        negocio,
        setNegocio,
        cargarNegocio,
        errorNegocio,
        cargando,
        iniciarSesion,
        cerrarSesion
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
