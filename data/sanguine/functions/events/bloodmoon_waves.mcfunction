# Spawn escalating waves during blood moon
execute unless score #bloodmoon sg.bloodmoon matches 1 run return 0

execute if score #moon_time sg.moon_time matches 1 run tellraw @a [{"text":"[Sanguine] ","color":"dark_red"},{"text":"Кровавая луна взошла. Мир изменился.","color":"red"}]

# Wave 1
execute if score #moon_time sg.moon_time matches 5 if score #wave sg.wave matches ..0 run function sanguine:mobs/spawn_wave_1
execute if score #moon_time sg.moon_time matches 5 if score #wave sg.wave matches ..0 run scoreboard players set #wave sg.wave 1

# Wave 2
execute if score #moon_time sg.moon_time matches 20 if score #wave sg.wave matches ..1 run function sanguine:mobs/spawn_wave_2
execute if score #moon_time sg.moon_time matches 20 if score #wave sg.wave matches ..1 run scoreboard players set #wave sg.wave 2

# Wave 3
execute if score #moon_time sg.moon_time matches 40 if score #wave sg.wave matches ..2 run function sanguine:mobs/spawn_wave_3
execute if score #moon_time sg.moon_time matches 40 if score #wave sg.wave matches ..2 run scoreboard players set #wave sg.wave 3
