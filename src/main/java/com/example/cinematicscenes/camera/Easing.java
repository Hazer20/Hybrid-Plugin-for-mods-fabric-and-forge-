package com.example.cinematicscenes.camera;
public enum Easing { LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT, SMOOTHSTEP, CUBIC, EASE_IN_OUT_CUBIC;
 public float apply(float t) { t=Math.max(0,Math.min(1,t)); return switch(this) { case LINEAR->t; case EASE_IN->t*t; case EASE_OUT->1-(1-t)*(1-t); case EASE_IN_OUT->t<.5f?2*t*t:1-(float)Math.pow(-2*t+2,2)/2; case SMOOTHSTEP->t*t*(3-2*t); case CUBIC->t*t*t; case EASE_IN_OUT_CUBIC->t<.5f?4*t*t*t:1-(float)Math.pow(-2*t+2,3)/2; }; } }
