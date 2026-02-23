# Cleanup all datapack spawned mobs/markers
kill @e[tag=sg.mob]
kill @e[tag=sg.ritual_block]
tellraw @s {"text":"[Sanguine] Кастомные сущности очищены.","color":"green"}
