import { useCallback, useEffect, useMemo, useState } from 'react'
import { Loader2 } from 'lucide-react'
import { api } from '../api'
import EstadoPastilla from '../components/EstadoPastilla'
import Modal from '../components/Modal'
import { METODOS_PAGO, fechaCorta, nombreCliente, soles } from '../utils/formato'

const RANGOS = [
  { clave: 'hoy', texto: 'Hoy' },
  { clave: 'semana', texto: '7 días' },
  { clave: 'mes', texto: '30 días' },
  { clave: 'todo', texto: 'Todo' }
]

function desde(rango) {
  const ahora = new Date()
  if (rango === 'hoy') return new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate()).getTime()
  if (rango === 'semana') return ahora.getTime() - 7 * 86400000
  if (rango === 'mes') return ahora.getTime() - 30 * 86400000
  return 0
}

export default function Historial() {
  const [pedidos, setPedidos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [rango, setRango] = useState('hoy')
  const [detalle, setDetalle] = useState(null)

  const cargar = useCallback(async () => {
    try {
      setPedidos(await api('/pedidos/negocio'))
      setError('')
    } catch (e) {
      setError(e.message)
    } finally {
      setCargando(false)
    }
  }, [])

  useEffect(() => {
    cargar()
  }, [cargar])

  const filtrados = useMemo(() => {
    const limite = desde(rango)
    return pedidos.filter((p) => new Date(p.fecha).getTime() >= limite)
  }, [pedidos, rango])

  const entregados = filtrados.filter((p) => p.estado === 'RECOGIDO')
  const ventas = entregados.reduce((suma, p) => suma + (p.total || 0), 0)
  const cancelados = filtrados.filter((p) => p.estado === 'CANCELADO').length

  return (
    <>
      <div className="titulo-pagina">
        <h1>Historial</h1>
      </div>

      <div className="chips">
        {RANGOS.map((r) => (
          <button key={r.clave} className={'chip' + (rango === r.clave ? ' activo' : '')} onClick={() => setRango(r.clave)}>
            {r.texto}
          </button>
        ))}
      </div>

      <div className="resumen-grid">
        <div className="metrica">
          <span>Ventas</span>
          <b style={{ color: 'var(--listo-tinta)' }}>{soles(ventas)}</b>
        </div>
        <div className="metrica">
          <span>Entregados</span>
          <b>{entregados.length}</b>
        </div>
        <div className="metrica">
          <span>Cancelados</span>
          <b>{cancelados}</b>
        </div>
      </div>

      {error && <div className="aviso error">{error}</div>}

      {cargando ? (
        <div className="cargando">
          <Loader2 className="giro" size={26} />
        </div>
      ) : filtrados.length === 0 ? (
        <div className="vacio">No hay pedidos en este periodo.</div>
      ) : (
        <div className="tarjeta" style={{ padding: 4, overflowX: 'auto' }}>
          <table className="tabla">
            <thead>
              <tr>
                <th>#</th>
                <th>Fecha</th>
                <th>Cliente</th>
                <th className="ocultar-movil">Productos</th>
                <th>Total</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {filtrados.map((p) => (
                <tr key={p.id} className="clic" onClick={() => setDetalle(p)}>
                  <td>
                    <b>#{p.id}</b>
                  </td>
                  <td>{fechaCorta(p.fecha)}</td>
                  <td>{nombreCliente(p)}</td>
                  <td className="ocultar-movil subtexto">
                    {p.detalles?.map((d) => `${d.cantidad}x ${d.producto?.nombre}`).join(', ')}
                  </td>
                  <td>{soles(p.total)}</td>
                  <td>
                    <EstadoPastilla estado={p.estado} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {detalle && (
        <Modal titulo={`Pedido #${detalle.id}`} alCerrar={() => setDetalle(null)}>
          <div className="fila entre" style={{ marginBottom: 12 }}>
            <span className="subtexto">{fechaCorta(detalle.fecha)}</span>
            <EstadoPastilla estado={detalle.estado} />
          </div>
          <p style={{ margin: '0 0 8px' }}>
            <b>Cliente:</b> {detalle.usuario?.nombre} {detalle.usuario?.apellido}
            {detalle.usuario?.telefono ? ` · ${detalle.usuario.telefono}` : ''}
          </p>
          <ul className="productos">
            {detalle.detalles?.map((d) => (
              <li key={d.id} className="fila entre">
                <span className="fila">
                  <span className="cantidad">{d.cantidad}</span>
                  {d.producto?.nombre}
                </span>
                <span>{soles(d.subtotal)}</span>
              </li>
            ))}
          </ul>
          {detalle.notas && <div className="nota">{detalle.notas}</div>}
          {detalle.motivoCancelacion && <div className="aviso error">Motivo: {detalle.motivoCancelacion}</div>}
          <div className="pedido-pie">
            <span>
              {METODOS_PAGO[detalle.pago?.metodo] || 'Pago'} · {detalle.pago?.estado === 'REEMBOLSADO' ? 'Reembolsado' : 'Pagado'}
            </span>
            <b>{soles(detalle.total)}</b>
          </div>
        </Modal>
      )}
    </>
  )
}
