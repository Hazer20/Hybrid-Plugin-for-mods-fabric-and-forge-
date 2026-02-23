# Trigger ritual: /trigger sg.trigger set 6
execute unless score @s sg.role matches 1 run tellraw @s {"text":"[Sanguine] Ритуал доступен только вампиру.","color":"gray"}
execute unless score @s sg.role matches 1 run return 0
execute if score @s sg.cooldown matches 1.. run tellraw @s {"text":"[Sanguine] Ритуал на перезарядке.","color":"gray"}
execute if score @s sg.cooldown matches 1.. run return 0

execute as @a[distance=..3,scores={sg.role=0},limit=1,sort=nearest] run scoreboard players set @s sg.bond 1
execute as @a[distance=..3,scores={sg.role=0},limit=1,sort=nearest] run tellraw @s {"text":"[Sanguine] Ты связан кровавой клятвой.","color":"dark_purple"}
scoreboard players remove @s sg.blood 15
scoreboard players set @s sg.cooldown 160
tellraw @s {"text":"[Sanguine] Кровавая клятва наложена.","color":"dark_purple"}
