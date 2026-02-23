# Horrid thralls
execute as @a at @s run summon minecraft:zombie ~2 ~ ~2 {Tags:["sg.mob","sg.thrall"],CustomName:'{"text":"Кровавый Раб","color":"dark_red"}',CanPickUpLoot:0b,Health:30f,Attributes:[{Name:"minecraft:generic.max_health",Base:30.0},{Name:"minecraft:generic.movement_speed",Base:0.30}]}
execute as @a at @s run summon minecraft:husk ~-3 ~ ~1 {Tags:["sg.mob","sg.thrall"],CustomName:'{"text":"Пожиратель Плоти","color":"red"}',Health:28f}
