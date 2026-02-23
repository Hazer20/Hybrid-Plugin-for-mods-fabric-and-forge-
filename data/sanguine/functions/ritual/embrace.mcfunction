# Turn executor into vampire
scoreboard players set @s sg.role 1
scoreboard players set @s sg.blood 100
scoreboard players set @s sg.form 0
scoreboard players set @s sg.cooldown 0
scoreboard players set @s sg.bite_cd 0
scoreboard players set @s sg.contract 0
scoreboard players set @s sg.infect 0
effect clear @s
effect give @s minecraft:regeneration 10 1 true
playsound minecraft:entity.evoker.prepare_wololo master @s ~ ~ ~ 1 0.7
tellraw @s {"text":"[Sanguine] Ритуал завершён. Ты стал вампиром.","color":"dark_red"}
