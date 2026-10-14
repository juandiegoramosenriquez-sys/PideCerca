import { useCallback, useEffect, useMemo, useState } from 'react'
import { ImageIcon, Loader2, Pencil, Plus, Tag, Trash2 } from 'lucide-react'
import { api } from '../api'
import Modal from '../components/Modal'
import { useToast } from '../components/Toast'
import { soles } from '../utils/formato'

const PRODUCTO_VACIO = { nombre: '', precio: '', descripcion: '', imagen: '', categoriaId: '', disponible: true }

function FormularioProducto({ inicial, categorias, alGuardar, alCerrar }) {
  const [datos, setDatos] = useState(inicial)
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  function cambiar(campo, valor) {
    setDatos((d) => ({ ...d, [campo]: valor }))
  }

  async function enviar(e) {
    e.preventDefault()
    setError('')
    const precio = Number(String(datos.precio).replace(',', '.'))
    if (!datos.nombre.trim()) return setError('Escribe el nombre del producto')
    if (!precio || precio <= 0) return setError('Escribe un precio mayor que 0')
    setGuardando(true)
    try {
      await alGuardar({
        nombre: datos.nombre.trim(),
        precio,
        descripcion: datos.descripcion,
        imagen: datos.imagen,
        disponible: datos.disponible,
        categoria: datos.categoriaId ? { id: Number(datos.categoriaId) } : null
      })
    } catch (err) {
      setError(err.message)
      setGuardando(false)
    }
  }

  return (
    <Modal titulo={inicial.id ? 'Editar producto' : 'Nuevo producto'} alCerrar={alCerrar}>
      <form className="formulario" onSubmit={enviar}>
        {error && <div className="aviso error" style={{ margin: 0 }}>{error}</div>}
        <div className="campo">
          <label>Nombre</label>
          <input value={datos.nombre} onChange={(e) => cambiar('nombre', e.target.value)} placeholder="Hamburguesa royal" autoFocus />
        </div>
        <div className="rejilla-2">
          <div className="campo">
            <label>Precio (S/)</label>
            <input inputMode="decimal" value={datos.precio} onChange={(e) => cambiar('precio', e.target.value)} placeholder="12.00" />
          </div>
          <div className="campo">
            <label>Categoría</label>
            <select value={datos.categoriaId} onChange={(e) => cambiar('categoriaId', e.target.value)}>
              <option value="">Sin categoría</option>
              {categorias.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.nombre}
                </option>
              ))}
            </select>
          </div>
        </div>
        <div className="campo">
          <label>Descripción</label>
          <textarea value={datos.descripcion || ''} onChange={(e) => cambiar('descripcion', e.target.value)} placeholder="Carne, huevo, queso y plátano" />
        </div>
        <div className="campo">
          <label>Foto (enlace de imagen, opcional)</label>
          <input value={datos.imagen || ''} onChange={(e) => cambiar('imagen', e.target.value)} placeholder="https://..." />
        </div>
        <label className="fila entre tarjeta" style={{ padding: 12 }}>
          <span>
            <b>Disponible</b>
            <div className="subtexto">Si lo apagas, los clientes no podrán pedirlo</div>
          </span>
          <button
            type="button"
            className={'interruptor' + (datos.disponible ? ' encendido' : '')}
            onClick={() => cambiar('disponible', !datos.disponible)}
            aria-label="Disponible"
          />
        </label>
        <button className="boton principal grande ancho" disabled={guardando}>
          {guardando ? <Loader2 className="giro" size={20} /> : 'Guardar'}
        </button>
      </form>
    </Modal>
  )
}

function FormularioCategoria({ inicial, alGuardar, alCerrar }) {
  const [nombre, setNombre] = useState(inicial.nombre || '')
  const [descripcion, setDescripcion] = useState(inicial.descripcion || '')
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  async function enviar(e) {
    e.preventDefault()
    if (!nombre.trim()) return setError('Escribe el nombre de la categoría')
    setGuardando(true)
    try {
      await alGuardar({ nombre: nombre.trim(), descripcion })
    } catch (err) {
      setError(err.message)
      setGuardando(false)
    }
  }

  return (
    <Modal titulo={inicial.id ? 'Editar categoría' : 'Nueva categoría'} alCerrar={alCerrar}>
      <form className="formulario" onSubmit={enviar}>
        {error && <div className="aviso error" style={{ margin: 0 }}>{error}</div>}
        <div className="campo">
          <label>Nombre</label>
          <input value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Especiales de la casa" autoFocus />
        </div>
        <div className="campo">
          <label>Descripción (opcional)</label>
          <input value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
        </div>
        <button className="boton principal grande ancho" disabled={guardando}>
          {guardando ? <Loader2 className="giro" size={20} /> : 'Guardar'}
        </button>
      </form>
    </Modal>
  )
}

export default function Menu() {
  const mostrar = useToast()
  const [productos, setProductos] = useState([])
  const [categorias, setCategorias] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [filtro, setFiltro] = useState('todas')
  const [editando, setEditando] = useState(null)
  const [editandoCategoria, setEditandoCategoria] = useState(null)
  const [borrando, setBorrando] = useState(null)

  const cargar = useCallback(async () => {
    try {
      const [p, c] = await Promise.all([api('/productos/mios'), api('/categorias/mias')])
      setProductos(p)
      setCategorias(c)
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

  const visibles = useMemo(() => {
    if (filtro === 'todas') return productos
    if (filtro === 'sin') return productos.filter((p) => !p.categoria)
    return productos.filter((p) => String(p.categoria?.id) === filtro)
  }, [productos, filtro])

  const categoriasUsadas = categorias.filter((c) => productos.some((p) => p.categoria?.id === c.id))
  const propias = categorias.filter((c) => c.negocio)

  async function alternarDisponible(producto) {
    try {
      const actualizado = await api(`/productos/${producto.id}/disponible`, {
        metodo: 'PUT',
        consulta: { valor: !producto.disponible }
      })
      setProductos((lista) => lista.map((p) => (p.id === actualizado.id ? actualizado : p)))
      mostrar(actualizado.disponible ? `${actualizado.nombre} ya se puede pedir` : `${actualizado.nombre} marcado como agotado`)
    } catch (e) {
      mostrar(e.message)
    }
  }

  async function guardarProducto(datos) {
    if (editando.id) {
      await api(`/productos/${editando.id}`, { metodo: 'PUT', cuerpo: datos })
      mostrar('Producto actualizado')
    } else {
      await api('/productos', { metodo: 'POST', cuerpo: datos })
      mostrar('Producto agregado a tu menú')
    }
    setEditando(null)
    cargar()
  }

  async function guardarCategoria(datos) {
    if (editandoCategoria.id) {
      await api(`/categorias/${editandoCategoria.id}`, { metodo: 'PUT', cuerpo: datos })
    } else {
      await api('/categorias', { metodo: 'POST', cuerpo: datos })
    }
    mostrar('Categoría guardada')
    setEditandoCategoria(null)
    cargar()
  }

  async function confirmarBorrado() {
    const objetivo = borrando
    setBorrando(null)
    try {
      if (objetivo.tipo === 'producto') {
        const r = await api(`/productos/${objetivo.item.id}`, { metodo: 'DELETE' })
        mostrar(r?.mensaje || 'Producto eliminado')
      } else {
        await api(`/categorias/${objetivo.item.id}`, { metodo: 'DELETE' })
        mostrar('Categoría eliminada')
        if (filtro === String(objetivo.item.id)) setFiltro('todas')
      }
      cargar()
    } catch (e) {
      mostrar(e.message)
    }
  }

  if (cargando) {
    return (
      <div className="cargando">
        <Loader2 className="giro" size={26} />
      </div>
    )
  }

  return (
    <>
      <div className="titulo-pagina">
        <div>
          <h1>Mi menú</h1>
          <div className="subtexto">
            {productos.length} productos · {productos.filter((p) => p.disponible).length} disponibles
          </div>
        </div>
        <button
          className="boton principal"
          onClick={() => setEditando({ ...PRODUCTO_VACIO, categoriaId: filtro !== 'todas' && filtro !== 'sin' ? filtro : '' })}
        >
          <Plus size={18} />
          Agregar producto
        </button>
      </div>

      {error && <div className="aviso error">{error}</div>}

      <div className="chips">
        <button className={'chip' + (filtro === 'todas' ? ' activo' : '')} onClick={() => setFiltro('todas')}>
          Todo
        </button>
        {categoriasUsadas.map((c) => (
          <button key={c.id} className={'chip' + (filtro === String(c.id) ? ' activo' : '')} onClick={() => setFiltro(String(c.id))}>
            {c.nombre}
          </button>
        ))}
        {productos.some((p) => !p.categoria) && (
          <button className={'chip' + (filtro === 'sin' ? ' activo' : '')} onClick={() => setFiltro('sin')}>
            Sin categoría
          </button>
        )}
      </div>

      <div className="productos-grid seccion">
        {visibles.map((p) => (
          <div key={p.id} className={'tarjeta producto' + (p.disponible ? '' : ' apagado')}>
            {p.imagen ? (
              <img className="foto" src={p.imagen} alt="" onError={(e) => (e.currentTarget.style.visibility = 'hidden')} />
            ) : (
              <div className="foto">
                <ImageIcon size={22} />
              </div>
            )}
            <div className="producto-info">
              <b>{p.nombre}</b>
              <div className="precio">{soles(p.precio)}</div>
              <div className="subtexto">{p.disponible ? p.categoria?.nombre || 'Sin categoría' : 'Agotado'}</div>
              <div className="fila" style={{ marginTop: 8 }}>
                <button
                  className="boton secundario chico"
                  onClick={() =>
                    setEditando({
                      ...p,
                      precio: p.precio,
                      categoriaId: p.categoria?.id ? String(p.categoria.id) : ''
                    })
                  }
                >
                  <Pencil size={14} />
                  Editar
                </button>
                <button className="boton peligro chico" onClick={() => setBorrando({ tipo: 'producto', item: p })} aria-label="Eliminar">
                  <Trash2 size={14} />
                </button>
              </div>
            </div>
            <button
              className={'interruptor' + (p.disponible ? ' encendido' : '')}
              onClick={() => alternarDisponible(p)}
              aria-label={p.disponible ? 'Marcar agotado' : 'Marcar disponible'}
              title={p.disponible ? 'Disponible' : 'Agotado'}
            />
          </div>
        ))}
        {visibles.length === 0 && <div className="vacio">Todavía no hay productos aquí. Toca “Agregar producto”.</div>}
      </div>

      <section className="seccion">
        <div className="titulo-pagina">
          <h2 style={{ margin: 0, fontSize: 18 }}>Categorías</h2>
          <button className="boton secundario chico" onClick={() => setEditandoCategoria({})}>
            <Plus size={14} />
            Nueva categoría
          </button>
        </div>
        <p className="subtexto" style={{ marginTop: 0 }}>
          Puedes usar las categorías generales de PideCerca o crear las tuyas.
        </p>
        <div className="chips" style={{ flexWrap: 'wrap' }}>
          {categorias.map((c) =>
            c.negocio ? (
              <span key={c.id} className="chip fila" style={{ gap: 6 }}>
                <Tag size={14} />
                {c.nombre}
                <button className="boton-icono" style={{ width: 26, height: 26 }} onClick={() => setEditandoCategoria(c)} aria-label="Editar categoría">
                  <Pencil size={12} />
                </button>
                <button
                  className="boton-icono"
                  style={{ width: 26, height: 26 }}
                  onClick={() => setBorrando({ tipo: 'categoria', item: c })}
                  aria-label="Eliminar categoría"
                >
                  <Trash2 size={12} />
                </button>
              </span>
            ) : (
              <span key={c.id} className="chip" title="Categoría general">
                {c.nombre}
              </span>
            )
          )}
        </div>
        {propias.length === 0 && <div className="subtexto">Aún no creaste categorías propias.</div>}
      </section>

      {editando && (
        <FormularioProducto inicial={editando} categorias={categorias} alGuardar={guardarProducto} alCerrar={() => setEditando(null)} />
      )}

      {editandoCategoria && (
        <FormularioCategoria inicial={editandoCategoria} alGuardar={guardarCategoria} alCerrar={() => setEditandoCategoria(null)} />
      )}

      {borrando && (
        <Modal titulo="¿Eliminar?" alCerrar={() => setBorrando(null)}>
          <p style={{ marginTop: 0 }}>
            Vas a eliminar <b>{borrando.item.nombre}</b>.{' '}
            {borrando.tipo === 'producto'
              ? 'Si ya tiene pedidos, solo se marcará como agotado.'
              : 'Solo se puede si no tiene productos.'}
          </p>
          <div className="fila">
            <button className="boton secundario ancho" onClick={() => setBorrando(null)}>
              Cancelar
            </button>
            <button className="boton peligro ancho" onClick={confirmarBorrado}>
              Sí, eliminar
            </button>
          </div>
        </Modal>
      )}
    </>
  )
}
