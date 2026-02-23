# Reveal world lore at rank milestones
execute as @a[scores={sg.rank=3}] unless entity @s[tag=sg.lore1] run function sanguine:lore/reveal_1
execute as @a[scores={sg.rank=6}] unless entity @s[tag=sg.lore2] run function sanguine:lore/reveal_2
execute as @a[scores={sg.rank=9}] unless entity @s[tag=sg.lore3] run function sanguine:lore/reveal_3
