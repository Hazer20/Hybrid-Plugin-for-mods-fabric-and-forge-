# Bonded mortals stay weak and track their sire
execute as @a[scores={sg.role=0,sg.bond=1..}] run effect give @s minecraft:mining_fatigue 2 0 true
execute as @a[scores={sg.role=0,sg.bond=1..}] run effect give @s minecraft:night_vision 2 0 true
