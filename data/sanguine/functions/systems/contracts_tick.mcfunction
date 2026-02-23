# Hunters with contracts accumulate points for passive rank
execute as @a[scores={sg.role=2,sg.contract=1..}] if score #dayclock sg.timer matches 0 run scoreboard players add @s sg.xp 1
execute as @a[scores={sg.role=2,sg.contract=1..,sg.xp=60..}] run scoreboard players add @s sg.rank 1
execute as @a[scores={sg.role=2,sg.contract=1..,sg.xp=60..}] run scoreboard players set @s sg.xp 0
