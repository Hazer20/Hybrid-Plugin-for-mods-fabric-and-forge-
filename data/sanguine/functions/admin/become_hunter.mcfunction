scoreboard players set @s sg.role 2
scoreboard players set @s sg.thirst 100
scoreboard players set @s sg.form 0
scoreboard players set @s sg.cooldown 0
scoreboard players set @s sg.bite_cd 0
scoreboard players set @s sg.contract 1
scoreboard players set @s sg.bond 0
effect clear @s
tellraw @s {"text":"[Sanguine] Ты теперь охотник. Контракт активирован.","color":"gold"}
