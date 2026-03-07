scoreboard players add #sky fu_sky_timer 1
execute if score #sky fu_sky_timer matches 0..80 run function fractured_universe:sky/pulse
execute if score #sky fu_sky_timer matches 81.. run scoreboard players set #sky fu_sky_timer 0
execute as @a[scores={fu_phase=2..}] at @s run function fractured_universe:particles/fissure_ring
