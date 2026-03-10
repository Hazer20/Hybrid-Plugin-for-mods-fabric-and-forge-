package com.hazerengine.items;

import com.hazerengine.abilities.Ability;
import com.hazerengine.components.ItemComponent;
import org.bukkit.Material;

import java.util.List;

public class CustomWeapon extends CustomItem { public CustomWeapon(String id,String name,Material material,int modelData,List<ItemComponent> components,Ability ability){super(id,name,material,modelData,components,ability);} }
