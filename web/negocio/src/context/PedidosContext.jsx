import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react'
import { api } from '../api'
import { sonarNuevoPedido } from '../utils/aviso'
import { useAuth } from './AuthContext'

const PedidosContext = createContext(null)

const INTERVALO = 8000

export function PedidosProvider({ children }) {
  const { negocio } = useAuth()
  const [pedidos, setPedidos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [recientes, setRecientes] = useState([])
  const [alerta, setAlerta] = useState(false)
  const conocidos = useRef(null)

  const recargar = useCallback(async () => {
    if (!negocio) return
    try {
      const datos = await api('/pedidos/negocio', { consulta: { activos: true } })
      const nuevos = datos.filter((p) => p.estado === 'PENDIENTE').map((p) => p.id)

      if (conocidos.current) {
        const llegaron = nuevos.filter((id) => !conocidos.current.has(id))
        if (llegaron.length > 0) {
          sonarNuevoPedido()
          setAlerta(true)
          setRecientes((anteriores) => [...anteriores, ...llegaron])
          if (document.hidden && 'Notification' in window && Notification.permission === 'granted') {
            new Notification('PideCerca · ¡Nuevo pedido!', { body: `Tienes ${llegaron.length} pedido(s) nuevo(s)` })
          }
        }
      }

      conocidos.current = new Set(datos.map((p) => p.id))
      setPedidos(datos)
      setError('')
    } catch (e) {
      setError(e.message)
    } finally {
      setCargando(false)
    }
  }, [negocio])

  useEffect(() => {
    recargar()
    const intervalo = setInterval(recargar, INTERVALO)
    return () => clearInterval(intervalo)
  }, [recargar])

  const reemplazar = useCallback((actualizado) => {
    setPedidos((lista) => {
      const activo = !['RECOGIDO', 'CANCELADO'].includes(actualizado.estado)
      if (!activo) return lista.filter((p) => p.id !== actualizado.id)
      return lista.map((p) => (p.id === actualizado.id ? actualizado : p))
    })
    setRecientes((lista) => lista.filter((id) => id !== actualizado.id))
  }, [])

  const nuevosCount = pedidos.filter((p) => p.estado === 'PENDIENTE').length

  return (
    <PedidosContext.Provider
      value={{ pedidos, cargando, error, recargar, reemplazar, recientes, alerta, setAlerta, nuevosCount }}
    >
      {children}
    </PedidosContext.Provider>
  )
}

export function usePedidos() {
  return useContext(PedidosContext)
}
