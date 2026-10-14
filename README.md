# notices

The notice colours shared by droidtop and Enginehost: one design for warnings
(an info, caution or danger notice, and the danger dialog), and the colour
arithmetic behind it.

- `ColorMath`: WCAG luminance and contrast on ARGB ints, mixing, and the
  smallest move that makes text readable over every surface it is drawn on.
- `NoticeColors.derive(NoticeInputs, highContrast)`: for each tone an ink, an
  opaque plate mixed from the ground toward that ink and a body colour, plus
  the danger fill for the risky button. Caution is a fixed amber and danger the
  theme's danger; both keep their hue and move only in lightness, to reach
  4.5:1 (7:1 with high-contrast text) and to keep danger 20 L* away from
  caution under simulated protanopia, deuteranopia and tritanopia. Info is the
  theme accent unless that reads as caution, then neutral text.
- `ColorVision`: CIELAB lightness and hue under those simulations
  (Machado, Oliveira and Fernandes 2009, severity 1.0).

The design and its reasons are droidtop's SPEC 7k, "Notices"
(Droidtop/droidtop docs/SPEC.md) and Droidtop/tracker#485.

## Use

Products compile the sources into their own modules and pin this repository
as a git submodule (droidtop: `vendor/notices`, compiled by `runtime-common`).
Nothing here depends on Android.

## Test

`gradle test` (JDK 17). The tests are the contrast, hue and separation checks
a product also runs over its own themes (`NoticeAssertions`).

## License

GPL-3.0-or-later.
