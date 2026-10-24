package app.buckykeys.keyboard.profile

import app.buckykeys.keyboard.settings.keyboard.domain.model.OutsideTerminals

val TERMINAL_PACKAGES: Set<String> =
    setOf("com.termux", "org.connectbot", "com.sonelli.juicessh", "com.server.auditor.ssh.client")

fun profileFor(
    packageName: String?,
    outside: OutsideTerminals,
    ownPackage: String,
    forcedFull: Boolean,
    canHandOff: Boolean,
): Profile =
    when {
        packageName in TERMINAL_PACKAGES || packageName == ownPackage || forcedFull -> Profile.FULL
        outside == OutsideTerminals.FULL -> Profile.FULL
        outside == OutsideTerminals.OTHER_KEYBOARD && canHandOff -> Profile.HAND_OFF
        else -> Profile.COMPACT
    }
