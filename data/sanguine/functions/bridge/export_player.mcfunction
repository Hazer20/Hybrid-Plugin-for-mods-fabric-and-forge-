# Writes current executor state to storage key 'last_player' for plugin polling
# Plugins can also read scoreboards directly, this is optional convenience.
data modify storage sanguine:bridge last_player.name set from entity @s UUID
execute store result storage sanguine:bridge last_player.role int 1 run scoreboard players get @s sg.role
execute store result storage sanguine:bridge last_player.blood int 1 run scoreboard players get @s sg.blood
execute store result storage sanguine:bridge last_player.thirst int 1 run scoreboard players get @s sg.thirst
execute store result storage sanguine:bridge last_player.rank int 1 run scoreboard players get @s sg.rank
