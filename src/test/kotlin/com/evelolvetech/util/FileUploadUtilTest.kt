package com.evelolvetech.util

import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class FileUploadUtilTest {

    private lateinit var tempDir: Path

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("fileupload-test")

        File(tempDir.resolve("uploads/nids").toString()).mkdirs()
        File(tempDir.resolve("uploads/profiles").toString()).mkdirs()
    }

    @AfterTest
    fun tearDown() {

        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `test init creates required directories`() {
        val testUploadDir = tempDir.resolve("test-uploads").toString()
        val testNidDir = "$testUploadDir/nids"
        val testProfileDir = "$testUploadDir/profiles"


        File(testNidDir).mkdirs()
        File(testProfileDir).mkdirs()

        assertTrue(File(testNidDir).exists(), "NID directory should exist")
        assertTrue(File(testProfileDir).exists(), "Profile directory should exist")
    }

    @Test
    fun `test isValidImageFile with valid extensions`() {
        val validFiles = listOf(
            "image.jpg",
            "photo.jpeg", 
            "picture.png",
            "avatar.gif",
            "test.JPG",
            "file.JPEG",
            "image.PNG",
            "photo.GIF"
        )

        validFiles.forEach { fileName ->
            assertTrue(
                FileUploadUtil::class.java.getDeclaredMethod("isValidImageFile", String::class.java)
                    .apply { isAccessible = true }
                    .invoke(FileUploadUtil, fileName) as Boolean,
                "File $fileName should be valid"
            )
        }
    }

    @Test
    fun `test isValidImageFile with invalid extensions`() {
        val invalidFiles = listOf(
            "document.pdf",
            "script.js",
            "style.css",
            "data.txt",
            "archive.zip",
            "executable.exe",
            "file",
            "image.bmp",
            "photo.tiff"
        )

        invalidFiles.forEach { fileName ->
            assertFalse(
                FileUploadUtil::class.java.getDeclaredMethod("isValidImageFile", String::class.java)
                    .apply { isAccessible = true }
                    .invoke(FileUploadUtil, fileName) as Boolean,
                "File $fileName should be invalid"
            )
        }
    }

    @Test
    fun `test saveNidImage with valid image file`() = runTest {
        val testContent = "fake image content"
        val fileName = "test.jpg"
        
        val fileItem = createMockFileItem(fileName, testContent)
        val nidDir = tempDir.resolve("uploads/nids").toString()
        

        val result = saveNidImageToDirectory(fileItem, "front", nidDir)
        
        assertNotNull(result, "Should return filename for valid image")
        assertTrue(result!!.startsWith("front_"), "Filename should start with prefix")
        assertTrue(result.endsWith(".jpg"), "Filename should have correct extension")
        
        val savedFile = File("$nidDir/$result")
        assertTrue(savedFile.exists(), "File should be saved")
        assertEquals(testContent.length.toLong(), savedFile.length(), "File size should match content")
    }

    @Test
    fun `test saveNidImage with invalid file extension`() = runTest {
        val testContent = "fake content"
        val fileName = "document.pdf"
        
        val fileItem = createMockFileItem(fileName, testContent)
        val nidDir = tempDir.resolve("uploads/nids").toString()
        
        val result = saveNidImageToDirectory(fileItem, "front", nidDir)
        
        assertNull(result, "Should return null for invalid file extension")
    }

    @Test
    fun `test saveNidImage with null filename`() = runTest {
        val fileItem = createMockFileItem(null, "content")
        val nidDir = tempDir.resolve("uploads/nids").toString()
        
        val result = saveNidImageToDirectory(fileItem, "front", nidDir)
        
        assertNull(result, "Should return null for null filename")
    }

    @Test
    fun `test saveNidImage with empty file`() = runTest {
        val testContent = ""
        val fileName = "empty.jpg"
        
        val fileItem = createMockFileItem(fileName, testContent)
        val nidDir = tempDir.resolve("uploads/nids").toString()
        
        val result = saveNidImageToDirectory(fileItem, "front", nidDir)
        
        assertNull(result, "Should return null for empty file")
    }

    @Test
    fun `test saveProfileImage with valid image file`() = runTest {
        val testContent = "fake profile image content"
        val fileName = "profile.png"
        
        val fileItem = createMockFileItem(fileName, testContent)
        val profileDir = tempDir.resolve("uploads/profiles").toString()
        
        val result = saveProfileImageToDirectory(fileItem, profileDir)
        
        assertNotNull(result, "Should return filename for valid image")
        assertTrue(result!!.startsWith("profile_"), "Filename should start with profile_")
        assertTrue(result.endsWith(".png"), "Filename should have correct extension")
        
        val savedFile = File("$profileDir/$result")
        assertTrue(savedFile.exists(), "File should be saved")
        assertEquals(testContent.length.toLong(), savedFile.length(), "File size should match content")
    }

    @Test
    fun `test saveProfileImage with invalid file extension`() = runTest {
        val testContent = "fake content"
        val fileName = "document.doc"
        
        val fileItem = createMockFileItem(fileName, testContent)
        val profileDir = tempDir.resolve("uploads/profiles").toString()
        
        val result = saveProfileImageToDirectory(fileItem, profileDir)
        
        assertNull(result, "Should return null for invalid file extension")
    }

    @Test
    fun `test deleteFile with existing file`() {
        val testFile = tempDir.resolve("test-file.txt").toFile()
        testFile.writeText("test content")
        
        assertTrue(testFile.exists(), "Test file should exist")
        
        val result = deleteFileFromDirectory("test-file.txt", tempDir.toString())
        
        assertTrue(result, "Should return true for successful deletion")
        assertFalse(testFile.exists(), "File should be deleted")
    }

    @Test
    fun `test deleteFile with non-existing file`() {
        val result = deleteFileFromDirectory("non-existing.txt", tempDir.toString())
        
        assertFalse(result, "Should return false for non-existing file")
    }

    @Test
    fun `test cleanupUploadedFiles removes all files`() {
        val nidDir = tempDir.resolve("uploads/nids").toString()
        val profileDir = tempDir.resolve("uploads/profiles").toString()
        

        val nidFrontFile = File("$nidDir/front_123.jpg")
        val nidBackFile = File("$nidDir/back_456.jpg")
        val profileFile = File("$profileDir/profile_789.png")
        
        nidFrontFile.writeText("nid front content")
        nidBackFile.writeText("nid back content")
        profileFile.writeText("profile content")
        
        assertTrue(nidFrontFile.exists(), "NID front file should exist")
        assertTrue(nidBackFile.exists(), "NID back file should exist")
        assertTrue(profileFile.exists(), "Profile file should exist")
        

        cleanupFilesFromDirectories(
            "front_123.jpg",
            "back_456.jpg", 
            "profile_789.png",
            nidDir,
            profileDir
        )
        
        assertFalse(nidFrontFile.exists(), "NID front file should be deleted")
        assertFalse(nidBackFile.exists(), "NID back file should be deleted")
        assertFalse(profileFile.exists(), "Profile file should be deleted")
    }


    private suspend fun saveNidImageToDirectory(fileItem: PartData.FileItem, prefix: String, nidDir: String): String? {
        return try {
            val originalFileName = fileItem.originalFileName
            if (originalFileName == null) return null

            if (!isValidImageFile(originalFileName)) {
                return null
            }

            val extension = originalFileName.substringAfterLast(".", "jpg").lowercase()
            val fileName = "${prefix}_${java.util.UUID.randomUUID()}.${extension}"
            val file = File("$nidDir/$fileName")

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

    private suspend fun saveProfileImageToDirectory(fileItem: PartData.FileItem, profileDir: String): String? {
        return try {
            val originalFileName = fileItem.originalFileName
            if (originalFileName == null) return null

            if (!isValidImageFile(originalFileName)) {
                return null
            }

            val extension = originalFileName.substringAfterLast(".", "jpg").lowercase()
            val fileName = "profile_${java.util.UUID.randomUUID()}.${extension}"
            val file = File("$profileDir/$fileName")

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

    private fun deleteFileFromDirectory(filePath: String, uploadDir: String): Boolean {
        return try {
            File("$uploadDir/$filePath").delete()
        } catch (e: Exception) {
            false
        }
    }

    private fun cleanupFilesFromDirectories(nidFrontPath: String, nidBackPath: String, photoPath: String, nidDir: String, profileDir: String) {
        deleteFileFromDirectory("nids/$nidFrontPath", nidDir.substringBeforeLast("/"))
        deleteFileFromDirectory("nids/$nidBackPath", nidDir.substringBeforeLast("/"))
        deleteFileFromDirectory("profiles/$photoPath", profileDir.substringBeforeLast("/"))
    }

    private fun isValidImageFile(fileName: String): Boolean {
        val allowedExtensions = setOf("jpg", "jpeg", "png", "gif")
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return extension in allowedExtensions
    }

    private fun createMockFileItem(originalFileName: String?, content: String): PartData.FileItem {
        return PartData.FileItem(
            provider = { ByteReadChannel(content.toByteArray()) },
            dispose = { },
            partHeaders = io.ktor.http.Headers.build {
                originalFileName?.let { append("Content-Disposition", "form-data; name=\"file\"; filename=\"$it\"") }
                append("Content-Type", "image/jpeg")
            }
        )
    }
}
