package com.sfj3861.ctotriggers

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.net.Uri
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
 * GPTM Official menu. This is a menu shell; gameplay features stay marked unavailable
 * until a tested in-process loader and game adapter exist.
 */
class OverlayService : Service() {
    private var windowManager: WindowManager? = null
    private var bubble: TextView? = null
    private var bubbleParams: WindowManager.LayoutParams? = null
    private var panel: View? = null
    private var panelParams: WindowManager.LayoutParams? = null
    private var selectedTab = "player"
    private var contentContainer: LinearLayout? = null
    private val tabButtons = mutableMapOf<String, TextView>()
    private var language = "es"

    private val background = Color.rgb(17, 19, 29)
    private val surface = Color.rgb(29, 32, 47)
    private val accent = Color.rgb(139, 92, 246)
    private val primaryText = Color.rgb(245, 246, 255)
    private val secondaryText = Color.rgb(174, 180, 202)
    private val warning = Color.rgb(255, 202, 120)

    private val tabs = listOf(
        "player" to Pair("Jugador", "Player"),
        "visuals" to Pair("Visuales", "Visuals"),
        "botting" to Pair("Botteo", "Botting"),
        "editor" to Pair("Editor", "Editor"),
        "mods" to Pair("Mods", "Mods"),
        "music" to Pair("Música", "Music"),
        "system" to Pair("Sistema", "System")
    )

    override fun onCreate() {
        super.onCreate()
        language = getSharedPreferences("gptm", MODE_PRIVATE).getString("language", "es") ?: "es"
        if (!android.provider.Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createBubble()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun tr(es: String, en: String): String = if (language == "es") es else en

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
                        if (abs(event.rawX - downX) < dp(10) && abs(event.rawY - downY) < dp(10)) togglePanel()
                        true
                    }
                    else -> false
                }
            }
        }
        try { windowManager?.addView(bubble, bubbleParams) } catch (_: Exception) { stopSelf() }
    }

    private fun togglePanel() {
        if (panel != null) removePanel() else showPanel()
    }

    private fun showPanel() {
        if (panel != null || windowManager == null) return
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(this@OverlayService.background)
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
            text = "×"
            setOnClickListener { removePanel() }
        }, LinearLayout.LayoutParams(dp(48), dp(44)))
        root.addView(heading)

        root.addView(TextView(this).apply {
            text = tr("INTERFAZ OFICIAL · El motor nativo aún no está conectado", "OFFICIAL UI · Native engine is not connected yet")
            textSize = 11f
            setTextColor(warning)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setBackgroundColor(surface)
        }, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(6), 0, dp(10)) })

        val tabScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val tabRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        tabs.forEach { (key, labels) ->
            val tabView = TextView(this).apply {
                text = if (language == "es") labels.first else labels.second
                textSize = 13f
                gravity = Gravity.CENTER
                setPadding(dp(13), dp(11), dp(13), dp(11))
                setOnClickListener {
                    selectedTab = key
                    updateTabs()
                    renderTab()
                }
            }
            tabButtons[key] = tabView
            tabRow.addView(tabView, LinearLayout.LayoutParams(-2, -2).apply {
                setMargins(0, 0, dp(6), 0)
            })
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
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(TextView(this).apply {
            text = tr("GPTM · 1.0.0-dev · Funciones reales en desarrollo", "GPTM · 1.0.0-dev · Real features in development")
            textSize = 10f
            gravity = Gravity.CENTER
            setTextColor(secondaryText)
            setPadding(0, dp(10), 0, 0)
        })
        panelParams = WindowManager.LayoutParams(
            dp(340), dp(500), WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            0, PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }
        try {
            windowManager?.addView(root, panelParams)
            panel = root
            updateTabs()
            renderTab()
        } catch (_: Exception) {
            panel = null
            Toast.makeText(this, tr("No se pudo abrir el menú GPTM", "Could not open GPTM menu"), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTabs() {
        tabButtons.forEach { (key, view) ->
            view.setTextColor(if (key == selectedTab) Color.WHITE else secondaryText)
            view.setBackgroundColor(if (key == selectedTab) accent else surface)
        }
    }

    private fun renderTab() {
        val c = contentContainer ?: return
        c.removeAllViews()
        when (selectedTab) {
            "player" -> {
                section(c, tr("PRÁCTICA Y JUGABILIDAD", "PRACTICE & GAMEPLAY"))
                feature(c, "Practice checkpoints", tr("Gestión de checkpoints", "Checkpoint management"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Start Position helper", tr("Ayudas para posiciones iniciales", "Start position tools"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Auto-retry", tr("Reintento automático", "Automatic retry"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Practice music sync", tr("Sincronización de música en práctica", "Practice music sync"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Noclip / Ignore Damage", tr("Pruebas de colisión", "Collision testing"), tr("No implementado", "Not implemented"))
                feature(c, "Speedhack / TPS", tr("Control de velocidad y simulación", "Speed and tick-rate controls"), tr("No implementado", "Not implemented"))
                feature(c, "Input Display", tr("Visualización de pulsaciones", "Show input presses"), tr("Pendiente del loader", "Waiting for loader"))
            }
            "visuals" -> {
                section(c, tr("HUD Y RENDIMIENTO", "HUD & PERFORMANCE"))
                feature(c, "FPS Counter", tr("Contador de fotogramas", "Frame-rate counter"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Hitboxes", tr("Visualización de hitboxes", "Hitbox visualization"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Attempt Counter", tr("Contador de intentos", "Attempt counter"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Disable Screen Shake", tr("Control de sacudida de pantalla", "Screen-shake control"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Disable Pulses / Particles", tr("Control de efectos visuales", "Visual-effects controls"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Low Detail helper", tr("Ayudas para bajo detalle", "Low-detail helpers"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Level Progress", tr("Información de progreso", "Progress information"), tr("Pendiente del loader", "Waiting for loader"))
            }
            "botting" -> {
                section(c, tr("HERRAMIENTAS DE BOTEO", "BOTTING TOOLS"))
                feature(c, "Macro Recorder", tr("Grabar entradas para repetir una ruta", "Record inputs to replay a route"), tr("Pendiente del motor", "Waiting for game engine"))
                feature(c, "Macro Playback", tr("Reproducir una macro guardada", "Replay a saved macro"), tr("Pendiente del motor", "Waiting for game engine"))
                feature(c, "Frame-step", tr("Avanzar fotograma a fotograma", "Advance frame by frame"), tr("Pendiente del motor", "Waiting for game engine"))
                feature(c, "Frame Advance / Pause", tr("Pausar y avanzar con precisión", "Pause and advance precisely"), tr("Pendiente del motor", "Waiting for game engine"))
                feature(c, "Input Timeline", tr("Ver la secuencia de pulsaciones", "Inspect the input sequence"), tr("Pendiente del motor", "Waiting for game engine"))
                feature(c, "Practice Route Library", tr("Organizar macros por nivel", "Organize macros by level"), tr("Pendiente del motor", "Waiting for game engine"))
                note(c, tr("Las herramientas de bot aún no ejecutan entradas dentro de Geometry Dash. Se muestran como plan de funciones, no como opciones activas.", "Botting tools do not send inputs into Geometry Dash yet. These are planned features, not active controls."))
            }
            "editor" -> {
                section(c, tr("HERRAMIENTAS DEL EDITOR", "EDITOR TOOLS"))
                feature(c, "Object / Group ID Inspector", tr("Inspección de identificadores", "Inspect object and group IDs"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "C→T Trigger Planner", tr("Planificar triggers y secuencias", "Plan triggers and sequences"), tr("Herramienta independiente", "Standalone tool"))
                feature(c, "Layout Helper", tr("Ayudas para layouts", "Layout helpers"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Copy / Paste Objects", tr("Herramientas de objetos", "Object tools"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Trigger Plan Preview", tr("Vista previa de planificación", "Preview a trigger plan"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Group ID ↔ Decoration Link", tr("Vincular decoración con el layout", "Link decoration to layout"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Level Backup", tr("Copias de seguridad de niveles", "Level backups"), tr("Pendiente del loader", "Waiting for loader"))
            }
            "mods" -> {
                section(c, tr("CATÁLOGO DE MODS", "MOD CATALOG"))
                note(c, tr("Catálogo inicial de ideas de integración. No están instalados ni activos: GPTM todavía no dispone de un loader.", "Initial integration catalog. These are not installed or active: GPTM does not have a loader yet."))
                listOf(
                    "Geode Loader", "Mega Hack-style Practice Tools", "BetterInfo", "Level Comments", "Quick Search",
                    "BetterEdit", "Editor Tweaks", "Jukebox / NONG Manager", "Auto Nong", "FPS Bypass",
                    "Click Between Frames", "Prism Menu", "Death Tracker", "Icon Kit", "Music Preview",
                    "Object Explorer", "Trigger Helper", "Save Manager", "Input Display", "Replay Manager",
                    "Hide Attempts", "No Transition", "Low Detail Mode", "Screenshot Helper"
                ).forEach { feature(c, it, tr("Entrada de catálogo; integración por verificar", "Catalog entry; integration unverified"), tr("No instalado", "Not installed")) }
            }
            "music" -> {
                section(c, tr("MÚSICA Y NONG", "MUSIC & NONG"))
                note(c, tr("Song File Hub reúne canciones NONG para Geometry Dash. GPTM puede abrir su web; la descarga e instalación automática dentro del juego aún no están conectadas.", "Song File Hub hosts NONG tracks for Geometry Dash. GPTM can open its website; automatic download and in-game installation are not connected yet."))
                actionButton(c, tr("ABRIR SONG FILE HUB", "OPEN SONG FILE HUB")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://songfilehub.com/")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (_: Exception) {
                        Toast.makeText(this, tr("No hay navegador disponible", "No browser available"), Toast.LENGTH_SHORT).show()
                    }
                }
                feature(c, "Song File Hub", tr("Buscar canciones NONG en la web oficial", "Browse NONG tracks on the official site"), tr("Enlace web disponible", "Website link available"))
                feature(c, "NONG Download Manager", tr("Descarga y biblioteca local", "Downloads and local library"), tr("Pendiente de integración", "Integration pending"))
                feature(c, "Assign NONG to a level", tr("Vincular una canción con un nivel", "Assign a song to a level"), tr("Pendiente del loader", "Waiting for loader"))
                feature(c, "Audio format check", tr("Comprobar archivos de audio compatibles", "Check supported audio files"), tr("Pendiente", "Pending"))
            }
            "system" -> {
                section(c, tr("IDIOMA Y ESTADO", "LANGUAGE & STATUS"))
                note(c, tr("Selecciona el idioma del menú flotante:", "Choose the floating menu language:"))
                actionButton(c, "Español") { setLanguage("es") }
                actionButton(c, "English") { setLanguage("en") }
                feature(c, "Geometry Dash 2.2081", tr("Perfil A objetivo", "Target Profile A"), tr("Por verificar en dispositivo", "Needs on-device verification"))
                feature(c, "Geometry Dash 2.209", tr("Perfil separado", "Separate profile"), tr("No habilitado", "Not enabled"))
                feature(c, tr("Loader nativo", "Native loader"), tr("Carga dentro del proceso del juego", "Loads inside the game process"), tr("No implementado", "Not implemented"))
                feature(c, "Mod API", tr("Conexión entre menú y juego", "Connection between menu and game"), tr("No implementado", "Not implemented"))
                note(c, tr("No se sustituye el APK original. No marques una función como activa hasta que esté probada dentro del juego.", "The original APK is not replaced. No feature is marked active until tested in-game."))
            }
        }
    }

    private fun setLanguage(value: String) {
        language = value
        getSharedPreferences("gptm", MODE_PRIVATE).edit().putString("language", value).apply()
        tabs.forEach { (key, labels) ->
            tabButtons[key]?.text = if (value == "es") labels.first else labels.second
        }
        updateTabs()
        renderTab()
    }

    private fun section(parent: LinearLayout, title: String) {
        parent.addView(TextView(this).apply {
            text = title
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(190, 170, 255))
            setPadding(dp(3), dp(8), dp(3), dp(7))
        })
    }

    private fun note(parent: LinearLayout, textValue: String) {
        parent.addView(TextView(this).apply {
            text = textValue
            textSize = 12f
            setTextColor(secondaryText)
            setPadding(dp(4), dp(6), dp(4), dp(10))
        })
    }

    private fun actionButton(parent: LinearLayout, label: String, action: () -> Unit) {
        val button = Button(this).apply {
            text = label
            setOnClickListener { action() }
        }
        parent.addView(button, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, dp(2), 0, dp(5))
        })
    }

    private fun feature(parent: LinearLayout, name: String, description: String, state: String) {
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
            setTextColor(if (state.contains("Activo") || state.contains("available", true) || state.contains("disponible", true)) Color.rgb(120, 220, 160) else warning)
        })
        parent.addView(card, LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(0, 0, 0, dp(7))
        })
    }

    private fun removePanel() {
        val current = panel ?: return
        try { windowManager?.removeView(current) } catch (_: Exception) {}
        panel = null
        contentContainer = null
        tabButtons.clear()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        removePanel()
        bubble?.let { try { windowManager?.removeView(it) } catch (_: Exception) {} }
        bubble = null
        super.onDestroy()
    }
}
