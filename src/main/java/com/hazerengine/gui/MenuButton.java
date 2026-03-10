package com.hazerengine.gui;

import org.bukkit.inventory.ItemStack;

public record MenuButton(int slot, ItemStack icon, Runnable clickHandler) { }
