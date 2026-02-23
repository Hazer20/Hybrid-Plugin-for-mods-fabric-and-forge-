# Sanguine Legacy datapack bootstrap
scoreboard objectives add sg.role dummy
scoreboard objectives add sg.blood dummy
scoreboard objectives add sg.thirst dummy
scoreboard objectives add sg.cooldown dummy
scoreboard objectives add sg.form dummy
scoreboard objectives add sg.timer dummy
scoreboard objectives add sg.trigger trigger
scoreboard objectives add sg.bite_cd dummy
scoreboard objectives add sg.test_cd dummy
scoreboard objectives add sg.tmp dummy
scoreboard objectives add sg.bloodmoon dummy
scoreboard objectives add sg.rank dummy
scoreboard objectives add sg.xp dummy
scoreboard objectives add sg.infect dummy
scoreboard objectives add sg.bond dummy
scoreboard objectives add sg.ward dummy
scoreboard objectives add sg.contract dummy
scoreboard objectives add sg.exposure dummy
scoreboard objectives add sg.surge dummy
scoreboard objectives add sg.wave dummy
scoreboard objectives add sg.moon_time dummy
scoreboard objectives add sg.ritual dummy
scoreboard objectives add sg.kills dummy

scoreboard players set #dayclock sg.timer 0
scoreboard players set #bloodmoon sg.bloodmoon 0
scoreboard players set #wardx sg.ward 0
scoreboard players set #wardz sg.exposure 0
scoreboard players set #wave sg.wave 0
scoreboard players set #moon_time sg.moon_time 0

tellraw @a [{"text":"[Sanguine] ","color":"dark_red"},{"text":"Ultimate datapack port loaded. /function sanguine:admin/help","color":"red"}]
