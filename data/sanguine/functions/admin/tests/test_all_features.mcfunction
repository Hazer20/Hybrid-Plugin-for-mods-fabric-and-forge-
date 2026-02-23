execute unless entity @s[tag=sg.admin] run tellraw @s {"text":"[Sanguine] Нет прав: нужен tag sg.admin.","color":"red"}
execute unless entity @s[tag=sg.admin] run return 0

# Stage 1: vampire suite
function sanguine:admin/tests/setup_vampire
function sanguine:ritual/feed
function sanguine:player/toggle_form
function sanguine:player/abilities/bite
function sanguine:player/abilities/mist_step
function sanguine:ritual/infect_target
function sanguine:ritual/blood_bond
function sanguine:rituals/channel_blood

# Stage 2: hunter suite
function sanguine:admin/become_hunter
function sanguine:player/abilities/hunter_scan
function sanguine:player/abilities/stake_strike
function sanguine:player/abilities/purge
function sanguine:ritual/create_ward
function sanguine:rituals/channel_blood

# Stage 3: world, mobs, bosses, loot, lore
function sanguine:blocks/create_ritual_altar
function sanguine:admin/tests/force_bloodmoon
function sanguine:admin/tests/spawn_all_mobs
function sanguine:boss/phase_tick
function sanguine:loot/collect_tick
function sanguine:lore/reveal_tick
function sanguine:admin/tests/run_smoke

# Stage 4: admin tools sanity
function sanguine:admin/tools/reset_player
function sanguine:admin/tools/cleanup_entities

tellraw @s {"text":"[Sanguine] Полный test_all_features выполнен: события/мобы/ритуалы/лут/лор/инструменты проверены.","color":"green"}
