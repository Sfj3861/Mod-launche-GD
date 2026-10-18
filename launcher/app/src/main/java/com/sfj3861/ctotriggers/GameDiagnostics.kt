package com.sfj3861.ctotriggers

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

/**
 * Read-only diagnostics for Profile A (Geometry Dash Android 2.2081).
 *
 * This class deliberately does not patch, inject into, or modify the game.
 * Android package metadata often reports a broad versionName (for example "2.2"),
 * so a target profile is only identified when the metadata contains a reliable
 * target marker. Otherwise the result remains UNKNOWN and needs manual evidence.
 */
data class GameDiagnostics(
    val installed: Boolean,
    val packageName: String,
    val versionName: String?,
    val versionCode: Long?,
    val nativeLibraryDir: String?,
    val abi: String,
    val cocosLibraryFound: Boolean,
    val profile: String,
    val details: String
) {
    companion object {
        const val PROFILE_A = "GD_ANDROID_2_2081"
        private const val DEFAULT_PACKAGE = "com.robtopx.geometryjump"

        fun inspect(context: Context): GameDiagnostics {
            val pm = context.packageManager
            val packageName = findGamePackage(pm) ?: DEFAULT_PACKAGE
            val info = try {
                if (Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(packageName, 0)
                }
            } catch (_: Exception) {
                return GameDiagnostics(
                    installed = false,
                    packageName = packageName,
                    versionName = null,
                    versionCode = null,
                    nativeLibraryDir = null,
                    abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
                    cocosLibraryFound = false,
                    profile = "NO INSTALADO",
                    details = "No se encontró el paquete $packageName. Si tu GDPS usa otro paquete, hay que identificarlo."
                )
            }

            val appInfo = try {
                if (Build.VERSION.SDK_INT >= 33) {
                    pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getApplicationInfo(packageName, 0)
                }
            } catch (_: Exception) {
                null
            }

            @Suppress("DEPRECATION")
            val code = if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()
            val versionName = info.versionName
            val nativeDir = appInfo?.nativeLibraryDir
            val nativeFile = nativeDir?.let { File(it, "libcocos2dcpp.so") }
            val cocosFound = nativeFile?.exists() == true

            val profile = identifyProfile(versionName, code)
            val details = buildString {
                append("Paquete: $packageName\n")
                append("versionName: ${versionName ?: "desconocida"}\n")
                append("versionCode: $code\n")
                append("ABI del dispositivo: ${Build.SUPPORTED_ABIS.joinToString()}\n")
                append("Biblioteca nativa: ${nativeDir ?: "ruta no expuesta"}\n")
                append("libcocos2dcpp.so visible: ${if (cocosFound) "sí" else "no"}\n")
                append("Perfil detectado: $profile")
                if (profile == "DESCONOCIDO") {
                    append("\nAndroid puede mostrar una versión genérica; no se habilitará el perfil 2.2081 sin confirmar la compilación.")
                }
                if (!cocosFound) {
                    append("\nLa ausencia en esta ruta no demuestra que el juego no la tenga: puede estar empaquetada o no ser accesible desde esta app.")
                }
            }

            return GameDiagnostics(
                installed = true,
                packageName = packageName,
                versionName = versionName,
                versionCode = code,
                nativeLibraryDir = nativeDir,
                abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
                cocosLibraryFound = cocosFound,
                profile = profile,
                details = details
            )
        }

        private fun findGamePackage(pm: PackageManager): String? {
            val candidates = listOf(DEFAULT_PACKAGE, "com.robtopx.geometryjump2")
            val known = candidates.firstOrNull { candidate ->
                try {
                    if (Build.VERSION.SDK_INT >= 33) {
                        pm.getPackageInfo(candidate, PackageManager.PackageInfoFlags.of(0))
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getPackageInfo(candidate, 0)
                    }
                    true
                } catch (_: Exception) {
                    false
                }
            }
            if (known != null) return known

            // GDPS builds may use a different package ID. Search visible launcher
            // activities by app label, but never mistake the Geode launcher itself
            // for the game package.
            val launcherIntent = android.content.Intent(android.content.Intent.ACTION_MAIN).addCategory(
                android.content.Intent.CATEGORY_LAUNCHER
            )
            val activities = try {
                if (Build.VERSION.SDK_INT >= 33) {
                    pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.queryIntentActivities(launcherIntent, 0)
                }
            } catch (_: Exception) {
                emptyList()
            }
            return activities.firstOrNull { resolveInfo ->
                val pkg = resolveInfo.activityInfo?.packageName ?: return@firstOrNull false
                val label = resolveInfo.loadLabel(pm)?.toString().orEmpty()
                pkg != "com.geode.launcher" &&
                    (label.contains("geometry dash", ignoreCase = true) ||
                     label.contains("gdps", ignoreCase = true))
            }?.activityInfo?.packageName
        }

        private fun identifyProfile(versionName: String?, versionCode: Long): String {
            val version = versionName.orEmpty()
            return if (
                version.contains("2.2081", ignoreCase = true) ||
                versionCode.toString() == "22081" ||
                versionCode.toString() == "220810"
            ) {
                "PERFIL A (2.2081) — CANDIDATO"
            } else {
                "DESCONOCIDO"
            }
        }
    }
}
