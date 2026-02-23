execute unless entity @s[tag=sg.admin] run tellraw @s {"text":"[Sanguine] Нет прав: нужен tag sg.admin.","color":"red"}
execute unless entity @s[tag=sg.admin] run return 0
function sanguine:ritual/start_bloodmoon
tellraw @s {"text":"[Sanguine] Bloodmoon форсирован для теста.","color":"dark_red"}
