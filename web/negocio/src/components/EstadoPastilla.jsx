import { CheckCircle2, CircleCheckBig, Flame, Sparkles, XCircle } from 'lucide-react'
import { ESTADOS } from '../utils/formato'

const ICONOS = {
  nuevo: Sparkles,
  cocinando: Flame,
  listo: CheckCircle2,
  entregado: CircleCheckBig,
  cancelado: XCircle
}

export default function EstadoPastilla({ estado }) {
  const info = ESTADOS[estado] || { texto: estado, grupo: 'entregado' }
  const Icono = ICONOS[info.grupo]
  return (
    <span className={'pastilla ' + info.grupo}>
      <Icono size={13} />
      {info.texto}
    </span>
  )
}
