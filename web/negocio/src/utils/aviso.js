let contexto = null

export function sonarNuevoPedido() {
  try {
    const Audio = window.AudioContext || window.webkitAudioContext
    if (!Audio) return
    contexto = contexto || new Audio()
    const ahora = contexto.currentTime
    ;[0, 0.18, 0.36].forEach((retraso, i) => {
      const oscilador = contexto.createOscillator()
      const volumen = contexto.createGain()
      oscilador.type = 'sine'
      oscilador.frequency.value = i === 2 ? 1175 : 880
      volumen.gain.setValueAtTime(0.0001, ahora + retraso)
      volumen.gain.exponentialRampToValueAtTime(0.35, ahora + retraso + 0.02)
      volumen.gain.exponentialRampToValueAtTime(0.0001, ahora + retraso + 0.16)
      oscilador.connect(volumen).connect(contexto.destination)
      oscilador.start(ahora + retraso)
      oscilador.stop(ahora + retraso + 0.18)
    })
  } catch {
    return
  }
  try {
    navigator.vibrate?.([200, 100, 200])
  } catch {
    return
  }
}

export function desbloquearSonido() {
  try {
    const Audio = window.AudioContext || window.webkitAudioContext
    if (!Audio) return
    contexto = contexto || new Audio()
    if (contexto.state === 'suspended') contexto.resume()
  } catch {
    return
  }
}
