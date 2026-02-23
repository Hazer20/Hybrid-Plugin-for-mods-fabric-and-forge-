# Vampire active skill: /trigger sg.trigger set 2
execute if score @s sg.bite_cd matches 1.. run tellraw @s {"text":"[Sanguine] Укус ещё на перезарядке.","color":"gray"}
execute if score @s sg.bite_cd matches 1.. run return 0

# Bite nearest non-vampire target in 2 blocks
execute as @e[type=!minecraft:player,distance=..2,limit=1,sort=nearest] run damage @s 4 minecraft:magic
execute as @p[distance=..2,scores={sg.role=0..2},limit=1,sort=nearest] unless score @s sg.role matches 1 run damage @s 5 minecraft:magic

# Restore blood to vampire
scoreboard players add @s sg.blood 15
execute if score @s sg.blood matches 101.. run scoreboard players set @s sg.blood 100
scoreboard players set @s sg.bite_cd 80
playsound minecraft:entity.player.attack.strong master @s ~ ~ ~ 1 0.6
tellraw @s {"text":"[Sanguine] Укус использован.","color":"dark_red"}
