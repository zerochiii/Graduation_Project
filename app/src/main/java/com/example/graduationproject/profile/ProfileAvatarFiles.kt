package com.example.graduationproject.profile

import java.io.File
import java.io.InputStream

internal interface ProfileAvatarFileManager {
    fun stageGalleryImage(accountId: Int, input: InputStream): Result<AvatarSource.LocalImage>
    fun createCameraCandidate(accountId: Int): Result<AvatarSource.LocalImage>
    fun persistCandidate(accountId: Int, source: AvatarSource.LocalImage): Result<AvatarSource.LocalImage>
    fun isSavedImageAvailable(accountId: Int, source: AvatarSource.LocalImage): Boolean
    fun discardCandidate(source: AvatarSource.LocalImage)
    fun deleteSavedImage(source: AvatarSource.LocalImage)
    fun fileFor(source: AvatarSource.LocalImage): File = File(source.path)
}

internal class AppPrivateProfileAvatarFileManager(
    private val filesDir: File,
    private val cacheDir: File
) : ProfileAvatarFileManager {
    init {
        cleanupStaleCandidates()
    }

    override fun stageGalleryImage(
        accountId: Int,
        input: InputStream
    ): Result<AvatarSource.LocalImage> = runCatching {
        require(accountId > 0) { "Invalid account" }
        val candidate = createCandidateFile(accountId, ".img")
        try {
            input.use { source -> candidate.outputStream().use(source::copyTo) }
            require(candidate.length() > 0L) { "Empty image" }
            AvatarSource.LocalImage(candidate.absolutePath, LocalImageSource.GALLERY)
        } catch (error: Throwable) {
            candidate.delete()
            throw error
        }
    }

    override fun createCameraCandidate(accountId: Int): Result<AvatarSource.LocalImage> = runCatching {
        require(accountId > 0) { "Invalid account" }
        AvatarSource.LocalImage(
            path = createCandidateFile(accountId, ".jpg").absolutePath,
            sourceType = LocalImageSource.CAMERA
        )
    }

    override fun persistCandidate(
        accountId: Int,
        source: AvatarSource.LocalImage
    ): Result<AvatarSource.LocalImage> = runCatching {
        require(accountId > 0) { "Invalid account" }
        val sourceFile = fileFor(source)
        require(sourceFile.isFile && sourceFile.length() > 0L) { "Image is unavailable" }
        require(sourceFile.isInside(candidateDirectory(accountId))) { "Image is not an account candidate" }

        val accountDirectory = savedDirectory(accountId).apply { mkdirs() }
        val extension = if (source.sourceType == LocalImageSource.CAMERA) ".jpg" else ".img"
        val destination = File.createTempFile("avatar_", extension, accountDirectory)
        try {
            sourceFile.inputStream().use { input -> destination.outputStream().use(input::copyTo) }
            require(destination.length() > 0L) { "Saved image is empty" }
            sourceFile.delete()
            source.copy(path = destination.absolutePath)
        } catch (error: Throwable) {
            destination.delete()
            throw error
        }
    }

    override fun isSavedImageAvailable(accountId: Int, source: AvatarSource.LocalImage): Boolean {
        val file = fileFor(source)
        return accountId > 0 && file.isFile && file.length() > 0L && file.isInside(savedDirectory(accountId))
    }

    override fun discardCandidate(source: AvatarSource.LocalImage) {
        val file = fileFor(source)
        if (file.isInside(File(cacheDir, CANDIDATE_ROOT))) file.delete()
    }

    override fun deleteSavedImage(source: AvatarSource.LocalImage) {
        val file = fileFor(source)
        if (file.isInside(File(filesDir, SAVED_ROOT))) file.delete()
    }

    private fun createCandidateFile(accountId: Int, extension: String): File {
        val directory = candidateDirectory(accountId).apply { mkdirs() }
        return File.createTempFile("avatar_", extension, directory)
    }

    private fun candidateDirectory(accountId: Int) = File(cacheDir, "$CANDIDATE_ROOT/$accountId")
    private fun savedDirectory(accountId: Int) = File(filesDir, "$SAVED_ROOT/$accountId")

    private fun File.isInside(parent: File): Boolean {
        val canonicalParent = parent.canonicalFile.path
        val canonicalChild = canonicalFile.path
        return canonicalChild == canonicalParent ||
            canonicalChild.startsWith(canonicalParent + File.separator)
    }

    private fun cleanupStaleCandidates(nowMillis: Long = System.currentTimeMillis()) {
        val root = File(cacheDir, CANDIDATE_ROOT)
        root.walkBottomUp().forEach { file ->
            when {
                file.isFile && nowMillis - file.lastModified() > STALE_CANDIDATE_AGE_MS -> file.delete()
                file.isDirectory && file != root && file.listFiles().isNullOrEmpty() -> file.delete()
            }
        }
    }

    private companion object {
        const val CANDIDATE_ROOT = "profile_avatar_temp"
        const val SAVED_ROOT = "profile_avatars"
        const val STALE_CANDIDATE_AGE_MS = 24L * 60L * 60L * 1_000L
    }
}
