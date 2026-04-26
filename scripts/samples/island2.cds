#! cadette
#
# Example script for creating an island made up of two 
# cabinets with a counter top.
# TODO - update when we have granite as a material

using none


# Create the base cabinets
create base_cabinet b0 w 80 h 60 d 40

# Use a for loop to create three identical cabinets next to each other
for $i = 1 to 3
   create base_cabinet "b$i" w 60 h 60 d 40 right of "b${i-1}"
end for




# Create the counter and rotate it so that it is on the cabinets
create part counter material "granite-1-1/4" size (80+(3*60)+4),45 at 0,60,0
rotate counter 90,0,0
cut counter rect at 85,10 size 50,25

