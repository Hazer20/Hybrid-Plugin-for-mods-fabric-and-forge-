# Hunter ritual: enable anti-vampire ward globally
execute unless score @s sg.role matches 2 run tellraw @s {"text":"[Sanguine] Только охотник может поднять печать.","color":"gray"}
execute unless score @s sg.role matches 2 run return 0

scoreboard players set #wardx sg.ward 1
playsound minecraft:block.beacon.activate master @a ~ ~ ~ 0.7 1.3
tellraw @a [{"text":"[Sanguine] ","color":"gold"},{"text":"Охотничья печать активирована (радиус 16 блоков вокруг игроков).","color":"yellow"}]
