# Vampire mobility skill: /trigger sg.trigger set 4
execute if score @s sg.cooldown matches 1.. run tellraw @s {"text":"[Sanguine] Туманная поступь на перезарядке.","color":"gray"}
execute if score @s sg.cooldown matches 1.. run return 0

effect give @s minecraft:invisibility 4 0 true
effect give @s minecraft:speed 4 2 true
effect give @s minecraft:jump_boost 4 1 true
scoreboard players set @s sg.cooldown 140
scoreboard players remove @s sg.blood 8
tellraw @s {"text":"[Sanguine] Туманная поступь активирована.","color":"dark_purple"}
