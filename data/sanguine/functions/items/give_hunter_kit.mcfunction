execute unless score @s sg.role matches 2 run tellraw @s {"text":"[Sanguine] Набор доступен только охотнику.","color":"gray"}
execute unless score @s sg.role matches 2 run return 0

give @s minecraft:crossbow[custom_name='{"text":"Арбалет Очищения","color":"gold","italic":false}',lore=['{"text":"Создан для охоты на кровь","color":"yellow","italic":false}'],enchantments={levels:{"minecraft:piercing":4,"minecraft:quick_charge":3}}]
give @s minecraft:iron_axe[custom_name='{"text":"Освященный Кол","color":"yellow","italic":false}',enchantments={levels:{"minecraft:smite":5}}]
give @s minecraft:arrow 64
