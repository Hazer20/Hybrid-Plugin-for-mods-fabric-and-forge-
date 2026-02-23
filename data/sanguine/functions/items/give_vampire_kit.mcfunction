# Custom weapons/items via components and lore
execute unless score @s sg.role matches 1 run tellraw @s {"text":"[Sanguine] Набор доступен только вампиру.","color":"gray"}
execute unless score @s sg.role matches 1 run return 0

give @s minecraft:netherite_sword[custom_name='{"text":"Клинок Багровой Клятвы","color":"dark_red","italic":false}',lore=['{"text":"Пьет кровь врагов","color":"red","italic":false}'],enchantments={levels:{"minecraft:sharpness":5,"minecraft:looting":3}}]
give @s minecraft:splash_potion[minecraft:potion_contents={potion:"minecraft:strong_healing"}] 3
give @s minecraft:fire_charge[custom_name='{"text":"Сгусток Крови","color":"dark_red","italic":false}',lore=['{"text":"Материал для запретных ритуалов","color":"gray","italic":false}']] 8
