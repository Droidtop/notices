package dev.droidtop.notices

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The notice colours (Droidtop/tracker#485, final design tests 1 and 2): every tone reads on its own plate and on
 * the surfaces it is drawn over, at 4.5:1 and at 7:1 with high-contrast text; caution and danger keep their hue;
 * danger stays apart from caution under the three simulated dichromacies; info falls back to neutral ink when the
 * accent would read as caution. droidtop runs the same [NoticeChecks] over every theme it bundles.
 */
class NoticeColorsTest {
    /** droidtop's own dark palette. */
    private val dark = NoticeInputs(
        ground = 0xFF000000.toInt(),
        overlaySurface = 0xFF1C2027.toInt(),
        card = 0xFF1A1A1A.toInt(),
        onSurface = 0xFFFFFFFF.toInt(),
        onSurfaceMuted = 0xFF8A93A1.toInt(),
        accent = 0xFF8AB4FF.toInt(),
        danger = 0xFFFFB4AB.toInt(),
    )

    /** A light palette: a light ground and dark text. */
    private val light = NoticeInputs(
        ground = 0xFFF2F2F2.toInt(),
        overlaySurface = 0xFFE4E4E4.toInt(),
        card = 0xFFE0E0E0.toInt(),
        onSurface = 0xFF111111.toInt(),
        onSurfaceMuted = 0xFF555555.toInt(),
        accent = 0xFF1A5FB4.toInt(),
        danger = 0xFFB3261E.toInt(),
    )

    @Test
    fun `a dark and a light palette read at AA`() {
        assertEquals(emptyList<String>(), NoticeChecks.unreadable(dark, NoticeColors.derive(dark), 4.5))
        assertEquals(emptyList<String>(), NoticeChecks.unreadable(light, NoticeColors.derive(light), 4.5))
    }

    @Test
    fun `high-contrast text raises every pair to 7 to 1`() {
        val hc = NoticeColors.HIGH_CONTRAST_FLOOR
        assertEquals(emptyList<String>(), NoticeChecks.unreadable(dark, NoticeColors.derive(dark, highContrast = true), hc))
        assertEquals(emptyList<String>(), NoticeChecks.unreadable(light, NoticeColors.derive(light, highContrast = true), hc))
    }

    @Test
    fun `caution and danger keep their hue`() {
        assertEquals(emptyList<String>(), NoticeChecks.hueMoved(dark, NoticeColors.derive(dark)))
        assertEquals(emptyList<String>(), NoticeChecks.hueMoved(light, NoticeColors.derive(light)))
    }

    @Test
    fun `danger stays apart from caution under every simulated dichromacy`() {
        listOf(dark, light).forEach { inputs ->
            val n = NoticeColors.derive(inputs)
            assertTrue(n.separated)
            assertEquals(emptyList<String>(), NoticeChecks.tooClose(n))
        }
    }

    @Test
    fun `an amber accent gives info neutral ink, a blue one keeps the accent`() {
        val amber = dark.copy(accent = 0xFFFFB84D.toInt())
        val notice = NoticeColors.derive(amber)
        assertTrue(notice.infoNeutral)
        assertEquals(emptyList<String>(), NoticeChecks.unreadable(amber, notice, 4.5))
        assertFalse(NoticeColors.derive(dark).infoNeutral)
    }

    @Test
    fun `caution is the fixed amber on a dark ground`() {
        assertEquals(NoticeColors.CAUTION_DARK, NoticeColors.derive(dark).cautionInk)
    }

    @Test
    fun `hsl round-trips a colour`() {
        listOf(0xFFFFC56B.toInt(), 0xFF8A5A00.toInt(), 0xFFFFB4AB.toInt(), 0xFF1A5FB4.toInt()).forEach { c ->
            val hsl = Hsl.toHsl(c)
            val back = Hsl.fromHsl(hsl[0], hsl[1], hsl[2])
            listOf(ColorMath::red, ColorMath::green, ColorMath::blue).forEach { channel ->
                assertTrue(Math.abs(channel(c) - channel(back)) <= 1)
            }
        }
    }

    @Test
    fun `contrast is WCAG's`() {
        assertEquals(21.0, ColorMath.ratio(ColorMath.WHITE, ColorMath.BLACK), 0.01)
        assertEquals(1.0, ColorMath.ratio(0xFF777777.toInt(), 0xFF777777.toInt()), 0.0001)
    }
}
