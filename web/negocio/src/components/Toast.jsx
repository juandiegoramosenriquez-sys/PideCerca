import { createContext, useCallback, useContext, useRef, useState } from 'react'

const ToastContext = createContext(() => {})

export function ToastProvider({ children }) {
  const [mensaje, setMensaje] = useState('')
  const temporizador = useRef(null)

  const mostrar = useCallback((texto) => {
    setMensaje(texto)
    clearTimeout(temporizador.current)
    temporizador.current = setTimeout(() => setMensaje(''), 3200)
  }, [])

  return (
    <ToastContext.Provider value={mostrar}>
      {children}
      {mensaje && (
        <div className="toast" role="status">
          {mensaje}
        </div>
      )}
    </ToastContext.Provider>
  )
}

export function useToast() {
  return useContext(ToastContext)
}
