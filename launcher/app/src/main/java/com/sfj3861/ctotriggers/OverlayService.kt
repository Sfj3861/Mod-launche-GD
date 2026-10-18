package com.sfj3861.ctotriggers

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

/**
 * GPTM Official menu shell.
 *
 * This version organizes the menu but intentionally does not expose fake gameplay
 * toggles. A feature is shown as unavailable until a real, tested game adapter exists.
 */
class OverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var bubble: TextView? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var panel: View? = null
    private var selectedTab = "Jugador"
    private var contentContainer: LinearLayout? = null
    private var tabButtons = mutableMapOf<String, TextView>()

    private val background = Color.rgb(17, 19, 29)
    private val surface = Color.rgb(29, 32, 47)
    private val accent = Color.rgb(139, 92, 246)
    private val primaryText = Color.rgb(245, 246, 255)
    private val secondaryText = Color.rgb(174, 180, 202)
    private val warning = Color.rgb(255, 202, 120)

    override fun onCreate() {
        super.onCreate()
        if (!android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createBubble()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createBubble() {
        bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(18)
            y = dp(220)
        }

        bubble = TextView(this).apply {
            text = "G"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setBackgroundColor(accent)
            setPadding(dp(17), dp(12), dp(17), dp(12))
            var downX = 0f
            var downY = 0f
            var startX = 0
            var startY = 0
            setOnTouchListener { view, event ->
                val params = bubbleParams ?: return@setOnTouchListener false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX
                        downY = event.rawY
                        startX = params.x
                        startY = params.y
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = startX + (event.rawX - downX).toInt()
                        params.y = startY + (event.rawY - downY).toInt()
                        windowManager?.updateViewLayout(view, params)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (abs(event.rawX - downX) < dp(10) && abs(event.rawY - downY) < dp(10)) {
                            togglePanel()
                        }
                        true
                    }
                    else -> false
                }
            }
        }
        try {
            windowManager?.addView(bubble, bubbleParams)
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun togglePanel() {
        if (panel != null) removePanel() else showPanel()
    }

    private fun showPanel() {
        if (panel != null || windowManager == null) return

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(background)
            setPadding(dp(12), dp(10), dp(12), dp(12))
        }

        val heading = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val titleBlock = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        titleBlock.addView(TextView(this).apply {
            text = "GPTM OFFICIAL"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(primaryText)
        })
        titleBlock.addView(TextView(this).apply {
            text = "Geometry Dash Android • Perfil A"
            textSize = 11f
            setTextColor(secondaryText)
        })
        heading.addView(titleBlock, LinearLayout.LayoutParams(0, dp(48), 1f))
        heading.addView(Button(this).apply {
            text = "—"
            setOnClickListener { removePanel() }
        }, LinearLayout.LayoutParams(dp(48), dp(44)))
        root.addView(heading)

        val status = TextView(this).apply {
            text = "MODO INTERFAZ · El motor nativo aún no está conectado"
            textSize = 11f
            setTextColor(warning)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setBackgroundColor(surface)
        }
        val statusParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        statusParams.setMargins(0, dp(6), 0, dp(10))
        root.addView(status, statusParams)

        val tabScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val tabRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("Jugador", "Visuales", "Editor", "Sistema").forEach { tab ->
            val tabView = TextView(this).apply {
                text = tab
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(13), dp(11), dp(13), dp(11))
                setOnClickListener {
                    selectedTab = tab
                    updateTabs()
                    renderTab()
                }
            }
            tabButtons[tab] = tabView
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, dp(6), 0)
            tabRow.addView(tabView, params)
        }
        tabScroll.addView(tabRow)
        root.addView(tabScroll)

        val scroll = ScrollView(this).apply {
            isFillViewport = false
            contentContainer = LinearLayout(this@OverlayService).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(8), 0, 0)
            }
            addView(contentContainer)
        }
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        val footer = TextView(this).apply {
            text = "GPTM · 1.0.0-dev · No modifica el juego en esta fase"
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(secondaryText)
            setPadding(0, dp(10), 0, 0)
        }
        root.addView(footer)

        panelParams = WindowManager.LayoutParams(
            dp(330),
            dp(470),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        try {
            windowManager?.addView(root, panelParams)
            panel = root
            updateTabs()
            renderTab()
        } catch (_: Exception) {
            panel = null
            Toast.makeText(this, "No se pudo abrir el menú GPTM", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTabs() {
        tabButtons.forEach { (name, view) ->
            view.setTextColor(if (name == selectedTab) Color.WHITE else secondaryText)
            view.setBackgroundColor(if (name == selectedTab) accent else surface)
        }
    }

    private fun renderTab() {
        val container = contentContainer ?: return
        container.removeAllViews()
        when (selectedTab) {
            "Jugador" -> {
                addSection(container, "PRÁCTICA Y JUGABILIDAD")
                addFeature(container, "Practice checkpoints", "Gestión de checkpoints", "Pendiente del motor")
                addFeature(container, "Start Position helper", "Herramientas para posiciones iniciales", "Pendiente del motor")
                addFeature(container, "Auto-retry", "Reintento automático", "Pendiente del motor")
                addFeature(container, "Practice music sync", "Sincronización de música en práctica", "Pendiente del motor")
                addFeature(container, "Noclip / Ignore damage", "Herramientas de pruebas de colisión", "No disponible")
                addFeature(container, "Speedhack / TPS", "Control de velocidad y simulación", "No disponible")
                addFeature(container, "Input display", "Visualización de pulsaciones", "Pendiente del motor")
            }
            "Visuales" -> {
                addSection(container, "HUD Y RENDIMIENTO")
                addFeature(container, "FPS counter", "Contador de fotogramas", "Pendiente del motor")
                addFeature(container, "Hitboxes", "Visualización de hitboxes", "Pendiente del motor")
                addFeature(container, "Attempt counter", "Contador de intentos", "Pendiente del motor")
                addFeature(container, "Disable screen shake", "Control de sacudida de pantalla", "Pendiente del motor")
                addFeature(container, "Disable pulses / particles", "Control de efectos visuales", "Pendiente del motor")
                addFeature(container, "Low Detail helper", "Ayudas para bajo detalle", "Pendiente del motor")
                addFeature(container, "Level progress", "Información de progreso", "Pendiente del motor")
            }
            "Editor" -> {
                addSection(container, "HERRAMIENTAS DE CREACIÓN")
                addFeature(container, "Object / Group ID inspector", "Inspección de identificadores", "Pendiente del motor")
                addFeature(container, "C→T trigger planner", "Planificador de comandos y triggers", "Herramienta independiente")
                addFeature(container, "Layout helper", "Ayudas para layouts", "Pendiente del motor")
                addFeature(container, "Copy / paste objects", "Herramientas de objetos", "Pendiente del motor")
                addFeature(container, "Trigger plan preview", "Vista previa de planificación", "Pendiente del motor")
            }
            "Sistema" -> {
                addSection(container, "ESTADO DE INTEGRACIÓN")
                addFeature(container, "Loader nativo", "Carga dentro del proceso del juego", "No implementado")
                addFeature(container, "Perfil 2.2081", "Compatibilidad objetivo", "Por verificar en dispositivo")
                addFeature(container, "Perfil 2.209", "Perfil separado", "No habilitado")
                addFeature(container, "API de mods", "Conexión entre menú y juego", "No implementado")
                addFeature(container, "Seguridad", "No modifica ni reemplaza el APK original", "Activo")
                container.addView(TextView(this).apply {
                    text = "Las opciones de juego permanecen desactivadas hasta disponer de un loader real y una prueba en la versión objetivo. No se mostrarán interruptores que aparenten funcionar."
                    textSize = 12f
                    setTextColor(secondaryText)
                    setPadding(dp(4), dp(10), dp(4), dp(4))
                })
            }
        }
    }

    private fun addSection(parent: LinearLayout, name: String) {
        parent.addView(TextView(this).apply {
            text = name
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(190, 170, 255))
            setPadding(dp(3), dp(8), dp(3), dp(7))
        })
    }

    private fun addFeature(parent: LinearLayout, name: String, description: String, state: String) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(surface)
            setPadding(dp(11), dp(10), dp(11), dp(10))
        }
        card.addView(TextView(this).apply {
            text = name
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(primaryText)
        })
        card.addView(TextView(this).apply {
            text = description
            textSize = 11f
            setTextColor(secondaryText)
            setPadding(0, dp(3), 0, dp(6))
        })
        card.addView(TextView(this).apply {
            text = "• $state"
            textSize = 10f
            setTextColor(if (state == "Activo") Color.rgb(120, 220, 160) else warning)
        })
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, dp(7))
        parent.addView(card, params)
    }

    private fun removePanel() {
        val current = panel ?: return
        try {
            windowManager?.removeView(current)
        } catch (_: Exception) {
            // The system may already have removed the overlay.
        }
        panel = null
        contentContainer = null
        tabButtons.clear()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        removePanel()
        bubble?.let {
            try { windowManager?.removeView(it) } catch (_: Exception) {}
        }
        bubble = null
        super.onDestroy()
    }
}
