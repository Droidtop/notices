package dev.droidtop.notices

/**
 * The checks every theme's notice colours must pass, as plain lists of problems so any product's tests (droidtop over
 * the themes it bundles, Enginehost over its own palettes) run exactly these: every pair reads at the floor, caution
 * and danger keep their hue, and danger stays apart from caution under the three simulated dichromacies.
 */
object NoticeChecks {
    /** Every text-on-surface pair below [floor]. */
    fun unreadable(c: NoticeInputs, n: NoticeColors, floor: Double): List<String> = buildList {
        val m = ColorMath
        val surfaces = listOf(c.ground, c.overlaySurface, c.card)
        fun check(name: String, fg: Int, bgs: List<Int>) = bgs.forEach { bg ->
            val r = m.ratio(fg, bg)
            if (r < floor - 0.01) add("$name is $r:1 on ${Integer.toHexString(bg)} (floor $floor)")
        }
        check("info ink", n.infoInk, surfaces + n.infoPlate)
        check("caution ink", n.cautionInk, surfaces + n.cautionPlate)
        check("danger ink", n.dangerInk, surfaces + n.dangerPlate)
        check("on info", n.onInfo, listOf(n.infoPlate))
        check("on caution", n.onCaution, listOf(n.cautionPlate))
        check("on danger", n.onDanger, listOf(n.dangerPlate))
        check("on danger fill", n.onDangerFill, listOf(n.dangerFill))
        check("muted", n.muted, listOf(n.infoPlate, n.cautionPlate, n.dangerPlate))
        // The plates are opaque: what is measured is what is drawn.
        listOf(n.infoPlate, n.cautionPlate, n.dangerPlate).forEach { if (m.alpha(it) != 0xFF) add("a plate is not opaque") }
    }

    /** Caution or danger whose hue moved (more than [tolerance] degrees, where the lightness leaves a hue to keep). */
    fun hueMoved(c: NoticeInputs, n: NoticeColors, tolerance: Double = 3.0): List<String> = buildList {
        fun hue(x: Int) = Hsl.toHsl(x)[0]
        fun apart(a: Double, b: Double) = Math.abs(a - b).let { if (it > 180) 360 - it else it }
        fun keepsHue(x: Int) = Hsl.toHsl(x)[2] in 0.04..0.96
        val cautionBase = if (c.isLight) NoticeColors.CAUTION_LIGHT else NoticeColors.CAUTION_DARK
        if (keepsHue(n.cautionInk) && apart(hue(n.cautionInk), hue(cautionBase)) > tolerance) add("caution hue moved")
        if (Hsl.toHsl(c.danger)[1] > 0.1 && keepsHue(n.dangerInk) && apart(hue(n.dangerInk), hue(c.danger)) > tolerance) add("danger hue moved")
    }

    /** Simulations under which danger and caution are closer than [NoticeColors.NEAR_LSTAR]. */
    fun tooClose(n: NoticeColors): List<String> = ColorVision.SIMULATIONS.mapNotNull { sim ->
        val apart = ColorVision.lightnessApart(n.dangerInk, n.cautionInk, sim)
        if (apart < NoticeColors.NEAR_LSTAR - 0.01) "danger and caution are $apart L* apart under $sim" else null
    }
}
