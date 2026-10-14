import { Navigate, Route, Routes } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import Login from './pages/Login'
import Pedidos from './pages/Pedidos'
import Menu from './pages/Menu'
import Historial from './pages/Historial'
import Perfil from './pages/Perfil'

export default function App() {
  const { usuario, cargando } = useAuth()

  if (cargando) {
    return (
      <div className="cargando">
        <Loader2 className="giro" size={28} />
      </div>
    )
  }

  if (!usuario) {
    return (
      <Routes>
        <Route path="*" element={<Login />} />
      </Routes>
    )
  }

  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Pedidos />} />
        <Route path="/menu" element={<Menu />} />
        <Route path="/historial" element={<Historial />} />
        <Route path="/perfil" element={<Perfil />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
