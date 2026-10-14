import { useState } from 'react'
import { ArrowRight, Check, Clock, Loader2, Plus, StickyNote } from 'lucide-react'
import { api } from '../api'
import { useAuth } from '../context/AuthContext'
import { usePedidos } from '../context/PedidosContext'
import { ESTADOS, METODOS_PAGO, haceCuanto, iniciales, minutosRestantes, nombreCliente, progreso, soles } from '../utils/formato'
import EstadoPastilla from './EstadoPastilla'
import Modal from './Modal'
import { useToast } from './Toast'

const OPCIONES_TIEMPO = [10, 15, 20, 30]

const MOTIVOS = ['Se acabó un producto', 'Tengo demasiados pedidos', 'Ya estoy cerrando']

export default function PedidoCard({ pedido, ahora }) {
  const { negocio } = useAuth()
  const { reemplazar, recientes } = usePedidos()
  const mostrar = useToast()
  const [ocupado, setOcupado] = useState(false)
  const [minutos, setMinutos] = useState(pedido.tiempoPreparacion || negocio?.tiempoEstimado || 15)
  const [rechazando, setRechazando] = useState(false)
  const [motivo, setMotivo] = useState(MOTIVOS[0])
  const [otroMotivo, setOtroMotivo] = useState('')

  const grupo = ESTADOS[pedido.estado]?.grupo || 'nuevo'
  const cliente = nombreCliente(pedido)

  async function ejecutar(accion, mensaje) {
    setOcupado(true)
    try {
      const actualizado = await accion()
      reemplazar(actualizado)
      if (mensaje) mostrar(mensaje)
    } catch (e) {
      mostrar(e.message)
    } finally {
      setOcupado(false)
    }
  }

  function aceptar() {
    ejecutar(async () => {
      await api(`/pedidos/${pedido.id}/aceptar`, { metodo: 'POST', cuerpo: { minutos } })
      return api(`/pedidos/${pedido.id}/preparar`, { metodo: 'POST' })
    }, `Pedido #${pedido.id} aceptado. ¡A cocinar!`)
  }

  function marcarListo() {
    ejecutar(() => api(`/pedidos/${pedido.id}/listo`, { metodo: 'POST' }), `Avisamos al cliente que el #${pedido.id} está listo`)
  }

  function entregar() {
    ejecutar(() => api(`/pedidos/${pedido.id}/recogido`, { metodo: 'POST' }), `Pedido #${pedido.id} entregado`)
  }

  function sumarTiempo() {
    ejecutar(
      () => api(`/pedidos/${pedido.id}/tiempo`, { metodo: 'PUT', cuerpo: { minutos: (pedido.tiempoPreparacion || 15) + 5 } }),
      'Le avisamos al cliente que necesitas 5 minutos más'
    )
  }

  function confirmarRechazo() {
    const texto = motivo === 'otro' ? otroMotivo.trim() : motivo
    setRechazando(false)
    ejecutar(
      () => api(`/pedidos/${pedido.id}/rechazar`, { metodo: 'POST', cuerpo: { motivo: texto || null } }),
      `Pedido #${pedido.id} rechazado. Se devolverá el dinero al cliente`
    )
  }

  const restantes = minutosRestantes(pedido, ahora)
  const avance = progreso(pedido, ahora)

  return (
    <article className={`pedido ${grupo} ${recientes.includes(pedido.id) ? 'recien' : ''}`}>
      <div className="pedido-cabeza">
        <span className="numero">#{pedido.id}</span>
        <EstadoPastilla estado={pedido.estado} />
      </div>

      <div className="cliente">
        <span className="avatar">{iniciales(cliente)}</span>
        <span>
          {cliente} · {haceCuanto(pedido.fecha, ahora)}
        </span>
      </div>

      <ul className="productos">
        {pedido.detalles?.map((d) => (
          <li key={d.id}>
            <span className="cantidad">{d.cantidad}</span>
            <span>{d.producto?.nombre}</span>
          </li>
        ))}
      </ul>

      {pedido.notas && (
        <div className="nota fila">
          <StickyNote size={16} />
          {pedido.notas}
        </div>
      )}

      <div className="pedido-pie">
        <span>Pagado con {METODOS_PAGO[pedido.pago?.metodo] || 'app'}</span>
        <b>{soles(pedido.total)}</b>
      </div>

      {grupo === 'nuevo' && (
        <>
          <div className="subtexto" style={{ marginBottom: 6 }}>
            ¿En cuánto estará listo?
          </div>
          <div className="tiempos">
            {OPCIONES_TIEMPO.map((m) => (
              <button key={m} className={'tiempo' + (minutos === m ? ' activo' : '')} onClick={() => setMinutos(m)}>
                {m} min
              </button>
            ))}
          </div>
          <button className="boton principal grande ancho" onClick={aceptar} disabled={ocupado}>
            {ocupado ? <Loader2 className="giro" size={20} /> : <Check size={20} />}
            Aceptar · {minutos} min
          </button>
          <button className="boton peligro ancho" style={{ marginTop: 8 }} onClick={() => setRechazando(true)} disabled={ocupado}>
            Rechazar
          </button>
        </>
      )}

      {grupo === 'cocinando' && (
        <>
          <div className="fila entre subtexto">
            <span className="fila">
              <Clock size={15} />
              {restantes === null ? 'Preparando' : restantes > 0 ? `Faltan ${restantes} min` : `Atrasado ${Math.abs(restantes)} min`}
            </span>
            <button className="boton secundario chico" onClick={sumarTiempo} disabled={ocupado}>
              <Plus size={14} />5 min
            </button>
          </div>
          <div className={'barra' + (restantes !== null && restantes < 0 ? ' tarde' : '')}>
            <div style={{ width: `${Math.round(avance * 100)}%` }} />
          </div>
          <button className="boton listo grande ancho" onClick={marcarListo} disabled={ocupado}>
            {ocupado ? <Loader2 className="giro" size={20} /> : <Check size={20} />}
            Ya está listo
          </button>
        </>
      )}

      {grupo === 'listo' && (
        <>
          <div className="aviso info" style={{ marginBottom: 10 }}>
            Cuando llegue el cliente, pregúntale por el número #{pedido.id}.
          </div>
          <button className="boton principal grande ancho" onClick={entregar} disabled={ocupado}>
            {ocupado ? <Loader2 className="giro" size={20} /> : <ArrowRight size={20} />}
            Entregado al cliente
          </button>
        </>
      )}

      {rechazando && (
        <Modal titulo={`¿Rechazar el pedido #${pedido.id}?`} alCerrar={() => setRechazando(false)}>
          <p className="subtexto" style={{ marginTop: 0 }}>
            Le avisaremos al cliente y se le devolverá su dinero. Elige el motivo:
          </p>
          <div className="formulario">
            {[...MOTIVOS, 'otro'].map((m) => (
              <label key={m} className="fila tarjeta" style={{ padding: 12, cursor: 'pointer' }}>
                <input type="radio" name="motivo" checked={motivo === m} onChange={() => setMotivo(m)} />
                {m === 'otro' ? 'Otro motivo' : m}
              </label>
            ))}
            {motivo === 'otro' && (
              <div className="campo">
                <input placeholder="Escribe el motivo" value={otroMotivo} onChange={(e) => setOtroMotivo(e.target.value)} />
              </div>
            )}
            <div className="fila">
              <button className="boton secundario ancho" onClick={() => setRechazando(false)}>
                Volver
              </button>
              <button className="boton peligro ancho" onClick={confirmarRechazo}>
                Sí, rechazar
              </button>
            </div>
          </div>
        </Modal>
      )}
    </article>
  )
}
