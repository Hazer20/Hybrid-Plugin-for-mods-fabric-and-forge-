# Natural blood moon toggles at deep night in overworld
execute in minecraft:overworld if time 18000..22000 run scoreboard players set #bloodmoon sg.bloodmoon 1
execute in minecraft:overworld unless time 18000..22000 run scoreboard players set #bloodmoon sg.bloodmoon 0

# Time in current blood moon
execute if score #bloodmoon sg.bloodmoon matches 1 run scoreboard players add #moon_time sg.moon_time 1
execute unless score #bloodmoon sg.bloodmoon matches 1 run scoreboard players set #moon_time sg.moon_time 0
execute unless score #bloodmoon sg.bloodmoon matches 1 run scoreboard players set #wave sg.wave 0
