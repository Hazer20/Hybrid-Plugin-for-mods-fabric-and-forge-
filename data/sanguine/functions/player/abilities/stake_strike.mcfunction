# Hunter anti-vampire strike: /trigger sg.trigger set 5
execute if score @s sg.cooldown matches 1.. run tellraw @s {"text":"[Sanguine] Удар колом на перезарядке.","color":"gray"}
execute if score @s sg.cooldown matches 1.. run return 0

execute as @a[distance=..3,scores={sg.role=1},limit=1,sort=nearest] run damage @s 8 minecraft:player_attack
execute as @a[distance=..3,scores={sg.role=1},limit=1,sort=nearest] run effect give @s minecraft:slowness 4 2 true
scoreboard players set @s sg.cooldown 100
tellraw @s {"text":"[Sanguine] Удар колом выполнен.","color":"gold"}
