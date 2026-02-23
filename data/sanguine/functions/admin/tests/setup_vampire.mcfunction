execute unless entity @s[tag=sg.admin] run tellraw @s {"text":"[Sanguine] Нет прав: нужен tag sg.admin.","color":"red"}
execute unless entity @s[tag=sg.admin] run return 0
function sanguine:ritual/embrace
function sanguine:items/give_vampire_kit
give @s minecraft:ender_pearl 8
tellraw @s {"text":"[Sanguine] Тест-набор вампира + оружие выдан.","color":"dark_red"}
