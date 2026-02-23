# Night horrors
execute as @a at @s run summon minecraft:stray ~4 ~ ~-2 {Tags:["sg.mob","sg.horror"],CustomName:'{"text":"Костяной Каратель","color":"gray"}'}
execute as @a at @s run summon minecraft:spider ~-4 ~ ~-1 {Tags:["sg.mob","sg.horror"],CustomName:'{"text":"Кровеплёт","color":"dark_purple"}',Health:32f,Attributes:[{Name:"minecraft:generic.max_health",Base:32.0},{Name:"minecraft:generic.attack_damage",Base:6.0}]}
execute as @a at @s run summon minecraft:phantom ~ ~12 ~ {Tags:["sg.mob","sg.horror"],CustomName:'{"text":"Лунный Разоритель","color":"dark_red"}',Size:3}
