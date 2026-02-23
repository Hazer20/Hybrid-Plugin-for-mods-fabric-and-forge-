# Optional ward anchor using fake players: #wardx in sg.ward, #wardz in sg.exposure
# When ward enabled (#wardx >= 1), nearby vampires are weakened
execute if score #wardx sg.ward matches 1.. as @a[scores={sg.role=1},distance=..16] run effect give @s minecraft:weakness 2 0 true
execute if score #wardx sg.ward matches 1.. as @a[scores={sg.role=2},distance=..16] run effect give @s minecraft:regeneration 2 0 true
