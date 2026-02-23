# Init players once
execute as @a[tag=!sg.init] run function sanguine:player/init

# Global timer: 20 ticks = 1 datapack second
scoreboard players add #dayclock sg.timer 1
execute if score #dayclock sg.timer matches 20.. run scoreboard players set #dayclock sg.timer 0

# World mechanics and events
function sanguine:world/bloodmoon_tick
function sanguine:events/bloodmoon_waves
function sanguine:systems/sanctuary_tick
function sanguine:blocks/ritual_block_tick

# System mechanics
function sanguine:systems/infection_tick
function sanguine:systems/blood_bond_tick
function sanguine:systems/contracts_tick
function sanguine:systems/progression_tick

# Tick per-role logic
execute as @a[scores={sg.role=1}] run function sanguine:player/vampire_tick
execute as @a[scores={sg.role=2}] run function sanguine:player/hunter_tick

# Custom mobs AI wrappers
function sanguine:mobs/tick
function sanguine:boss/phase_tick
function sanguine:loot/collect_tick
function sanguine:lore/reveal_tick

# Passive cooldown tick
scoreboard players remove @a[scores={sg.cooldown=1..}] sg.cooldown 1
scoreboard players remove @a[scores={sg.bite_cd=1..}] sg.bite_cd 1
scoreboard players remove @a[scores={sg.test_cd=1..}] sg.test_cd 1

# UI
execute as @a run function sanguine:ui/actionbar

# Plugin bridge + trigger enable
function sanguine:bridge/tick

# Trigger bindings (/trigger sg.trigger set X)
execute as @a[scores={sg.trigger=1,sg.role=1}] run function sanguine:player/toggle_form
execute as @a[scores={sg.trigger=2,sg.role=1}] run function sanguine:player/abilities/bite
execute as @a[scores={sg.trigger=3,sg.role=2}] run function sanguine:player/abilities/hunter_scan
execute as @a[scores={sg.trigger=4,sg.role=1}] run function sanguine:player/abilities/mist_step
execute as @a[scores={sg.trigger=5,sg.role=2}] run function sanguine:player/abilities/stake_strike
execute as @a[scores={sg.trigger=6,sg.role=1}] run function sanguine:ritual/blood_bond
execute as @a[scores={sg.trigger=7,sg.role=2}] run function sanguine:player/abilities/purge
execute as @a[scores={sg.trigger=8}] run function sanguine:rituals/channel_blood
scoreboard players set @a[scores={sg.trigger=1..}] sg.trigger 0
