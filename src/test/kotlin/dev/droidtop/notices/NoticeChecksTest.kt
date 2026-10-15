package dev.droidtop.notices

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** What [NoticeChecks.unreadable] holds a colour to: the floor wherever one colour could reach it on all its surfaces. */
class NoticeChecksTest {
    @Test
    fun `a set of surfaces one colour reads on is reachable`() {
        assertTrue(NoticeChecks.reachable(listOf(0xFF000000.toInt(), 0xFF1C2027.toInt()), 4.5))
        assertTrue(NoticeChecks.reachable(listOf(0xFF808080.toInt()), 4.5))
    }

    @Test
    fun `a mid-grey ground with a lighter card is not, at 4,5`() {
        // slate-es-de's light scheme: black is 4.2:1 on the ground, white 3.9:1 on the card.
        assertFalse(NoticeChecks.reachable(listOf(0xFF707070.toInt(), 0xFF808080.toInt()), 4.5))
    }

    @Test
    fun `an unreachable set is not reported and a reachable one still is`() {
        val mid = NoticeInputs(
            ground = 0xFF707070.toInt(),
            overlaySurface = 0xFF808080.toInt(),
            card = 0xFF7E7E7E.toInt(),
            onSurface = 0xFF000000.toInt(),
            onSurfaceMuted = 0xFF000000.toInt(),
            accent = 0xFF000000.toInt(),
            danger = 0xFF000000.toInt(),
        )
        assertEquals(emptyList<String>(), NoticeChecks.unreadable(mid, NoticeColors.derive(mid), 4.5))

        val dark = mid.copy(ground = 0xFF000000.toInt(), overlaySurface = 0xFF1C2027.toInt(), card = 0xFF1A1A1A.toInt())
        val unreadableInk = NoticeColors.derive(dark).copy(infoInk = 0xFF202020.toInt())
        assertTrue(NoticeChecks.unreadable(dark, unreadableInk, 4.5).any { it.startsWith("info ink") })
    }
}
