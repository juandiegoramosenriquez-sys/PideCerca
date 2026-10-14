import { useEffect } from 'react'
import { X } from 'lucide-react'

export default function Modal({ titulo, alCerrar, children }) {
  useEffect(() => {
    function tecla(e) {
      if (e.key === 'Escape') alCerrar()
    }
    window.addEventListener('keydown', tecla)
    return () => window.removeEventListener('keydown', tecla)
  }, [alCerrar])

  return (
    <div className="fondo-modal" onMouseDown={(e) => e.target === e.currentTarget && alCerrar()}>
      <div className="modal" role="dialog" aria-modal="true">
        <div className="fila entre" style={{ marginBottom: 16 }}>
          <h2>{titulo}</h2>
          <button className="boton-icono" onClick={alCerrar} aria-label="Cerrar">
            <X size={18} />
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
