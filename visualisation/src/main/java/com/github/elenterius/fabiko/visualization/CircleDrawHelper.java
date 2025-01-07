/// The MIT License (MIT)
///
/// Copyright (c) 2016-2020 Alastair Lansley / Federation University Australia
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.

package com.github.elenterius.fabiko.visualization;

import com.github.elenterius.fabiko.math.JomlMath;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/// Refactored copy from the Caliko library.
///
/// Original Author: Al Lansley
///
/// Source: <a href="https://github.com/FedUni/caliko/blob/master/caliko-visualisation/src/main/java/au/edu/federation/caliko/visualisation/Circle3D.java">Circle3D</a>
public class CircleDrawHelper {

	private static final String VERTEX_SHADER_SOURCE = """
			#version 330
			in vec4 vertexLocation; // Incoming vertex attribute
			out vec4 fragColour;    // Outgoing colour value
			uniform mat4 mvpMatrix; // ModelViewProjection matrix
			void main(void) {
				gl_Position = mvpMatrix * vertexLocation; // Project our geometry
			}
			""";

	private static final String FRAGMENT_SHADER_SOURCE = """
			#version 330
			out vec4 vOutputColour; // Outgoing colour value
			uniform vec4 fragColour;
			void main() {
				vOutputColour = fragColour;
			}
			""";


	private static final int NUM_VERTICES = 40;
	private static final int VERTEX_COMPONENTS = 3; // x, y, z

	private static final ShaderProgram SHADER_PROGRAM;

	private static final FloatBuffer MVP_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer VERTEX_BUFFER = BufferUtils.createFloatBuffer(NUM_VERTICES * VERTEX_COMPONENTS);
	private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(4);
	private static final FloatBuffer LINE_WIDTH_BUFFER = BufferUtils.createFloatBuffer(1);

	private static final float[] circleData = new float[NUM_VERTICES * VERTEX_COMPONENTS];

	/// Vertex Array Object (VAO) id.
	private static final int VAO_ID;

	/// Vertex Buffer Object (VBO) id.
	private static final int VBO_ID;

	private static final Vector3f VECTOR3f = new Vector3f();

	static {
		SHADER_PROGRAM = new ShaderProgram();
		SHADER_PROGRAM.initFromStrings(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
		SHADER_PROGRAM.addAttribute("vertexLocation");
		SHADER_PROGRAM.addUniform("mvpMatrix");
		SHADER_PROGRAM.addUniform("fragColour");

		// Get an id for the Vertex Array Object (VAO) and bind to it
		VAO_ID = glGenVertexArrays();
		glBindVertexArray(VAO_ID);

		// Generate an id for our Vertex Buffer Object (VBO) and bind to it
		VBO_ID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO_ID);

		// Place the location data into the VBO...
		glBufferData(GL_ARRAY_BUFFER, VERTEX_BUFFER, GL_DYNAMIC_DRAW);

		// ...and specify the data format.
		glVertexAttribPointer(SHADER_PROGRAM.attribute("vertexLocation"),  // Vertex location attribute index
				VERTEX_COMPONENTS,  // Number of normal components per vertex
				GL_FLOAT,  // Data type
				false,  // Normalised?
				0,  // Stride
				0); // Offset

		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attributes
		glEnableVertexAttribArray(SHADER_PROGRAM.attribute("vertexLocation"));

		// Unbind our from our VAO, saving all settings
		glBindVertexArray(0);
	}

	public CircleDrawHelper() {
	}

	/// Draw a circle in 3D space.
	///
	/// @param radius             the radius of the circle in pixels
	/// @param location           the location to draw the circle
	/// @param axis               the axis the circle will be perpendicular to
	/// @param color              the colour with which to draw the circle
	/// @param lineWidth          the width of the lines comprising the circle in pixels
	/// @param modeViewProjection the ModelViewProjection matrix with which to draw the circle
	public void draw(Vector3fc location, Vector3fc axis, float radius, Color4fc color, float lineWidth, Matrix4fc modeViewProjection) {

		for (int i = 0; i < NUM_VERTICES; i++) {
			// Create our circle in the plane perpendicular to the axis provided
			float angle = i * (Math.PI_TIMES_2_f / NUM_VERTICES);
			Vector3f perpendicularAxis = JomlMath.perpendicularQuick(axis, VECTOR3f);
			Vector3f point = perpendicularAxis
					.mul(radius) // get point
					.rotateAxis(angle, axis.x(), axis.y(), axis.z()) //rotate point about the axis
					.add(location); //translate to location

			circleData[(i * VERTEX_COMPONENTS)] = point.x();
			circleData[(i * VERTEX_COMPONENTS) + 1] = point.y();
			circleData[(i * VERTEX_COMPONENTS) + 2] = point.z();
		}

		VERTEX_BUFFER.put(circleData);
		VERTEX_BUFFER.flip();

		SHADER_PROGRAM.use();
		glBindVertexArray(VAO_ID); // bind to our VAO

		// Bind to the vertex buffer object (VBO) and place the new data into it
		glBindBuffer(GL_ARRAY_BUFFER, VBO_ID);
		glBufferData(GL_ARRAY_BUFFER, VERTEX_BUFFER, GL_DYNAMIC_DRAW);

		// Provide the projection matrix and colour uniforms to our shader
		glUniformMatrix4fv(SHADER_PROGRAM.getUniformLocation("mvpMatrix"), false, modeViewProjection.get(MVP_MATRIX_BUFFER));
		glUniform4fv(SHADER_PROGRAM.getUniformLocation("fragColour"), color.get(COLOR_BUFFER));

		glGetFloatv(GL_LINE_WIDTH, LINE_WIDTH_BUFFER); // store current GL_LINE_WIDTH

		glLineWidth(lineWidth);
		glDrawArrays(GL_LINE_LOOP, 0, NUM_VERTICES); // draw the circle as a line loop

		glLineWidth(LINE_WIDTH_BUFFER.get(0)); // restore previous GL_LINE_WIDTH

		// unbind from VBO & VAO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		SHADER_PROGRAM.disable();
	}

}
