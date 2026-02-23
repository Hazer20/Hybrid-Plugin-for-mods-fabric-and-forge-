tag @s add sg.init
scoreboard players set @s sg.role 0
scoreboard players set @s sg.blood 100
scoreboard players set @s sg.thirst 100
scoreboard players set @s sg.cooldown 0
scoreboard players set @s sg.form 0
scoreboard players set @s sg.bite_cd 0
scoreboard players set @s sg.test_cd 0
scoreboard players set @s sg.tmp 0
scoreboard players set @s sg.rank 1
scoreboard players set @s sg.xp 0
scoreboard players set @s sg.infect 0
scoreboard players set @s sg.bond 0
scoreboard players set @s sg.contract 0
scoreboard players set @s sg.exposure 0
scoreboard players set @s sg.surge 0
scoreboard players set @s sg.ritual 0
scoreboard players set @s sg.kills 0
tellraw @s [{"text":"[Sanguine] ","color":"dark_red"},{"text":"Ты смертный. Полный справочник: /function sanguine:admin/help","color":"gray"}]
