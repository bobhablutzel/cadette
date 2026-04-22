#! cadette
#
# Copyright 2026 Bob Hablutzel — Apache 2.0
# https://github.com/bobhablutzel/cadette
#
# Table-saw crosscut sled — MDF base with front and rear fences butt-joined
# to it. `fence_height` controls the fence thickness along the blade path.

define standard/jigs/crosscut_sled params width(w), length(l), fence_height(fh)
  create part "base" material "mdf-3/4" size $width, $length at 0, 0, 0
  rotate "base" -90, 0, 0
  create part "front-fence" size $width, $fence_height at 0, 0, 0
  rotate "front-fence" 90, 0, 0
  create part "rear-fence" size $width, $fence_height at 0, 0, -$length
  rotate "rear-fence" 90, 0, 0
  # Joinery
  join "base" to "front-fence" with butt
  join "base" to "rear-fence" with butt
end define
