# Hunters get daytime buffs
execute in minecraft:overworld if time 1000..12000 run effect give @s minecraft:strength 2 0 true
execute in minecraft:overworld if time 1000..12000 run effect give @s minecraft:resistance 2 0 true

# Blood moon pressure (anti-vampire power)
execute if score #bloodmoon sg.bloodmoon matches 1 run effect give @s minecraft:night_vision 3 0 true
execute if score #bloodmoon sg.bloodmoon matches 1 run effect give @s minecraft:speed 2 0 true

# Contract bonus
execute if score @s sg.contract matches 1.. run effect give @s minecraft:hero_of_the_village 2 0 true

# Hunter stamina mechanic
execute if score #dayclock sg.timer matches 0 run scoreboard players remove @s sg.thirst 1
execute if score @s sg.thirst matches ..20 run effect give @s minecraft:weakness 2 0 true
execute if score @s sg.thirst matches ..0 run damage @s 1 minecraft:starve
