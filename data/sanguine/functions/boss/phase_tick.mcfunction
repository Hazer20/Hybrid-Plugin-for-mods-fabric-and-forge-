# Evolve bloodmoon boss entities by HP thresholds
execute as @e[tag=sg.boss,nbt={Health:..80f}] run tag @s add sg.boss_p2
execute as @e[tag=sg.boss_p2,nbt={Health:..40f}] run tag @s add sg.boss_p3

execute as @e[tag=sg.boss_p2] run effect give @s minecraft:speed 2 1 true
execute as @e[tag=sg.boss_p2] at @s run particle minecraft:soul_fire_flame ~ ~1 ~ 0.5 0.5 0.5 0.01 8 force

execute as @e[tag=sg.boss_p3] run effect give @s minecraft:strength 2 2 true
execute as @e[tag=sg.boss_p3] run effect give @s minecraft:resistance 2 1 true
execute as @e[tag=sg.boss_p3] at @s run particle minecraft:crimson_spore ~ ~1 ~ 0.7 0.5 0.7 0.01 12 force
execute as @e[tag=sg.boss_p3] at @s run playsound minecraft:entity.wither.hurt master @a[distance=..24] ~ ~ ~ 0.5 0.8
