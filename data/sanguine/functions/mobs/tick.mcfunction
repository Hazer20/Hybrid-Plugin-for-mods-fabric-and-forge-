# Passive behaviors for custom-tagged mobs
execute as @e[tag=sg.thrall] run effect give @s minecraft:speed 2 0 true
execute as @e[tag=sg.horror] run effect give @s minecraft:strength 2 0 true
execute as @e[tag=sg.tyrant] run effect give @s minecraft:resistance 2 0 true
execute as @e[tag=sg.boss] run effect give @s minecraft:strength 2 1 true

# Scale pressure with average player rank approximation
execute if score #bloodmoon sg.bloodmoon matches 1 as @e[tag=sg.mob] run effect give @s minecraft:regeneration 2 0 true
