package com.example.cinematicscenes.event;
import com.example.cinematicscenes.scene.SceneContext;
public interface SceneEvent { void start(SceneContext context); void update(SceneContext context, float progress); void finish(SceneContext context); default void cancel(SceneContext context) {} }
