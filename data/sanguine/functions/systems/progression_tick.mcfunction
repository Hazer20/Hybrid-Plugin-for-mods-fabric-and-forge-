# Vampire progression by survival and feeding loop
execute as @a[scores={sg.role=1}] if score #dayclock sg.timer matches 0 run scoreboard players add @s sg.xp 1
execute as @a[scores={sg.role=1,sg.xp=120..}] run scoreboard players add @s sg.rank 1
execute as @a[scores={sg.role=1,sg.xp=120..}] run scoreboard players set @s sg.xp 0

# Cap rank
execute as @a[scores={sg.rank=11..}] run scoreboard players set @s sg.rank 10
