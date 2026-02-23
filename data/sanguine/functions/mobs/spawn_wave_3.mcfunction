# Blood tyrants and mini-boss
execute as @a at @s run summon minecraft:vindicator ~5 ~ ~5 {Tags:["sg.mob","sg.tyrant"],CustomName:'{"text":"Тиран Крови","color":"dark_red"}',Health:60f,Attributes:[{Name:"minecraft:generic.max_health",Base:60.0},{Name:"minecraft:generic.attack_damage",Base:10.0},{Name:"minecraft:generic.movement_speed",Base:0.35}]}
execute as @a at @s run summon minecraft:ravager ~-5 ~ ~-5 {Tags:["sg.mob","sg.boss"],CustomName:'{"text":"Багровый Бегемот","color":"red"}',Health:120f,Attributes:[{Name:"minecraft:generic.max_health",Base:120.0},{Name:"minecraft:generic.attack_damage",Base:14.0}]}
playsound minecraft:entity.wither.spawn master @a ~ ~ ~ 0.7 1.3
