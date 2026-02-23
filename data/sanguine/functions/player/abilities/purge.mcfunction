# Hunter purge: /trigger sg.trigger set 7
execute if score @s sg.cooldown matches 1.. run tellraw @s {"text":"[Sanguine] Очищение на перезарядке.","color":"gray"}
execute if score @s sg.cooldown matches 1.. run return 0

execute as @a[distance=..12,scores={sg.role=1}] run effect clear @s minecraft:invisibility
execute as @a[distance=..12,scores={sg.role=1}] run effect give @s minecraft:glowing 6 0 true
scoreboard players set @s sg.cooldown 180
tellraw @s {"text":"[Sanguine] Зона очищения активирована.","color":"yellow"}
