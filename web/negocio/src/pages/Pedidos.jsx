import { useEffect, useState } from 'react'
import { Loader2, RefreshCw } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { usePedidos } from '../context/PedidosContext'
import PedidoCard from '../components/PedidoCard'
import { ESTADOS } from '../utils/formato'

const GRUPOS = [
  { clave: 'nuevo', titulo: 'Nuevos', vacio: 'Aquí aparecerán los pedidos nuevos' },
  { clave: 'cocinando', titulo: 'Cocinando', vacio: 'Nada en la cocina por ahora' },
  { clave: 'listo', titulo: 'Listos para recoger', vacio: 'Ningún pedido esperando' }
]

function useAhora() {
  const [ahora, setAhora] = useState(Date.now())
  useEffect(() => {
    const intervalo = setInterval(() => setAhora(Date.now()), 15000)
    return () => clearInterval(intervalo)
  }, [])
  return ahora
}

function useEsMovil() {
  const [movil, setMovil] = useState(() => window.innerWidth <= 900)
  useEffect(() => {
    const cambio = () => setMovil(window.innerWidth <= 900)
    window.addEventListener('resize', cambio)
    return () => window.removeEventListener('resize', cambio)
  }, [])
  return movil
}

export default function Pedidos() {
  const { usuario, negocio } = useAuth()
  const { pedidos, cargando, error, recargar, setAlerta } = usePedidos()
  const [filtro, setFiltro] = useState('todos')
  const ahora = useAhora()
  const movil = useEsMovil()

  useEffect(() => {
    setAlerta(false)
  }, [setAlerta])

  if (!negocio) return null

  const porGrupo = (clave) =>
    pedidos
      .filter((p) => ESTADOS[p.estado]?.grupo === clave)
      .sort((a, b) => (clave === 'nuevo' ? b.id - a.id : a.id - b.id))

  const cuentas = Object.fromEntries(GRUPOS.map((g) => [g.clave, porGrupo(g.clave).length]))

  return (
    <>
      <div className="titulo-pagina">
        <div>
          <div className="subtexto">Hola, {usuario?.nombre}</div>
          <h1>Pedidos de hoy</h1>
        </div>
        <button className="boton secundario chico" onClick={recargar}>
          <RefreshCw size={15} />
          Actualizar
        </button>
      </div>

      {!negocio.abierto && (
        <div className="aviso alerta">
          Tu puesto está <b>cerrado</b>. Toca el botón “Cerrado” de arriba para empezar a recibir pedidos.
        </div>
      )}

      {error && <div className="aviso error">{error}</div>}

      <div className="contadores">
        {GRUPOS.map((g) => (
          <button
            key={g.clave}
            className={`contador-estado ${g.clave} ${movil && filtro === g.clave ? 'seleccionado' : ''}`}
            onClick={() => setFiltro(filtro === g.clave ? 'todos' : g.clave)}
          >
            <b>{cuentas[g.clave]}</b>
            {g.titulo.split(' ')[0]}
          </button>
        ))}
      </div>

      {cargando ? (
        <div className="cargando">
          <Loader2 className="giro" size={26} />
        </div>
      ) : movil ? (
        <div className="lista">
          {GRUPOS.filter((g) => filtro === 'todos' || filtro === g.clave)
            .flatMap((g) => porGrupo(g.clave))
            .map((p) => (
              <PedidoCard key={p.id} pedido={p} ahora={ahora} />
            ))}
          {pedidos.length === 0 && <div className="vacio">No tienes pedidos pendientes. Te avisaremos con un sonido cuando llegue uno.</div>}
          {pedidos.length > 0 && filtro !== 'todos' && cuentas[filtro] === 0 && (
            <div className="vacio">{GRUPOS.find((g) => g.clave === filtro)?.vacio}</div>
          )}
        </div>
      ) : (
        <div className="columnas">
          {GRUPOS.map((g) => (
            <section key={g.clave}>
              <div className={`columna-titulo ${g.clave}`}>
                <span>{g.titulo}</span>
                <span className={`pastilla ${g.clave}`}>{cuentas[g.clave]}</span>
              </div>
              <div className="lista">
                {porGrupo(g.clave).map((p) => (
                  <PedidoCard key={p.id} pedido={p} ahora={ahora} />
                ))}
                {cuentas[g.clave] === 0 && <div className="vacio">{g.vacio}</div>}
              </div>
            </section>
          ))}
        </div>
      )}
    </>
  )
}
