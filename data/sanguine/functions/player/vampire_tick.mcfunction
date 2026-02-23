# Blood drain each datapack second
execute if score #dayclock sg.timer matches 0 run scoreboard players remove @s sg.blood 1

# Blood starvation
execute if score @s sg.blood matches ..0 run damage @s 2 minecraft:magic

# Sunlight exposure and punishment
execute in minecraft:overworld if predicate sanguine:can_see_sky if time 1000..12000 run scoreboard players add @s sg.exposure 1
execute in minecraft:overworld unless time 1000..12000 run scoreboard players set @s sg.exposure 0
execute unless predicate sanguine:can_see_sky run scoreboard players set @s sg.exposure 0
execute if score @s sg.exposure matches 5.. run damage @s 3 minecraft:in_fire

# Night empowerment and blood moon frenzy
execute in minecraft:overworld if time 13000..23000 if score @s sg.blood matches 50.. run effect give @s minecraft:speed 2 0 true
execute in minecraft:overworld if time 13000..23000 if score @s sg.blood matches 50.. run effect give @s minecraft:night_vision 3 0 true
execute if score #bloodmoon sg.bloodmoon matches 1 run effect give @s minecraft:strength 2 0 true

# Rank bonuses
execute if score @s sg.rank matches 3.. run effect give @s minecraft:regeneration 2 0 true
execute if score @s sg.rank matches 5.. run effect give @s minecraft:resistance 2 0 true

# Bat form handling
execute if score @s sg.form matches 1 run effect give @s minecraft:slow_falling 2 0 true
execute if score @s sg.form matches 1 run effect give @s minecraft:jump_boost 2 1 true
execute if score @s sg.form matches 1 run effect give @s minecraft:speed 2 1 true
