export const ESTADOS = {
  PENDIENTE: { texto: 'Nuevo', grupo: 'nuevo' },
  ACEPTADO: { texto: 'Cocinando', grupo: 'cocinando' },
  EN_PREPARACION: { texto: 'Cocinando', grupo: 'cocinando' },
  LISTO_PARA_RECOGER: { texto: 'Listo', grupo: 'listo' },
  RECOGIDO: { texto: 'Entregado', grupo: 'entregado' },
  CANCELADO: { texto: 'Cancelado', grupo: 'cancelado' }
}

export const METODOS_PAGO = {
  YAPE: 'Yape',
  PLIN: 'Plin',
  TARJETA: 'Tarjeta',
  EFECTIVO: 'Efectivo'
}

export function soles(valor) {
  return 'S/ ' + Number(valor || 0).toFixed(2)
}

export function fecha(valor) {
  if (!valor) return null
  return new Date(valor)
}

export function haceCuanto(valor, ahora = Date.now()) {
  const inicio = fecha(valor)
  if (!inicio) return ''
  const minutos = Math.max(0, Math.floor((ahora - inicio.getTime()) / 60000))
  if (minutos < 1) return 'ahora'
  if (minutos < 60) return `hace ${minutos} min`
  const horas = Math.floor(minutos / 60)
  if (horas < 24) return `hace ${horas} h`
  const dias = Math.floor(horas / 24)
  return dias === 1 ? 'ayer' : `hace ${dias} días`
}

export function minutosRestantes(pedido, ahora = Date.now()) {
  const inicio = fecha(pedido.fechaAceptado || pedido.fecha)
  if (!inicio || !pedido.tiempoPreparacion) return null
  const fin = inicio.getTime() + pedido.tiempoPreparacion * 60000
  return Math.ceil((fin - ahora) / 60000)
}

export function progreso(pedido, ahora = Date.now()) {
  const inicio = fecha(pedido.fechaAceptado || pedido.fecha)
  if (!inicio || !pedido.tiempoPreparacion) return 0
  const total = pedido.tiempoPreparacion * 60000
  return Math.min(1, Math.max(0, (ahora - inicio.getTime()) / total))
}

export function fechaCorta(valor) {
  const f = fecha(valor)
  if (!f) return ''
  return f.toLocaleDateString('es-PE', { day: 'numeric', month: 'short' }) +
    ' · ' + f.toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' })
}

export function nombreCliente(pedido) {
  const u = pedido.usuario
  if (!u) return 'Cliente'
  return [u.nombre, u.apellido ? u.apellido.charAt(0) + '.' : ''].filter(Boolean).join(' ')
}

export function iniciales(nombre = '') {
  return nombre
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p.charAt(0).toUpperCase())
    .join('')
}
