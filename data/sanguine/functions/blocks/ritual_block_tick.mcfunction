# Visual pulse for ritual blocks and nearby effects
execute as @e[tag=sg.ritual_block] at @s run particle minecraft:crimson_spore ~ ~1 ~ 0.4 0.2 0.4 0.01 6 force
execute as @e[tag=sg.ritual_block] at @s if entity @a[distance=..3,scores={sg.role=1}] run effect give @a[distance=..3,scores={sg.role=1}] minecraft:regeneration 2 0 true
execute as @e[tag=sg.ritual_block] at @s if entity @a[distance=..3,scores={sg.role=2}] run effect give @a[distance=..3,scores={sg.role=2}] minecraft:strength 2 0 true
