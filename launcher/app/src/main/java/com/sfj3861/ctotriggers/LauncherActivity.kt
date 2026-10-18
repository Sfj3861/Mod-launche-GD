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
    private var detectedPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 28, 24, 28)
        }

        val title = TextView(this).apply {
            text = "GPTM OFFICIAL"
            textSize = 26f
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Versión 1.0.0-dev.1\nPerfil A · Geometry Dash Android 2.2081\nMenú organizado y diagnóstico"
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
    )

    private fun refreshDiagnostics() {
        val result = GameDiagnostics.inspect(this)
        detectedPackage = if (result.installed) result.packageName else null
        diagnosticsText?.text = buildString {
            append("ESTADO: ${if (result.installed) "Juego detectado" else "Juego no detectado"}\n")
            append("PERFIL A: ${result.profile}\n\n")
            append(result.details)
            append("\n\nESTADO OFICIAL: el menú ya está organizado, pero el loader nativo y la API de mods aún no están implementados. Esta aplicación no modifica el proceso del juego.")
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
