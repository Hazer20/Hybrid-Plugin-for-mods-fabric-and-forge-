execute unless entity @s[tag=sg.admin] run tellraw @s {"text":"[Sanguine] Нет прав: нужен tag sg.admin.","color":"red"}
execute unless entity @s[tag=sg.admin] run return 0

# Fast smoke: run core systems and role ticks
function sanguine:admin/tests/fill_resources
function sanguine:systems/infection_tick
function sanguine:systems/blood_bond_tick
function sanguine:systems/contracts_tick
function sanguine:systems/progression_tick
function sanguine:systems/sanctuary_tick

scoreboard players set @s sg.role 1
function sanguine:player/vampire_tick
scoreboard players set @s sg.role 2
function sanguine:player/hunter_tick
scoreboard players set @s sg.role 0

tellraw @s {"text":"[Sanguine] Smoke test OK: role/system ticks executed.","color":"aqua"}
