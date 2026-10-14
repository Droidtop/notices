package dev.droidtop.notices

/**
 * Colour arithmetic on plain ARGB `Int`s: WCAG luminance and contrast, mixing,
 * and the smallest move that makes text readable. Pure Kotlin with no platform
 * types, so droidtop's theme mapping, its notice colours and Enginehost's
 * tokens share one implementation and one set of tests.
 */
object ColorMath {
    /** WCAG AA for body text. */
    const val TEXT_FLOOR = 4.5

    fun alpha(argb: Int): Int = argb ushr 24 and 0xFF
    fun red(argb: Int): Int = argb shr 16 and 0xFF
    fun green(argb: Int): Int = argb shr 8 and 0xFF
    fun blue(argb: Int): Int = argb and 0xFF

    fun rgb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or (r.coerceIn(0, 255) shl 16) or (g.coerceIn(0, 255) shl 8) or b.coerceIn(0, 255)

    fun withAlpha(argb: Int, alpha: Int): Int = (alpha.coerceIn(0, 255) shl 24) or (argb and 0xFFFFFF)

    private fun channel(v: Int): Double {
        val c = v / 255.0
        return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
    }

    /** WCAG relative luminance of the colour's RGB (alpha ignored). */
    fun luminance(argb: Int): Double =
        0.2126 * channel(red(argb)) + 0.7152 * channel(green(argb)) + 0.0722 * channel(blue(argb))

    /** [fg] composited onto the opaque [bg], as it is on screen; the result is opaque. */
    fun over(fg: Int, bg: Int): Int {
        val a = alpha(fg) / 255.0
        fun mixc(f: Int, b: Int) = Math.round(f * a + b * (1 - a)).toInt()
        return rgb(mixc(red(fg), red(bg)), mixc(green(fg), green(bg)), mixc(blue(fg), blue(bg)))
    }

    /** WCAG contrast ratio of [fg] (composited over [bg] first) against [bg]. */
    fun ratio(fg: Int, bg: Int): Double {
        val l1 = luminance(over(fg, bg))
        val l2 = luminance(bg)
        return (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    }

    /** [a] moved [t] of the way to [b] (0 = a, 1 = b); the result is opaque. */
    fun mix(a: Int, b: Int, t: Double): Int {
        fun m(x: Int, y: Int) = Math.round(x + (y - x) * t).toInt()
        return rgb(m(red(a), red(b)), m(green(a), green(b)), m(blue(a), blue(b)))
    }

    /** HSV saturation, 0 for a grey and 1 for a pure hue. */
    fun saturation(argb: Int): Double {
        val mx = maxOf(red(argb), green(argb), blue(argb))
        val mn = minOf(red(argb), green(argb), blue(argb))
        return if (mx == 0) 0.0 else (mx - mn).toDouble() / mx
    }

    const val WHITE: Int = 0xFFFFFFFF.toInt()
    const val BLACK: Int = 0xFF000000.toInt()

    /** The smallest worst-case ratio of [fg] over every one of [backgrounds]. */
    fun worstRatio(fg: Int, backgrounds: List<Int>): Double =
        backgrounds.minOf { ratio(fg, it) }

    /**
     * [fg], unchanged if it already clears [floor] over every background,
     * otherwise moved toward white or toward black, whichever reaches the
     * floor with the smaller change to the colour, so a theme's amber stays
     * an amber and only gets as light or dark as it must. If neither end
     * can reach the floor (backgrounds on both sides of the text), the end
     * that gets closest. The result is opaque.
     */
    fun ensure(fg: Int, backgrounds: List<Int>, floor: Double): Int {
        val flat = over(fg, backgrounds.first())
        if (worstRatio(flat, backgrounds) >= floor) return flat
        var best: Int? = null
        var bestStep = Int.MAX_VALUE
        var closest = flat
        var closestWorst = worstRatio(flat, backgrounds)
        for (target in intArrayOf(WHITE, BLACK)) {
            for (step in 1..50) {
                val candidate = mix(flat, target, step / 50.0)
                val worst = worstRatio(candidate, backgrounds)
                if (worst >= floor) {
                    if (step < bestStep) {
                        bestStep = step
                        best = candidate
                    }
                    break
                }
                if (worst > closestWorst) {
                    closestWorst = worst
                    closest = candidate
                }
            }
        }
        return best ?: closest
    }

    /**
     * [bg] moved toward [target] until [fg] reads on it at [floor]; for a
     * fill that carries a label (the launch button), where the label is
     * fixed and the fill is what gives way.
     */
    fun ensureFill(bg: Int, fg: Int, floor: Double, target: Int): Int {
        if (ratio(fg, bg) >= floor) return bg
        for (step in 1..50) {
            val candidate = mix(bg, target, step / 50.0)
            if (ratio(fg, candidate) >= floor) return candidate
        }
        return mix(bg, target, 1.0)
    }

    /** Black or white, whichever reads better on [bg]. */
    fun pickOn(bg: Int): Int = if (ratio(WHITE, bg) >= ratio(BLACK, bg)) WHITE else BLACK
}
