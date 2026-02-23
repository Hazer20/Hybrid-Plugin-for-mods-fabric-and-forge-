# Vampire ritual: infect nearest mortal
execute unless score @s sg.role matches 1 run tellraw @s {"text":"[Sanguine] Только вампир может заражать.","color":"gray"}
execute unless score @s sg.role matches 1 run return 0

execute as @a[distance=..3,scores={sg.role=0},limit=1,sort=nearest] run scoreboard players set @s sg.infect 1
scoreboard players remove @s sg.blood 10
tellraw @s {"text":"[Sanguine] Жертва заражена кровавой порчей.","color":"dark_red"}
