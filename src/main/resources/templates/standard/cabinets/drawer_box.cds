#! cadette
#
# Copyright 2026 Bob Hablutzel — Apache 2.0
# https://github.com/bobhablutzel/cadette
#
# Standard drawer box — four dadoed sides with a rabbeted 1/4" plywood bottom.

define standard/cabinets/drawer_box params width(w), height(h), depth(d)
  create part "left-side" size $depth, $height at 0, 0, 0 grain vertical
  rotate "left-side" 0, 90, 0
  create part "right-side" size $depth, $height at $width - $thickness, 0, 0 grain vertical
  rotate "right-side" 0, 90, 0
  create part "front" size $width - 2 * $thickness, $height at $thickness, 0, -$thickness grain vertical
  create part "back" size $width - 2 * $thickness, $height at $thickness, 0, -$depth grain vertical
  create part "bottom" material "plywood-1/4" size $width, $depth at 0, 0, 0
  rotate "bottom" -90, 0, 0
  # Joinery
  join "left-side" to "front" with dado
  join "right-side" to "front" with dado
  join "left-side" to "back" with dado
  join "right-side" to "back" with dado
  join "left-side" to "bottom" with rabbet
  join "right-side" to "bottom" with rabbet
end define
