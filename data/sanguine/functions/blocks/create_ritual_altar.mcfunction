# Build ritual altar structure at executor position
fill ~-1 ~ ~-1 ~1 ~ ~1 minecraft:chiseled_polished_blackstone
setblock ~ ~1 ~ minecraft:redstone_torch
setblock ~1 ~1 ~ minecraft:candle[candles=4,lit=true]
setblock ~-1 ~1 ~ minecraft:candle[candles=4,lit=true]
setblock ~ ~1 ~1 minecraft:candle[candles=4,lit=true]
setblock ~ ~1 ~-1 minecraft:candle[candles=4,lit=true]
setblock ~ ~2 ~ minecraft:lodestone
summon minecraft:armor_stand ~ ~1 ~ {Invisible:1b,NoGravity:1b,Marker:1b,Tags:["sg.ritual_block"],CustomName:'{"text":"Алтарь Крови","color":"dark_red"}'}
tellraw @s {"text":"[Sanguine] Алтарь ритуала создан.","color":"dark_red"}
