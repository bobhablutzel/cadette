#! cadette
# Open shelving unit with an integer number of evenly-spaced shelves between
# the top and bottom panels. Defaults to 3 shelves; caller can override.
define standard/cabinets/adjustable_shelf_unit params width, height, depth, shelf_count=3
  create part "left-side"  size $depth, $height at 0, 0, 0 grain vertical
  create part "right-side" size $depth, $height at $width - $thickness, 0, 0 grain vertical
  create part "bottom"     size $width - 2 * $thickness, $depth at $thickness, 0, 0
  create part "top"        size $width - 2 * $thickness, $depth at $thickness, $height - $thickness, 0
  create part "back"       material "hardboard-5.5mm" size $width, $height at 0, 0, -$depth
  # Shelves are evenly spaced — $i/($shelf_count+1) places them between the
  # bottom and top panels without touching either. A 10mm back-offset keeps
  # them clear of the hardboard back.
  for $i = 1 to $shelf_count
    create part "shelf_$i" size $width - 2 * $thickness, $depth - 10 * $mm at $thickness, $i * $height / ($shelf_count + 1), 0
  end for
end define
