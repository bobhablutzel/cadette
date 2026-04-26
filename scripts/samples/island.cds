#! cadette
#
# Example script for creating an island made up of two 
# cabinets with a counter top.

using none

# Create the two base cabinets
create base_cabinet b w 80 h 60 d 40
create base_cabinet b2 w 60 h 60 d 40 right of b

# Create the counter and rotate it so that it is on the cabinets
create part counter material "granite-1-1/4" size 145,45 at 0,60,0
rotate counter 90,0,0


