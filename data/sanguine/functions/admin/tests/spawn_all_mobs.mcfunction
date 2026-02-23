execute unless entity @s[tag=sg.admin] run tellraw @s {"text":"[Sanguine] Нет прав: нужен tag sg.admin.","color":"red"}
execute unless entity @s[tag=sg.admin] run return 0
function sanguine:mobs/spawn_wave_1
function sanguine:mobs/spawn_wave_2
function sanguine:mobs/spawn_wave_3
tellraw @s {"text":"[Sanguine] Все кастомные монстры заспавнены для теста.","color":"red"}
