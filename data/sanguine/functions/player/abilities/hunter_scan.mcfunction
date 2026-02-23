# Hunter active skill: /trigger sg.trigger set 3
execute if score @s sg.cooldown matches 1.. run tellraw @s {"text":"[Sanguine] Сканер на перезарядке.","color":"gray"}
execute if score @s sg.cooldown matches 1.. run return 0

execute as @a[distance=..24,scores={sg.role=1}] run effect give @s minecraft:glowing 5 0 true
tellraw @s {"text":"[Sanguine] Обнаружение вампиров активировано (24 блока).","color":"gold"}
scoreboard players set @s sg.cooldown 120
