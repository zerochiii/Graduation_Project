package com.example.graduationproject.profile

import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProfileAvatarFileManagerTest {
    private lateinit var root: File
    private lateinit var filesDir: File
    private lateinit var cacheDir: File
    private lateinit var manager: AppPrivateProfileAvatarFileManager

    @Before fun setUp() {
        root = Files.createTempDirectory("profile-avatar-test").toFile()
        filesDir = File(root, "files").apply { mkdirs() }
        cacheDir = File(root, "cache").apply { mkdirs() }
        manager = AppPrivateProfileAvatarFileManager(filesDir, cacheDir)
    }

    @After fun tearDown() { root.deleteRecursively() }

    @Test fun gallerySelection_isCopiedThenPersistedInAccountPrivateDirectory() {
        val candidate = manager.stageGalleryImage(11, ByteArrayInputStream(byteArrayOf(1, 2, 3))).getOrThrow()
        assertTrue(File(candidate.path).isFile)
        assertTrue(candidate.path.contains("profile_avatar_temp${File.separator}11"))
        val saved = manager.persistCandidate(11, candidate).getOrThrow()
        assertFalse(File(candidate.path).exists())
        assertTrue(File(saved.path).isFile)
        assertTrue(saved.path.contains("profile_avatars${File.separator}11"))
        assertTrue(manager.isSavedImageAvailable(11, saved))
        assertFalse(manager.isSavedImageAvailable(22, saved))
    }

    @Test fun cancelledCameraCapture_discardsOnlyItsCandidate() {
        val account11 = manager.createCameraCandidate(11).getOrThrow()
        val account22 = manager.createCameraCandidate(22).getOrThrow()
        File(account11.path).writeBytes(byteArrayOf(1))
        File(account22.path).writeBytes(byteArrayOf(2))
        manager.discardCandidate(account11)
        assertFalse(File(account11.path).exists())
        assertTrue(File(account22.path).exists())
    }

    @Test fun candidateCannotBePersistedForAnotherAccount() {
        val candidate = manager.stageGalleryImage(11, ByteArrayInputStream(byteArrayOf(1))).getOrThrow()
        val result = manager.persistCandidate(22, candidate)
        assertTrue(result.isFailure)
        assertTrue(File(candidate.path).exists())
    }

    @Test fun replacingSavedAvatar_canDeletePreviousFileWithoutTouchingAnotherAccount() {
        val first = manager.persistCandidate(11, manager.stageGalleryImage(11, ByteArrayInputStream(byteArrayOf(1))).getOrThrow()).getOrThrow()
        val other = manager.persistCandidate(22, manager.stageGalleryImage(22, ByteArrayInputStream(byteArrayOf(2))).getOrThrow()).getOrThrow()
        manager.deleteSavedImage(first)
        assertFalse(File(first.path).exists())
        assertTrue(File(other.path).exists())
        assertEquals(1L, File(other.path).length())
    }

    @Test fun managerInitialization_removesOnlyStaleCandidates() {
        val stale = manager.createCameraCandidate(11).getOrThrow()
        val recent = manager.createCameraCandidate(11).getOrThrow()
        File(stale.path).writeBytes(byteArrayOf(1))
        File(recent.path).writeBytes(byteArrayOf(2))
        File(stale.path).setLastModified(System.currentTimeMillis() - 25L * 60L * 60L * 1_000L)

        AppPrivateProfileAvatarFileManager(filesDir, cacheDir)

        assertFalse(File(stale.path).exists())
        assertTrue(File(recent.path).exists())
    }
}
