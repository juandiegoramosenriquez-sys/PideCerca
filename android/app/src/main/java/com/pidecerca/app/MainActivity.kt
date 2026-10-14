package com.pidecerca.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pidecerca.app.data.Avisos
import com.pidecerca.app.ui.AppViewModel
import com.pidecerca.app.ui.components.Cargando
import com.pidecerca.app.ui.screens.AvisosScreen
import com.pidecerca.app.ui.screens.BarraCarrito
import com.pidecerca.app.ui.screens.CarritoScreen
import com.pidecerca.app.ui.screens.InicioScreen
import com.pidecerca.app.ui.screens.LoginScreen
import com.pidecerca.app.ui.screens.MisPedidosScreen
import com.pidecerca.app.ui.screens.NegocioScreen
import com.pidecerca.app.ui.screens.PagoScreen
import com.pidecerca.app.ui.screens.PedidoScreen
import com.pidecerca.app.ui.screens.PerfilScreen
import com.pidecerca.app.ui.screens.RegistroScreen
import com.pidecerca.app.ui.theme.LocalPaleta
import com.pidecerca.app.ui.theme.PideCercaTema
import com.pidecerca.app.ui.theme.crearPaleta
import java.time.LocalTime

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()
    private var pedidoPendiente by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        leerIntent(intent)
        setContent {
            val sistemaOscuro = isSystemInDarkTheme()
            val oscuro = when (vm.modo) {
                "oscuro" -> true
                "claro" -> false
                else -> {
                    val hora = LocalTime.now().hour
                    if (hora in 6..17) sistemaOscuro else true
                }
            }
            val paleta = crearPaleta(vm.colorMarca, oscuro)
            LaunchedEffect(oscuro) {
                val color = paleta.fondo.toArgb()
                enableEdgeToEdge(
                    statusBarStyle = if (oscuro) SystemBarStyle.dark(color) else SystemBarStyle.light(color, color),
                    navigationBarStyle = if (oscuro) SystemBarStyle.dark(color) else SystemBarStyle.light(color, color)
                )
            }
            PideCercaTema(paleta) {
                Box(Modifier.fillMaxSize().background(paleta.fondo)) {
                    when {
                        vm.iniciando -> Cargando()
                        vm.usuario == null -> Acceso(vm)
                        else -> Principal(vm, pedidoPendiente) { pedidoPendiente = null }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        leerIntent(intent)
    }

    private fun leerIntent(intent: Intent?) {
        val id = intent?.getLongExtra(EXTRA_PEDIDO, -1L) ?: -1L
        if (id > 0) pedidoPendiente = id
    }

    companion object {
        const val EXTRA_PEDIDO = "pedidoId"
    }
}

@Composable
private fun Acceso(vm: AppViewModel) {
    var registrando by rememberSaveable { mutableStateOf(false) }
    if (registrando) {
        RegistroScreen(vm) { registrando = false }
    } else {
        LoginScreen(vm) { registrando = true }
    }
}

private data class Pestana(val ruta: String, val texto: String, val icono: ImageVector)

private val PESTANAS = listOf(
    Pestana("inicio", "Inicio", Icons.Default.Home),
    Pestana("pedidos", "Pedidos", Icons.Default.List),
    Pestana("avisos", "Avisos", Icons.Default.Notifications),
    Pestana("perfil", "Perfil", Icons.Default.Person)
)

@Composable
private fun Principal(vm: AppViewModel, pedidoPendiente: Long?, alAbrirPendiente: () -> Unit) {
    val p = LocalPaleta.current
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val enPestana = PESTANAS.any { it.ruta == rutaActual }
    var notas by remember { mutableStateOf("") }

    val permisoNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val contexto = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Avisos.puedeNotificar(contexto)) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(pedidoPendiente) {
        pedidoPendiente?.let {
            nav.navigate("pedido/$it")
            alAbrirPendiente()
        }
    }

    Scaffold(
        containerColor = p.fondo,
        bottomBar = {
            if (enPestana) {
                NavigationBar(containerColor = p.tarjeta, tonalElevation = 0.dp) {
                    PESTANAS.forEach { pestana ->
                        NavigationBarItem(
                            selected = rutaActual == pestana.ruta,
                            onClick = { irAPestana(nav, pestana.ruta) },
                            icon = {
                                if (pestana.ruta == "avisos" && vm.noLeidas > 0) {
                                    BadgedBox(badge = { Badge(containerColor = p.nuevo) { Text("${vm.noLeidas}") } }) {
                                        Icon(pestana.icono, contentDescription = pestana.texto)
                                    }
                                } else {
                                    Icon(pestana.icono, contentDescription = pestana.texto)
                                }
                            },
                            label = { Text(pestana.texto) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = p.marcaTexto,
                                selectedTextColor = p.marca,
                                indicatorColor = p.marca,
                                unselectedIconColor = p.texto2,
                                unselectedTextColor = p.texto2
                            )
                        )
                    }
                }
            }
        }
    ) { relleno ->
        Box(Modifier.fillMaxSize().padding(bottom = relleno.calculateBottomPadding())) {
            NavHost(
                navController = nav,
                startDestination = "inicio",
                modifier = Modifier.fillMaxSize()
            ) {
                composable("inicio") {
                    Box(Modifier.statusBarsPadding()) {
                        InicioScreen(vm) { nav.navigate("negocio/$it") }
                    }
                }
                composable("pedidos") {
                    Box(Modifier.statusBarsPadding()) {
                        MisPedidosScreen(vm) { nav.navigate("pedido/$it") }
                    }
                }
                composable("avisos") {
                    Box(Modifier.statusBarsPadding()) {
                        AvisosScreen(vm) { nav.navigate("pedido/$it") }
                    }
                }
                composable("perfil") {
                    Box(Modifier.statusBarsPadding()) { PerfilScreen(vm) }
                }
                composable("negocio/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                    NegocioScreen(
                        vm,
                        e.arguments?.getLong("id") ?: 0L,
                        volver = { nav.popBackStack() },
                        abrirCarrito = { nav.navigate("carrito") }
                    )
                }
                composable("carrito") {
                    CarritoScreen(
                        vm,
                        notas = notas,
                        alCambiarNotas = { notas = it },
                        volver = { nav.popBackStack() },
                        irAPagar = { nav.navigate("pago") },
                        irAInicio = { irAPestana(nav, "inicio") }
                    )
                }
                composable("pago") {
                    PagoScreen(vm, notas, volver = { nav.popBackStack() }) { id ->
                        notas = ""
                        nav.navigate("pedido/$id") {
                            popUpTo(nav.graph.findStartDestination().id)
                        }
                    }
                }
                composable("pedido/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { e ->
                    PedidoScreen(vm, e.arguments?.getLong("id") ?: 0L) { nav.popBackStack() }
                }
            }

            if (enPestana && rutaActual != "perfil" && vm.cantidadCarrito > 0) {
                Box(Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                    BarraCarrito(vm.cantidadCarrito, vm.totalCarrito) { nav.navigate("carrito") }
                }
            }
        }
    }
}

private fun irAPestana(nav: NavHostController, ruta: String) {
    nav.navigate(ruta) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
