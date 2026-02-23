# Universal ritual trigger: /trigger sg.trigger set 8
execute if score @s sg.cooldown matches 1.. run tellraw @s {"text":"[Sanguine] Канал крови на перезарядке.","color":"gray"}
execute if score @s sg.cooldown matches 1.. run return 0

execute if score @s sg.role matches 1 run scoreboard players remove @s sg.blood 20
execute if score @s sg.role matches 1 run effect give @s minecraft:strength 8 1 true
execute if score @s sg.role matches 1 run effect give @s minecraft:regeneration 8 1 true
execute if score @s sg.role matches 1 run tellraw @s {"text":"[Sanguine] Кровавый канал усиляет твою плоть.","color":"dark_red"}

execute if score @s sg.role matches 2 run scoreboard players remove @s sg.thirst 15
execute if score @s sg.role matches 2 run effect give @s minecraft:resistance 8 1 true
execute if score @s sg.role matches 2 run effect give @s minecraft:speed 8 1 true
execute if score @s sg.role matches 2 run tellraw @s {"text":"[Sanguine] Охотничий канал укрепляет твою волю.","color":"gold"}

scoreboard players set @s sg.cooldown 220
