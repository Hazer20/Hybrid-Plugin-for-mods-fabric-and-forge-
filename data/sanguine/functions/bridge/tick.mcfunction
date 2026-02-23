# Enable trigger objective for all players so /trigger always works
scoreboard players enable @a sg.trigger

# Export world state for plugin bridge consumers
execute store result storage sanguine:bridge world.bloodmoon int 1 run scoreboard players get #bloodmoon sg.bloodmoon
execute store result storage sanguine:bridge world.wave int 1 run scoreboard players get #wave sg.wave
execute store result storage sanguine:bridge world.moon_time int 1 run scoreboard players get #moon_time sg.moon_time

# Export nearby player snapshots (plugin can read scoreboard directly too)
execute as @a run function sanguine:bridge/export_player

# Import plugin control flags from storage (if plugin writes them)
function sanguine:bridge/import_flags
