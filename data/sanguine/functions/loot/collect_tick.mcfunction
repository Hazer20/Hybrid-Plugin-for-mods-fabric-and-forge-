# Track kills on custom mobs for progression/lore drops
execute as @a[scores={sg.role=1..2}] if entity @e[tag=sg.mob,distance=..10,nbt={Health:0.0f}] run scoreboard players add @s sg.kills 1

# Reward every 10 kills with ritual materials
execute as @a[scores={sg.kills=10..}] run give @s minecraft:fire_charge[custom_name='{"text":"Кровавый Осколок","color":"dark_red","italic":false}'] 1
execute as @a[scores={sg.kills=10..}] run give @s minecraft:amethyst_shard[custom_name='{"text":"Сгусток Лунной Пыли","color":"light_purple","italic":false}'] 1
execute as @a[scores={sg.kills=10..}] run scoreboard players set @s sg.kills 0
