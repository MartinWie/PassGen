package de.mw.frontend.pages

import de.mw.models.SharePublicKey
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertTrue

class SharePagesTest {
    @Test
    fun `getSharePage renders one-time view page with correct action URL`() {
        val shareId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val salt = UUID.fromString("00000000-0000-0000-0000-000000000002")

        val html = getSharePage(shareId, salt)

        assertTrue(html.contains("Shared Password"))
        assertTrue(html.contains("One-Time View"))
        assertTrue(html.contains("hx-post=\"/share/$shareId/$salt\""))
        assertTrue(html.contains("View Password"))
    }

    @Test
    fun `getPasswordLoaded renders reveal and modal content`() {
        val html = getPasswordLoaded("s3cr3t-value")

        assertTrue(html.contains("Password Retrieved"))
        assertTrue(html.contains("id=\"reveal-btn\""))
        assertTrue(html.contains("id=\"view_share_modal\""))
        assertTrue(html.contains("id=\"password-field\""))
        assertTrue(html.contains("s3cr3t-value"))
        assertTrue(html.contains("copyToClipboard('password-field');"))
    }

    @Test
    fun `getShareCreateResult renders modal and hidden share link`() {
        val shareId = UUID.fromString("00000000-0000-0000-0000-000000000010")
        val salt = UUID.fromString("00000000-0000-0000-0000-000000000011")

        val html = getShareCreateResult(shareId, salt)

        assertTrue(html.contains("id=\"share_modal\""))
        assertTrue(html.contains("Password Shared"))
        assertTrue(html.contains("id=\"password-share-link\""))
        assertTrue(html.contains("href=\"/share/$shareId/$salt\""))
        assertTrue(html.contains("copyShareUrl();"))
    }

    @Test
    fun `getKeySharePage renders pending state for recipient generation`() {
        val share =
            SharePublicKey(
                id = UUID.fromString("00000000-0000-0000-0000-000000000100"),
                created = LocalDateTime.now(),
                publicKey = null,
                algorithm = "ed25519",
                purpose = "ssh",
                label = "alice@host",
                completedAt = null,
                format = "openssh",
            )

        val html = getKeySharePage(share)

        assertTrue(html.contains("Generate Your Key"))
        assertTrue(html.contains("How This Works"))
        assertTrue(html.contains("id=\"generate-share-key-btn\""))
        assertTrue(html.contains("id=\"share-id\""))
        assertTrue(html.contains("id=\"share-algorithm\""))
        assertTrue(html.contains("value=\"ed25519\""))
        assertTrue(html.contains("alice@host"))
    }

    @Test
    fun `getKeySharePage renders completed state with download and copy actions`() {
        val share =
            SharePublicKey(
                id = UUID.fromString("00000000-0000-0000-0000-000000000101"),
                created = LocalDateTime.now(),
                publicKey = "ssh-ed25519 AAAATESTKEY bob@host",
                algorithm = "ecdsa-p384",
                purpose = "git",
                label = "git-signing",
                completedAt = LocalDateTime.now(),
                format = "pem",
            )

        val html = getKeySharePage(share)

        assertTrue(html.contains("Shared Public Key"))
        assertTrue(html.contains("Git Signing"))
        assertTrue(html.contains("PEM"))
        assertTrue(html.contains("id=\"public-key-display\""))
        assertTrue(html.contains("ssh-ed25519 AAAATESTKEY bob@host"))
        assertTrue(html.contains("downloadSharePublicKey();"))
        assertTrue(html.contains("copyToClipboard('public-key-display');"))
    }

    @Test
    fun `getKeyShareCompletedFragment renders success fragment and copy button`() {
        val share =
            SharePublicKey(
                id = UUID.fromString("00000000-0000-0000-0000-000000000102"),
                created = LocalDateTime.now(),
                publicKey = "ssh-rsa AAAARSAKEY",
                algorithm = "rsa-4096",
                purpose = "ssh",
                label = null,
                completedAt = LocalDateTime.now(),
                format = "openssh",
            )

        val html = getKeyShareCompletedFragment(share)

        assertTrue(html.contains("Key Generated Successfully!"))
        assertTrue(html.contains("Private key downloaded"))
        assertTrue(html.contains("Public Key (now shared)"))
        assertTrue(html.contains("Copy Public Key"))
        assertTrue(html.contains("ssh-rsa AAAARSAKEY"))
    }

    @Test
    fun `getKeyShareCreateResult renders key-share modal with copy link`() {
        val shareId = UUID.fromString("00000000-0000-0000-0000-000000000103")

        val html = getKeyShareCreateResult(shareId)

        assertTrue(html.contains("id=\"key_share_modal\""))
        assertTrue(html.contains("Share Link Created"))
        assertTrue(html.contains("id=\"key-share-link\""))
        assertTrue(html.contains("href=\"/key/share/$shareId\""))
        assertTrue(html.contains("copyKeyShareUrl();"))
    }
}
