import { useState } from 'react'
import { Eye, EyeOff, Loader2, Moon, Sun } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useTema } from '../context/ThemeContext'
import Logo from '../components/Logo'

export default function Login() {
  const { iniciarSesion } = useAuth()
  const { modoReal, alternarModo } = useTema()
  const [correo, setCorreo] = useState('')
  const [password, setPassword] = useState('')
  const [ver, setVer] = useState(false)
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(e) {
    e.preventDefault()
    setError('')
    if (!correo.trim() || !password) {
      setError('Escribe tu correo y tu contraseña')
      return
    }
    setEnviando(true)
    try {
      await iniciarSesion(correo.trim(), password)
      if ('Notification' in window && Notification.permission === 'default') {
        Notification.requestPermission().catch(() => {})
      }
    } catch (err) {
      setError(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="login">
      <div className="login-caja">
        <div className="fila entre" style={{ marginBottom: 28 }}>
          <Logo />
          <button className="boton-icono" onClick={alternarModo} aria-label="Cambiar modo claro u oscuro">
            {modoReal === 'oscuro' ? <Sun size={18} /> : <Moon size={18} />}
          </button>
        </div>
        <h1 style={{ margin: '0 0 6px', fontSize: 26 }}>Hola, bienvenida</h1>
        <p className="subtexto" style={{ margin: '0 0 24px' }}>
          Entra para ver los pedidos de tu puesto.
        </p>
        <form className="formulario tarjeta" onSubmit={enviar} style={{ padding: 20 }}>
          {error && <div className="aviso error" style={{ margin: 0 }}>{error}</div>}
          <div className="campo">
            <label htmlFor="correo">Correo</label>
            <input
              id="correo"
              type="email"
              autoComplete="username"
              placeholder="tu-correo@gmail.com"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
            />
          </div>
          <div className="campo">
            <label htmlFor="password">Contraseña</label>
            <div style={{ position: 'relative' }}>
              <input
                id="password"
                type={ver ? 'text' : 'password'}
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={{ paddingRight: 48 }}
              />
              <button
                type="button"
                onClick={() => setVer(!ver)}
                aria-label={ver ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                style={{ position: 'absolute', right: 8, top: 8, border: 'none', background: 'none', color: 'var(--texto-2)', padding: 6 }}
              >
                {ver ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>
          <button className="boton principal grande ancho" disabled={enviando}>
            {enviando ? <Loader2 className="giro" size={20} /> : 'Entrar'}
          </button>
        </form>
        <p className="subtexto" style={{ textAlign: 'center', marginTop: 18 }}>
          ¿Todavía no tienes cuenta? Pídele al administrador de PideCerca que registre tu puesto.
        </p>
      </div>
    </div>
  )
}
