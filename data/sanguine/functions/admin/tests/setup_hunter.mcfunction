execute unless entity @s[tag=sg.admin] run tellraw @s {"text":"[Sanguine] Нет прав: нужен tag sg.admin.","color":"red"}
execute unless entity @s[tag=sg.admin] run return 0
function sanguine:admin/become_hunter
function sanguine:items/give_hunter_kit
give @s minecraft:golden_apple 4
tellraw @s {"text":"[Sanguine] Тест-набор охотника + оружие выдан.","color":"gold"}
