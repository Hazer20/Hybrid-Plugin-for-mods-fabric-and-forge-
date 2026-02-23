execute if score @s sg.role matches 1 run scoreboard players add @s sg.blood 25
execute if score @s sg.role matches 1 if score @s sg.blood matches 101.. run scoreboard players set @s sg.blood 100
execute if score @s sg.role matches 1 run tellraw @s {"text":"[Sanguine] Кровь восстановлена.","color":"red"}
execute unless score @s sg.role matches 1 run tellraw @s {"text":"[Sanguine] Только вампир может использовать кормление.","color":"gray"}
