package com.hazerengine.dungeons;

import java.util.Random;

public class DungeonEngine { public int generateRooms(int min,int max){ return new Random().nextInt(min,max+1);} }
