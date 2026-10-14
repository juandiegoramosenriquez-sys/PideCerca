import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { BellRing, ClipboardList, History, Moon, Store, Sun, UtensilsCrossed } from 'lucide-react'
import { api } from '../api'
import { useAuth } from '../context/AuthContext'
import { PedidosProvider, usePedidos } from '../context/PedidosContext'
import { useTema } from '../context/ThemeContext'
import { desbloquearSonido } from '../utils/aviso'
import Logo from './Logo'
import { ToastProvider, useToast } from './Toast'

function Pestanas() {
  const { nuevosCount } = usePedidos()
  return (
    <nav className="pestanas">
      <NavLink to="/" end className={({ isActive }) => 'pestana' + (isActive ? ' activa' : '')}>
        <ClipboardList size={18} />
        Pedidos
        {nuevosCount > 0 && <span className="contador">{nuevosCount}</span>}
      </NavLink>
      <NavLink to="/menu" className={({ isActive }) => 'pestana' + (isActive ? ' activa' : '')}>
        <UtensilsCrossed size={18} />
        Menú
      </NavLink>
      <NavLink to="/historial" className={({ isActive }) => 'pestana' + (isActive ? ' activa' : '')}>
        <History size={18} />
        Historial
      </NavLink>
      <NavLink to="/perfil" className={({ isActive }) => 'pestana' + (isActive ? ' activa' : '')}>
        <Store size={18} />
        Mi puesto
      </NavLink>
    </nav>
  )
}

function BotonAbierto() {
  const { negocio, setNegocio } = useAuth()
  const mostrar = useToast()
  const [guardando, setGuardando] = useState(false)

  if (!negocio) return null
  const abierto = Boolean(negocio.abierto)

  async function alternar() {
    setGuardando(true)
    try {
      const actualizado = await api('/negocios/mio/abierto', { metodo: 'PUT', consulta: { valor: !abierto } })
      setNegocio(actualizado)
      mostrar(actualizado.abierto ? 'Tu puesto está abierto. Ya puedes recibir pedidos' : 'Tu puesto está cerrado')
    } catch (e) {
      mostrar(e.message)
    } finally {
      setGuardando(false)
    }
  }

  return (
    <button className={'boton-abierto ' + (abierto ? 'si' : 'no')} onClick={alternar} disabled={guardando}>
      <span className="punto" />
      {abierto ? 'Abierto' : 'Cerrado'}
    </button>
  )
}

function AlertaNuevo() {
  const { alerta, setAlerta } = usePedidos()
  const navegar = useNavigate()
  if (!alerta) return null
  return (
    <button
      className="alerta-nuevo"
      onClick={() => {
        setAlerta(false)
        navegar('/')
      }}
    >
      <BellRing size={22} />
      ¡Nuevo pedido!
    </button>
  )
}

function Estructura() {
  const { negocio, errorNegocio } = useAuth()
  const { modoReal, alternarModo } = useTema()

  return (
    <div className="app" onClick={desbloquearSonido}>
      <header className="cabecera">
        <div className="cabecera-fila">
          <div style={{ minWidth: 0 }}>
            <Logo />
            <div className="negocio-nombre solo-movil">{negocio?.nombre || 'Mi puesto'}</div>
          </div>
          <div className="solo-escritorio">
            <Pestanas />
          </div>
          <div className="espacio" />
          <span className="negocio-nombre solo-escritorio">{negocio?.nombre}</span>
          <button className="boton-icono" onClick={alternarModo} aria-label="Cambiar modo claro u oscuro">
            {modoReal === 'oscuro' ? <Sun size={18} /> : <Moon size={18} />}
          </button>
          <BotonAbierto />
        </div>
        <div className="solo-movil">
          <Pestanas />
        </div>
      </header>
      <AlertaNuevo />
      <main className="contenido">
        {errorNegocio && <div className="aviso error">{errorNegocio}</div>}
        {negocio && !negocio.activo && (
          <div className="aviso alerta">
            Tu negocio está deshabilitado por el administrador. Los clientes no pueden verlo hasta que lo habiliten.
          </div>
        )}
        <Outlet />
      </main>
    </div>
  )
}

export default function Layout() {
  return (
    <ToastProvider>
      <PedidosProvider>
        <Estructura />
      </PedidosProvider>
    </ToastProvider>
  )
}
