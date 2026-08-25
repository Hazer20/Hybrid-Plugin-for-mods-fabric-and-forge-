package com.example.cinematicscenes.scene;
import com.example.cinematicscenes.event.SceneEvent;
import java.util.*;
public final class Timeline { public record Entry(int start, int duration, SceneEvent event) {} private final List<Entry> events=new ArrayList<>(); public Timeline at(int start,int duration,SceneEvent event){events.add(new Entry(start,duration,event));return this;} public List<Entry> events(){return List.copyOf(events);} }
