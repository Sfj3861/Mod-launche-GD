package com.sfj3861.ctotriggers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class LauncherActivity : Activity() {
    private var diagnosticsText: TextView? = null
    private var loaderText: TextView? = null
    private var detectedPackage: String? = null

    companion object {
        private const val REQUEST_NOVA_FILE = 4201
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 28, 24, 28)
        }

        val title = TextView(this).apply {
            text = "NOVA LOADER · GPTM"
            textSize = 26f
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Versión 1.0.0-dev.2\nPerfil A · Geometry Dash Android 2.2081\nDiagnóstico y validador de paquetes NOVA"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 14, 0, 18)
        }
        val diagnostics = TextView(this).apply {
            textSize = 13f
            setTextIsSelectable(true)
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xFFEFEFF4.toInt())
        }
        diagnosticsText = diagnostics

        val refresh = Button(this).apply {
            text = "ANALIZAR INSTALACIÓN"
            setOnClickListener { refreshDiagnostics() }
        }
        val importNova = Button(this).apply {
            text = "IMPORTAR / VALIDAR .NOVA"
            setOnClickListener { chooseNovaPackage() }
        }
        val loaderStatus = TextView(this).apply {
            textSize = 13f
            setTextIsSelectable(true)
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xFFE8EAF6.toInt())
            text = "NOVA Loader: validador de paquetes listo.\nRuntime nativo del juego: no implementado."
        }
        loaderText = loaderStatus

        val launch = Button(this).apply {
            text = "ABRIR GEOMETRY DASH"
            setOnClickListener { launchGame() }
        }
        val enableOverlay = Button(this).apply {
            text = "ACTIVAR MENÚ FLOTANTE"
            setOnClickListener { enableFloatingButton() }
        }
        val stopOverlay = Button(this).apply {
            text = "CERRAR MENÚ FLOTANTE"
            setOnClickListener {
                stopService(Intent(this@LauncherActivity, OverlayService::class.java))
                Toast.makeText(this@LauncherActivity, "Menú GPTM cerrado", Toast.LENGTH_SHORT).show()
            }
        }

        root.addView(title, matchWrap())
        root.addView(subtitle, matchWrap())
        root.addView(diagnostics, matchWrap())
        root.addView(refresh, matchWrap())
        root.addView(importNova, matchWrap())
        root.addView(loaderStatus, matchWrap())
        root.addView(launch, matchWrap())
        root.addView(enableOverlay, matchWrap())
        root.addView(stopOverlay, matchWrap())

        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
        refreshDiagnostics()
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = 8 }

    private fun chooseNovaPackage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TITLE, "Selecciona un paquete .nova")
        }
        try {
            startActivityForResult(intent, REQUEST_NOVA_FILE)
        } catch (_: Exception) {
            Toast.makeText(this, "No se pudo abrir el selector de archivos.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Uses the compatible Activity result callback for the current minimum SDK.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_NOVA_FILE || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        loaderText?.text = "Analizando paquete…"
        val report = NovaLoader.importAndValidate(this, uri)
        loaderText?.text = buildString {
            append(if (report.acceptedForLibrary) "PAQUETE ACEPTADO EN BIBLIOTECA" else "PAQUETE RECHAZADO")
            append("\n")
            append(if (report.runnable) "Estado del paquete: candidato ejecutable" else "Estado del paquete: no ejecutable")
            append("\n\n")
            append(report.details)
            append("\n\nIMPORTANTE: la validación no equivale a cargar el mod dentro de Geometry Dash.")
        }
        Toast.makeText(
            this,
            if (report.acceptedForLibrary) "Paquete NOVA validado y guardado" else "Paquete NOVA rechazado",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun refreshDiagnostics() {
        val result = GameDiagnostics.inspect(this)
        detectedPackage = if (result.installed) result.packageName else null
        diagnosticsText?.text = buildString {
            append("ESTADO: \${if (result.installed) "Juego detectado" else "Juego no detectado"}\n")
            append("PERFIL A: \${result.profile}\n\n")
            append(result.details)
            append("\n\nINTEGRACIÓN: el análisis de Geode ha servido para separar launcher, bootstrap nativo y runtime. NOVA ya valida e importa paquetes, pero no ejecuta código dentro del juego.")
        }
    }

    private fun enableFloatingButton() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Activa 'Mostrar sobre otras apps' y vuelve a GPTM.", Toast.LENGTH_LONG).show()
            return
        }
        startService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Menú flotante GPTM abierto; las funciones de juego siguen pendientes.", Toast.LENGTH_LONG).show()
    }

    private fun launchGame() {
        val gamePackage = detectedPackage
        if (gamePackage == null) {
            Toast.makeText(this, "No se ha detectado el paquete del juego. Pulsa ANALIZAR INSTALACIÓN.", Toast.LENGTH_LONG).show()
            return
        }
        val launchIntent = packageManager.getLaunchIntentForPackage(gamePackage)
        if (launchIntent == null) {
            Toast.makeText(this, "El paquete $gamePackage no tiene una actividad de inicio accesible.", Toast.LENGTH_LONG).show()
            return
        }
        startActivity(launchIntent)
    }
}
