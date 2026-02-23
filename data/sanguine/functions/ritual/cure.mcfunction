scoreboard players set @s sg.role 0
scoreboard players set @s sg.blood 100
scoreboard players set @s sg.thirst 100
scoreboard players set @s sg.form 0
scoreboard players set @s sg.cooldown 0
scoreboard players set @s sg.bite_cd 0
scoreboard players set @s sg.infect 0
scoreboard players set @s sg.bond 0
scoreboard players set @s sg.contract 0
effect clear @s
effect give @s minecraft:regeneration 5 0 true
tellraw @s {"text":"[Sanguine] Проклятие снято. Ты снова смертный.","color":"green"}
