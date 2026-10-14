import { useState } from 'react'
import { Loader2, LogOut, MapPin, Monitor, Moon, Sun } from 'lucide-react'
import { api } from '../api'
import { useAuth } from '../context/AuthContext'
import { COLORES, useTema } from '../context/ThemeContext'
import { useToast } from '../components/Toast'

function DatosNegocio() {
  const { negocio, setNegocio } = useAuth()
  const mostrar = useToast()
  const [datos, setDatos] = useState(() => ({
    nombre: negocio?.nombre || '',
    tipoComida: negocio?.tipoComida || '',
    descripcion: negocio?.descripcion || '',
    direccion: negocio?.direccion || '',
    telefono: negocio?.telefono || '',
    horario: negocio?.horario || '',
    imagen: negocio?.imagen || '',
    tiempoEstimado: negocio?.tiempoEstimado || 15,
    latitud: negocio?.latitud ?? '',
    longitud: negocio?.longitud ?? ''
  }))
  const [guardando, setGuardando] = useState(false)
  const [ubicando, setUbicando] = useState(false)
  const [error, setError] = useState('')

  function cambiar(campo, valor) {
    setDatos((d) => ({ ...d, [campo]: valor }))
  }

  function usarUbicacion() {
    if (!navigator.geolocation) {
      setError('Tu navegador no permite obtener la ubicación')
      return
    }
    setUbicando(true)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        cambiar('latitud', Number(pos.coords.latitude.toFixed(6)))
        cambiar('longitud', Number(pos.coords.longitude.toFixed(6)))
        setUbicando(false)
        mostrar('Ubicación lista. Toca “Guardar cambios”')
      },
      () => {
        setUbicando(false)
        setError('No pudimos obtener tu ubicación. Revisa los permisos del navegador')
      },
      { enableHighAccuracy: true, timeout: 15000 }
    )
  }

  async function guardar(e) {
    e.preventDefault()
    setError('')
    if (!datos.nombre.trim()) return setError('El nombre del puesto es obligatorio')
    setGuardando(true)
    try {
      const actualizado = await api('/negocios/mio', {
        metodo: 'PUT',
        cuerpo: {
          ...datos,
          tiempoEstimado: Number(datos.tiempoEstimado) || null,
          latitud: datos.latitud === '' ? null : Number(datos.latitud),
          longitud: datos.longitud === '' ? null : Number(datos.longitud)
        }
      })
      setNegocio(actualizado)
      mostrar('Datos de tu puesto guardados')
    } catch (err) {
      setError(err.message)
    } finally {
      setGuardando(false)
    }
  }

  return (
    <form className="formulario tarjeta" onSubmit={guardar}>
      {error && <div className="aviso error" style={{ margin: 0 }}>{error}</div>}
      <div className="rejilla-2">
        <div className="campo">
          <label>Nombre del puesto</label>
          <input value={datos.nombre} onChange={(e) => cambiar('nombre', e.target.value)} />
        </div>
        <div className="campo">
          <label>Tipo de comida</label>
          <input value={datos.tipoComida} onChange={(e) => cambiar('tipoComida', e.target.value)} placeholder="Hamburguesas" />
        </div>
      </div>
      <div className="campo">
        <label>Descripción</label>
        <textarea value={datos.descripcion} onChange={(e) => cambiar('descripcion', e.target.value)} />
      </div>
      <div className="rejilla-2">
        <div className="campo">
          <label>Dirección o referencia</label>
          <input value={datos.direccion} onChange={(e) => cambiar('direccion', e.target.value)} placeholder="Av. Grau, frente al parque" />
        </div>
        <div className="campo">
          <label>Teléfono</label>
          <input inputMode="tel" value={datos.telefono} onChange={(e) => cambiar('telefono', e.target.value)} />
        </div>
      </div>
      <div className="rejilla-2">
        <div className="campo">
          <label>Horario</label>
          <input value={datos.horario} onChange={(e) => cambiar('horario', e.target.value)} placeholder="Lun a Dom · 6 pm - 12 am" />
        </div>
        <div className="campo">
          <label>Tiempo normal de preparación (min)</label>
          <input inputMode="numeric" value={datos.tiempoEstimado} onChange={(e) => cambiar('tiempoEstimado', e.target.value)} />
        </div>
      </div>
      <div className="campo">
        <label>Foto del puesto (enlace, opcional)</label>
        <input value={datos.imagen} onChange={(e) => cambiar('imagen', e.target.value)} placeholder="https://..." />
      </div>
      <div className="tarjeta" style={{ background: 'var(--tarjeta-2)' }}>
        <div className="fila entre" style={{ flexWrap: 'wrap', gap: 10 }}>
          <div>
            <b>Ubicación del puesto</b>
            <div className="subtexto">
              {datos.latitud !== '' && datos.longitud !== ''
                ? `${datos.latitud}, ${datos.longitud}`
                : 'Sin ubicación: los clientes no te verán como “cercano”'}
            </div>
          </div>
          <button type="button" className="boton secundario" onClick={usarUbicacion} disabled={ubicando}>
            {ubicando ? <Loader2 className="giro" size={18} /> : <MapPin size={18} />}
            Usar mi ubicación actual
          </button>
        </div>
      </div>
      <button className="boton principal grande" disabled={guardando}>
        {guardando ? <Loader2 className="giro" size={20} /> : 'Guardar cambios'}
      </button>
    </form>
  )
}

function Apariencia() {
  const { color, modo, cambiarColor, cambiarModo } = useTema()
  const modos = [
    { clave: 'oscuro', texto: 'Oscuro', icono: Moon },
    { clave: 'claro', texto: 'Claro', icono: Sun },
    { clave: 'auto', texto: 'Según la hora', icono: Monitor }
  ]
  return (
    <div className="tarjeta formulario">
      <div>
        <div className="subtexto" style={{ marginBottom: 8 }}>
          Color principal
        </div>
        <div className="colores">
          {Object.entries(COLORES).map(([clave, c]) => (
            <button key={clave} className={'color' + (color === clave ? ' activo' : '')} onClick={() => cambiarColor(clave)}>
              <i style={{ background: c.oscuro }} />
              {c.nombre}
            </button>
          ))}
        </div>
      </div>
      <div>
        <div className="subtexto" style={{ marginBottom: 8 }}>
          Modo de pantalla
        </div>
        <div className="colores">
          {modos.map((m) => (
            <button key={m.clave} className={'color' + (modo === m.clave ? ' activo' : '')} onClick={() => cambiarModo(m.clave)}>
              <m.icono size={18} />
              {m.texto}
            </button>
          ))}
        </div>
        <div className="subtexto" style={{ marginTop: 8 }}>
          De día al sol se ve mejor el modo claro. De noche, el oscuro.
        </div>
      </div>
    </div>
  )
}

function MiCuenta() {
  const { usuario, setUsuario, cerrarSesion } = useAuth()
  const mostrar = useToast()
  const [perfil, setPerfil] = useState({
    nombre: usuario?.nombre || '',
    apellido: usuario?.apellido || '',
    telefono: usuario?.telefono || ''
  })
  const [claves, setClaves] = useState({ passwordActual: '', nuevaPassword: '' })
  const [error, setError] = useState('')

  async function guardarPerfil(e) {
    e.preventDefault()
    setError('')
    try {
      setUsuario(await api('/usuarios/me', { metodo: 'PUT', cuerpo: perfil }))
      mostrar('Tus datos se guardaron')
    } catch (err) {
      setError(err.message)
    }
  }

  async function cambiarClave(e) {
    e.preventDefault()
    setError('')
    try {
      await api('/usuarios/me/password', { metodo: 'PUT', cuerpo: claves })
      setClaves({ passwordActual: '', nuevaPassword: '' })
      mostrar('Contraseña actualizada')
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="formulario">
      {error && <div className="aviso error" style={{ margin: 0 }}>{error}</div>}
      <form className="tarjeta formulario" onSubmit={guardarPerfil}>
        <div className="subtexto">{usuario?.correo}</div>
        <div className="rejilla-2">
          <div className="campo">
            <label>Nombre</label>
            <input value={perfil.nombre} onChange={(e) => setPerfil({ ...perfil, nombre: e.target.value })} />
          </div>
          <div className="campo">
            <label>Apellido</label>
            <input value={perfil.apellido} onChange={(e) => setPerfil({ ...perfil, apellido: e.target.value })} />
          </div>
        </div>
        <div className="campo">
          <label>Teléfono</label>
          <input inputMode="tel" value={perfil.telefono} onChange={(e) => setPerfil({ ...perfil, telefono: e.target.value })} />
        </div>
        <button className="boton secundario">Guardar mis datos</button>
      </form>
      <form className="tarjeta formulario" onSubmit={cambiarClave}>
        <div className="rejilla-2">
          <div className="campo">
            <label>Contraseña actual</label>
            <input type="password" autoComplete="current-password" value={claves.passwordActual} onChange={(e) => setClaves({ ...claves, passwordActual: e.target.value })} />
          </div>
          <div className="campo">
            <label>Nueva contraseña</label>
            <input type="password" autoComplete="new-password" value={claves.nuevaPassword} onChange={(e) => setClaves({ ...claves, nuevaPassword: e.target.value })} />
          </div>
        </div>
        <button className="boton secundario">Cambiar contraseña</button>
      </form>
      <button className="boton peligro grande" onClick={cerrarSesion}>
        <LogOut size={18} />
        Cerrar sesión
      </button>
    </div>
  )
}

export default function Perfil() {
  const { negocio } = useAuth()
  return (
    <>
      <div className="titulo-pagina">
        <h1>Mi puesto</h1>
      </div>
      {negocio && (
        <section className="seccion">
          <h2>Datos del puesto</h2>
          <DatosNegocio />
        </section>
      )}
      <section className="seccion">
        <h2>Apariencia</h2>
        <Apariencia />
      </section>
      <section className="seccion">
        <h2>Mi cuenta</h2>
        <MiCuenta />
      </section>
    </>
  )
}
