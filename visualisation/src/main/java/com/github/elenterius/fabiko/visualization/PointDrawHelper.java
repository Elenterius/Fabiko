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

import org.joml.Matrix4fc;
import org.joml.Vector3f;
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
/// Source: <a href="https://github.com/FedUni/caliko/blob/master/caliko-visualisation/src/main/java/au/edu/federation/caliko/visualisation/Point3D.java">Point3D</a>
public class PointDrawHelper {

	private static final String VERTEX_SHADER_GLSL_SOURCE = """
			#version 330
			in vec3 vertexLocation;                                 // Incoming vertex attribute
			in vec4 vertexColour;                                   // Incoming colour value
			flat out vec4 fragColour;                               // Outgoing non-interpolated colour
			uniform mat4 mvpMatrix;                                 // ModelViewProjectionMatrix
			void main(void) {
				fragColour = vertexColour;                           // Pass through colour
				gl_Position = mvpMatrix * vec4(vertexLocation, 1.0); // Project our geometry
			}
			""";

	private static final String FRAGMENT_SHADER_GLSL_SOURCE = """
			#version 330
			flat in vec4 fragColour; // Incoming colour from vertex shader
			out vec4 vOutputColour;  // Outgoing colour value
			void main() {
				vOutputColour = fragColour;
			}
			""";

	private static final int NUM_VERTICES = 1;
	private static final int VERTEX_COMPONENTS = 3; // xyz
	private static final int COLOUR_COMPONENTS = 4; // rgba
	private static final int COMPONENT_COUNT = VERTEX_COMPONENTS + COLOUR_COMPONENTS;

	private static final FloatBuffer VERTEX_BUFFER = BufferUtils.createFloatBuffer(NUM_VERTICES * COMPONENT_COUNT);
	private static final FloatBuffer MVP_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer POINT_SIZE_BUFFER = BufferUtils.createFloatBuffer(1);

	private static final int VAO_ID; // The Vertex Array Object ID which holds our shader attributes
	private static final int VBO_ID; // The id of the Vertex Buffer Object containing the grid vertex data

	private static final ShaderProgram SHADER_PROGRAM;

	static {
		SHADER_PROGRAM = new ShaderProgram();
		SHADER_PROGRAM.initFromStrings(VERTEX_SHADER_GLSL_SOURCE, FRAGMENT_SHADER_GLSL_SOURCE);
		SHADER_PROGRAM.addAttribute("vertexLocation");
		SHADER_PROGRAM.addAttribute("vertexColour");
		SHADER_PROGRAM.addUniform("mvpMatrix");

		// ----- Set up our Vertex Array Object (VAO) to hold the shader attributes -----
		// Get an ID for the Vertex Array Object (VAO) and bind to it
		VAO_ID = glGenVertexArrays();
		glBindVertexArray(VAO_ID);

		// ----- Location Vertex Buffer Object (VBO) -----

		// Generate an id for the locationBuffer and bind to it
		VBO_ID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO_ID);

		// Place the location data into the VBO...
		glBufferData(GL_ARRAY_BUFFER, VERTEX_BUFFER, GL_DYNAMIC_DRAW);

		// ...and specify the data format.
		glVertexAttribPointer(SHADER_PROGRAM.attribute("vertexLocation"), // Vertex location attribute index
				VERTEX_COMPONENTS, // Number of vertex components per vertex
				GL_FLOAT, // Data type
				false, // Normalised?
				COMPONENT_COUNT * Float.BYTES, // Stride
				0); // Offset

		// ...and specify the data format.
		glVertexAttribPointer(SHADER_PROGRAM.attribute("vertexColour"),  // Vertex colour attribute index
				COLOUR_COMPONENTS,  // Number of colour components per vertex
				GL_FLOAT,  // Data type
				false,  // Normalised?
				COMPONENT_COUNT * Float.BYTES,  // Stride
				(long) VERTEX_COMPONENTS * Float.BYTES);  // Offset

		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attributes
		glEnableVertexAttribArray(SHADER_PROGRAM.attribute("vertexLocation"));
		glEnableVertexAttribArray(SHADER_PROGRAM.attribute("vertexColour"));

		// Unbind our Vertex Array object - all the buffer and attribute settings above will be associated with our VAO
		glBindVertexArray(0);
	}

	public PointDrawHelper() {
	}

	/// Draw a Point3D as a GL_POINT.
	///
	/// @param location            the location of the point to draw
	/// @param color               the colour with which to draw the point
	/// @param pointSize           the size of the point to draw
	/// @param modelViewProjection the ModelViewProjection matrix with which to draw the point
	public void draw(Vector3f location, Color4fc color, float pointSize, Matrix4fc modelViewProjection) {
		location.get(VERTEX_BUFFER);
		color.get(3, VERTEX_BUFFER);

		SHADER_PROGRAM.use();
		glBindVertexArray(VAO_ID); // bind VAO
		glBindBuffer(GL_ARRAY_BUFFER, VBO_ID); // bing VBO
		glBufferData(GL_ARRAY_BUFFER, VERTEX_BUFFER, GL_DYNAMIC_DRAW);

		// provide the projection matrix uniform
		glUniformMatrix4fv(SHADER_PROGRAM.getUniformLocation("mvpMatrix"), false, modelViewProjection.get(MVP_MATRIX_BUFFER));

		glGetFloatv(GL_POINT_SIZE, POINT_SIZE_BUFFER); // store current GL_POINT_SIZE

		glPointSize(pointSize);
		glDrawArrays(GL_POINTS, 0, NUM_VERTICES); // draw the axis points

		glPointSize(POINT_SIZE_BUFFER.get(0)); // restore previous point size

		glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind VBO
		glBindVertexArray(0); // unbind VAO
		SHADER_PROGRAM.disable();
	}

}
