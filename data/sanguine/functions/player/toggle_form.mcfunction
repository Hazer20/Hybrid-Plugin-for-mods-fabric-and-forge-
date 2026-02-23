# Triggered with /trigger sg.trigger set 1
execute if score @s sg.cooldown matches 1.. run tellraw @s {"text":"[Sanguine] Способность на перезарядке.","color":"gray"}
execute if score @s sg.cooldown matches 1.. run return 0

# Safe toggle without double-trigger bug
execute if score @s sg.form matches 0 run scoreboard players set @s sg.tmp 1
execute if score @s sg.form matches 1 run scoreboard players set @s sg.tmp 0
scoreboard players operation @s sg.form = @s sg.tmp

execute if score @s sg.form matches 1 run tellraw @s {"text":"[Sanguine] Форма летучей мыши активирована.","color":"dark_purple"}
execute if score @s sg.form matches 0 run tellraw @s {"text":"[Sanguine] Возврат в гуманоидную форму.","color":"dark_purple"}

scoreboard players set @s sg.cooldown 100
