package com.sfj3861.ctotriggers

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream

/**
 * NOVA package intake and validation layer.
 * This validates archives and stores them privately. It does not execute native code:
 * the in-game runtime/bootstrap is not implemented or verified yet.
 */
object NovaLoader {
    private const val MAX_ARCHIVE_BYTES = 32L * 1024L * 1024L
    private const val MAX_EXPANDED_BYTES = 64L * 1024L * 1024L
    private const val MAX_ENTRIES = 2048
    private const val MAX_MANIFEST_BYTES = 256L * 1024L
    private const val FORMAT_VERSION = 1

    data class Report(
        val acceptedForLibrary: Boolean,
        val runnable: Boolean,
        val packageId: String?,
        val displayName: String?,
        val version: String?,
        val status: String,
        val details: String
    )

    fun importAndValidate(context: Context, uri: Uri): Report {
        val libraryDir = File(context.filesDir, "nova-mods")
        if (!libraryDir.exists() && !libraryDir.mkdirs()) {
            return failure("No se pudo crear el directorio privado nova-mods.")
        }

        val temp = File.createTempFile("incoming-", ".nova", libraryDir)
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: return failure("Android no pudo abrir el archivo seleccionado.")
            input.use { source ->
                temp.outputStream().use { target ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        total += count
                        if (total > MAX_ARCHIVE_BYTES) {
                            throw IOException("El archivo supera el límite de 32 MiB.")
                        }
                        target.write(buffer, 0, count)
                    }
                }
            }

            val report = validateArchive(temp, GameDiagnostics.inspect(context))
            if (!report.acceptedForLibrary) return report

            val safeId = report.packageId!!.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val safeVersion = report.version!!.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val destination = File(libraryDir, "$safeId-$safeVersion.nova")
            if (destination.canonicalFile.parentFile != libraryDir.canonicalFile) {
                return failure("Destino de paquete no válido.")
            }
            if (!temp.renameTo(destination)) {
                temp.copyTo(destination, overwrite = true)
                temp.delete()
            }
            return report.copy(
                details = report.details + "\nGuardado en la biblioteca privada de NOVA.\n" +
                    if (report.runnable) "Elegible para una futura ejecución cuando exista un runtime compatible."
                    else "Archivado para inspección; no se ejecutará."
            )
        } catch (e: Exception) {
            return failure("Importación rechazada: ${e.message ?: e.javaClass.simpleName}")
        } finally {
            if (temp.exists()) temp.delete()
        }
    }

    private fun validateArchive(file: File, gameDiagnostics: GameDiagnostics): Report {
        if (file.length() <= 0L || file.length() > MAX_ARCHIVE_BYTES) {
            return failure("Tamaño de archivo inválido.")
        }

        var manifestText: String? = null
        val entryNames = linkedSetOf<String>()
        var expandedBytes = 0L
        var entryCount = 0

        ZipInputStream(file.inputStream().buffered()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                entryCount++
                if (entryCount > MAX_ENTRIES) return failure("Demasiadas entradas ZIP.")
                val name = entry.name
                if (!isSafeArchivePath(name)) return failure("Ruta peligrosa en ZIP: $name")
                if (!entryNames.add(name)) return failure("Entrada ZIP duplicada: $name")

                var entryBytes = 0L
                val buffer = ByteArray(8192)
                val captureManifest = name == "manifest.json" && !entry.isDirectory
                val manifest = if (captureManifest) java.io.ByteArrayOutputStream() else null
                while (true) {
                    val count = zip.read(buffer)
                    if (count < 0) break
                    entryBytes += count
                    expandedBytes += count
                    if (expandedBytes > MAX_EXPANDED_BYTES) {
                        return failure("El contenido expandido supera 64 MiB.")
                    }
                    if (captureManifest && entryBytes > MAX_MANIFEST_BYTES) {
                        return failure("manifest.json supera 256 KiB.")
                    }
                    manifest?.write(buffer, 0, count)
                }
                if (captureManifest) {
                    if (manifestText != null) return failure("Hay más de un manifest.json en la raíz.")
                    manifestText = manifest!!.toString(Charsets.UTF_8.name())
                }
                zip.closeEntry()
            }
        }

        if (manifestText == null) return failure("Falta manifest.json en la raíz del .nova.")
        val json = try {
            JSONObject(manifestText)
        } catch (_: Exception) {
            return failure("manifest.json no contiene JSON válido.")
        }

        val format = json.optString("format")
        val formatVersion = json.optInt("format_version", -1)
        val id = json.optString("id")
        val name = json.optString("name")
        val version = json.optString("version")
        val author = json.optString("author")
        val runtime = json.optString("runtime")
        val status = json.optString("status", "unknown")
        val entrypoint = json.optString("entrypoint")

        if (format != "nova") return failure("Formato no compatible: se esperaba 'nova'.")
        if (formatVersion != FORMAT_VERSION) return failure("Versión del formato NOVA no soportada: $formatVersion.")
        if (!ID_PATTERN.matches(id)) return failure("Identificador de mod inválido.")
        if (name.isBlank() || version.isBlank() || author.isBlank() || runtime.isBlank()) {
            return failure("Faltan campos obligatorios: name, version, author o runtime.")
        }
        if (!isSafeArchivePath(entrypoint) || entrypoint.isBlank() || entrypoint.endsWith("/")) {
            return failure("Ruta de entrypoint inválida.")
        }
        if (entrypoint !in entryNames) return failure("El entrypoint declarado no existe: $entrypoint.")

        val target = json.optJSONObject("target") ?: return failure("Falta target en el manifest.")
        val game = target.optString("game")
        if (!game.contains("Geometry Dash", ignoreCase = true)) {
            return failure("Este paquete no declara Geometry Dash como juego objetivo.")
        }

        val targetAbis = target.optJSONArray("abis")
        val deviceAbis = android.os.Build.SUPPORTED_ABIS.toList()
        val abiCompatible = targetAbis == null || (0 until targetAbis.length())
            .map { targetAbis.optString(it) }
            .any { it in deviceAbis }
        val targetProfiles = target.optJSONArray("profiles")
        val profileVersions = mutableListOf<String>()
        if (targetProfiles != null) {
            for (index in 0 until targetProfiles.length()) {
                val item = targetProfiles.optJSONObject(index)
                if (item != null) profileVersions += item.optString("version")
            }
        }
        val profileCompatible = profileVersions.isEmpty() ||
            profileVersions.any { marker ->
                marker.isNotBlank() && gameDiagnostics.profile.contains(marker)
            }
        val declaredRunnable = status in setOf("ready", "runnable", "stable") &&
            !entrypoint.endsWith(".md", ignoreCase = true)
        val reportText = buildString {
            append("Paquete: $name\nID: $id\nVersión: $version\nAutor: $author\n")
            append("Runtime declarado: $runtime\nEstado declarado: $status\n")
            append("Entradas ZIP: $entryCount\nTamaño expandido: $expandedBytes bytes\n")
            append("Entrypoint: $entrypoint\n")
            append("ABI del dispositivo: " + (deviceAbis.firstOrNull() ?: "desconocida") + "\n")
            append("ABI compatible según manifest: " + if (abiCompatible) "sí\n" else "no\n")
            append("Perfil detectado: " + gameDiagnostics.profile + "\n")
            append("Perfil compatible según manifest: " + if (profileCompatible) "posible\n" else "no\n")
            if (!declaredRunnable) {
                append("\nPaquete de metadatos/documentación: no declara un payload ejecutable.")
            }
            append("\nEstado real: no ejecutable todavía; falta un runtime NOVA nativo verificado para este build.")
        }
        return Report(
            acceptedForLibrary = true,
            runnable = false,
            packageId = id,
            displayName = name,
            version = version,
            status = status,
            details = reportText
        )
    }

    private fun isSafeArchivePath(path: String): Boolean {
        if (path.isBlank() || path.startsWith("/") || path.startsWith("\\") || path.contains("\u0000")) return false
        if (Regex("^[A-Za-z]:").containsMatchIn(path)) return false
        val normalized = path.replace('\\', '/')
        return normalized.split('/').none { it == ".." || it == "." }
    }

    private fun failure(message: String) = Report(
        acceptedForLibrary = false,
        runnable = false,
        packageId = null,
        displayName = null,
        version = null,
        status = "rejected",
        details = message
    )

    private val ID_PATTERN = Regex("[A-Za-z0-9]+([._-][A-Za-z0-9]+)*")
}
