package com.evelolvetech.util

import com.evelolvetech.data.models.UploadedFilePaths
import io.ktor.http.content.*
import io.ktor.utils.io.*
import java.io.File
import java.util.*

object FileUploadUtil {

    const val UPLOAD_DIR = "uploads"
    const val NID_DIR = "$UPLOAD_DIR/nids"
    const val PROFILE_DIR = "$UPLOAD_DIR/profiles"

    fun init() {
        File(NID_DIR).mkdirs()
        File(PROFILE_DIR).mkdirs()
    }

    private val allowedExtensions = setOf("jpg", "jpeg", "png", "gif")

    private fun isValidImageFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return extension in allowedExtensions
    }

    suspend fun saveNidImage(fileItem: PartData.FileItem, prefix: String): String? {
        return try {
            val originalFileName = fileItem.originalFileName
            if (originalFileName == null) return null

            if (!isValidImageFile(originalFileName)) {
                return null
            }

            val extension = originalFileName.substringAfterLast(".", "jpg").lowercase()
            val fileName = "${prefix}_${UUID.randomUUID()}.${extension}"
            val file = File("$NID_DIR/$fileName")

            val input = fileItem.provider()
            file.outputStream().use { output ->
                val buffer = ByteArray(8192)
                while (true) {
                    val bytesRead = input.readAvailable(buffer)
                    if (bytesRead == -1) break
                    output.write(buffer, 0, bytesRead)
                }
            }

            if (file.length() == 0L) {
                file.delete()
                return null
            }

            fileName
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveProfileImage(fileItem: PartData.FileItem): String? {
        return try {
            val originalFileName = fileItem.originalFileName
            if (originalFileName == null) return null

            if (!isValidImageFile(originalFileName)) {
                return null
            }

            val extension = originalFileName.substringAfterLast(".", "jpg").lowercase()
            val fileName = "profile_${UUID.randomUUID()}.${extension}"
            val file = File("$PROFILE_DIR/$fileName")

            val input = fileItem.provider()
            file.outputStream().use { output ->
                val buffer = ByteArray(8192)
                while (true) {
                    val bytesRead = input.readAvailable(buffer)
                    if (bytesRead == -1) break
                    output.write(buffer, 0, bytesRead)
                }
            }

            if (file.length() == 0L) {
                file.delete()
                return null
            }

            fileName
        } catch (e: Exception) {
            null
        }
    }

    fun deleteFile(filePath: String): Boolean {
        return try {
            File("$UPLOAD_DIR/$filePath").delete()
        } catch (e: Exception) {
            false
        }
    }

    fun cleanupUploadedFiles(filePaths: UploadedFilePaths) {
        deleteFile("nids/${filePaths.nidFrontPath}")
        deleteFile("nids/${filePaths.nidBackPath}")
        deleteFile("profiles/${filePaths.photoPath}")
    }
}
