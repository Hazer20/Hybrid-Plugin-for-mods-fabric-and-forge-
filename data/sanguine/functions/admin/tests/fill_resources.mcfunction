execute unless entity @s[tag=sg.admin] run tellraw @s {"text":"[Sanguine] Нет прав: нужен tag sg.admin.","color":"red"}
execute unless entity @s[tag=sg.admin] run return 0
scoreboard players set @s sg.blood 100
scoreboard players set @s sg.thirst 100
scoreboard players set @s sg.cooldown 0
scoreboard players set @s sg.bite_cd 0
tellraw @s {"text":"[Sanguine] Ресурсы восстановлены для теста.","color":"green"}
