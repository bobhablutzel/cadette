#! cadette
# Copyright 2026 Bob Hablutzel — Apache 2.0
# https://github.com/bobhablutzel/cadette
#
# First tutorial - creating a simple 60cm x 100cm x 20cm plywood box,
# joined with pocket screws.


# Part 1
create part s1 size 60,20
move s1 to 0,0,98.2

# Part 2
create part s2 size 96.4,20
rotate s2 0,270,0
move s2 to 60,0,1.8

# Part 3
create part s3 size 96.4,20
rotate s3 0,270,0
move s3 to 1.8,0,1.8

# Part 4
create part s4 size 60,20

# Joinery
join s2 to s1 with pocket screws 2
join s3 to s1 with pocket screws 2
join s2 to s4 with pocket screws 2
join s3 to s4 with pocket screws 2
