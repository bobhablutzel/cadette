#! cadette
#
# Copyright 2026 Bob Hablutzel — Apache 2.0
# https://github.com/bobhablutzel/cadette
#
# Standard wall cabinet — enclosed top and bottom, sides dadoed to both,
# hardboard back rabbeted into the sides.

define standard/cabinets/wall_cabinet params width(w), height(h), depth(d)
  create part "left-side" size $depth, $height at 0, 0, 0 grain vertical
  rotate "left-side" 0, 90, 0
  create part "right-side" size $depth, $height at $width - $thickness, 0, 0 grain vertical
  rotate "right-side" 0, 90, 0
  create part "top" size $width - 2 * $thickness, $depth at $thickness, $height - $thickness, 0
  rotate "top" -90, 0, 0
  create part "bottom" size $width - 2 * $thickness, $depth at $thickness, 0, 0
  rotate "bottom" -90, 0, 0
  create part "back" material "hardboard-5.5mm" size $width, $height at 0, 0, -$depth
  # Joinery
  join "left-side" to "top" with dado
  join "right-side" to "top" with dado
  join "left-side" to "bottom" with dado
  join "right-side" to "bottom" with dado
  join "left-side" to "back" with rabbet
  join "right-side" to "back" with rabbet
end define
