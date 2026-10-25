package app.buckykeys.keyboard.profile

import app.buckykeys.keyboard.settings.keyboard.domain.model.OutsideTerminals
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfilesTest {

    private val own = "app.buckykeys.keyboard"

    private fun profile(
        packageName: String?,
        outside: OutsideTerminals,
        forcedFull: Boolean = false,
        canHandOff: Boolean = true,
        handedOffFrom: String? = null,
    ): Profile = profileFor(packageName, outside, own, forcedFull, canHandOff, handedOffFrom)

    @Test
    fun `each terminal app gets the full layout whatever the setting`() {
        listOf(
                "com.termux",
                "org.connectbot",
                "com.sonelli.juicessh",
                "com.server.auditor.ssh.client",
            )
            .forEach { terminal ->
                OutsideTerminals.entries.forEach { setting ->
                    assertEquals("$terminal $setting", Profile.FULL, profile(terminal, setting))
                }
            }
    }

    @Test
    fun `other apps follow the setting`() {
        assertEquals(Profile.COMPACT, profile("com.whatsapp", OutsideTerminals.COMPACT))
        assertEquals(Profile.FULL, profile("com.whatsapp", OutsideTerminals.FULL))
        assertEquals(Profile.HAND_OFF, profile("com.whatsapp", OutsideTerminals.OTHER_KEYBOARD))
    }

    @Test
    fun `bucky's own app always gets the full layout`() {
        OutsideTerminals.entries.forEach { setting ->
            assertEquals(Profile.FULL, profile(own, setting))
        }
    }

    @Test
    fun `a forced full layout wins outside terminals`() {
        assertEquals(
            Profile.FULL,
            profile("com.whatsapp", OutsideTerminals.COMPACT, forcedFull = true),
        )
        assertEquals(
            Profile.FULL,
            profile("com.whatsapp", OutsideTerminals.OTHER_KEYBOARD, forcedFull = true),
        )
    }

    @Test
    fun `hand off falls back to compact when it can't hand off`() {
        assertEquals(
            Profile.COMPACT,
            profile("com.whatsapp", OutsideTerminals.OTHER_KEYBOARD, canHandOff = false),
        )
    }

    @Test
    fun `a null package follows the setting`() {
        assertEquals(Profile.COMPACT, profile(null, OutsideTerminals.COMPACT))
    }

    @Test
    fun `picking bucky again in the app it handed off from keeps compact`() {
        assertEquals(
            Profile.COMPACT,
            profile(
                "com.whatsapp",
                OutsideTerminals.OTHER_KEYBOARD,
                handedOffFrom = "com.whatsapp",
            ),
        )
    }

    @Test
    fun `another app still hands off`() {
        assertEquals(
            Profile.HAND_OFF,
            profile(
                "com.android.chrome",
                OutsideTerminals.OTHER_KEYBOARD,
                handedOffFrom = "com.whatsapp",
            ),
        )
    }
}
