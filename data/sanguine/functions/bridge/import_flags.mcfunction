# If plugin sets storage flags, datapack consumes them here
# force_bloodmoon: 1 -> force event on
execute if data storage sanguine:bridge flags{force_bloodmoon:1} run scoreboard players set #bloodmoon sg.bloodmoon 1

# global_cleanup: 1 -> cleanup custom entities then reset flag
execute if data storage sanguine:bridge flags{global_cleanup:1} run function sanguine:admin/tools/cleanup_entities
execute if data storage sanguine:bridge flags{global_cleanup:1} run data merge storage sanguine:bridge {flags:{global_cleanup:0}}
