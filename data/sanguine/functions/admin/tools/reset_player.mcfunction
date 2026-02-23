# Full reset of player progress/state
scoreboard players set @s sg.role 0
scoreboard players set @s sg.blood 100
scoreboard players set @s sg.thirst 100
scoreboard players set @s sg.rank 1
scoreboard players set @s sg.xp 0
scoreboard players set @s sg.infect 0
scoreboard players set @s sg.bond 0
scoreboard players set @s sg.contract 0
scoreboard players set @s sg.exposure 0
scoreboard players set @s sg.kills 0
tag @s remove sg.lore1
tag @s remove sg.lore2
tag @s remove sg.lore3
tellraw @s {"text":"[Sanguine] Профиль игрока сброшен.","color":"green"}
