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
  # Top, bottom, and back extend $thickness/2 (= default dado/rabbet depth) into
  # the side panels' grooves rather than butting against the inside faces.
  create part "top" size $width - $thickness, $depth at $thickness / 2, $height - $thickness, 0
  rotate "top" -90, 0, 0
  create part "bottom" size $width - $thickness, $depth at $thickness / 2, 0, 0
  rotate "bottom" -90, 0, 0
  create part "back" material "hardboard-5.5mm" size $width - $thickness, $height at $thickness / 2, 0, -$depth
  # Joinery
  join "left-side" to "top" with dado
  join "right-side" to "top" with dado
  join "left-side" to "bottom" with dado
  join "right-side" to "bottom" with dado
  join "left-side" to "back" with rabbet
  join "right-side" to "back" with rabbet
end define
