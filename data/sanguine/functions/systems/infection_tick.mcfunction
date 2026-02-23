# Infection progression for mortals
execute as @a[scores={sg.role=0,sg.infect=1..}] if score #dayclock sg.timer matches 0 run scoreboard players add @s sg.infect 1
execute as @a[scores={sg.role=0,sg.infect=40..79}] run effect give @s minecraft:weakness 2 0 true
execute as @a[scores={sg.role=0,sg.infect=80..119}] run effect give @s minecraft:nausea 2 0 true
execute as @a[scores={sg.role=0,sg.infect=120..}] run function sanguine:ritual/embrace
execute as @a[scores={sg.role=0,sg.infect=120..}] run scoreboard players set @s sg.infect 0
