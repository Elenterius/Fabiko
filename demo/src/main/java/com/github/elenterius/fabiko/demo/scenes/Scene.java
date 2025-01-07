package com.github.elenterius.fabiko.demo.scenes;

import org.joml.Matrix4fc;

public interface Scene {

	void setup();

	void draw(Matrix4fc modelViewProjection);

	String getTitle();

}
