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
/// Source: <a href="https://github.com/FedUni/caliko/blob/master/caliko-visualisation/src/main/java/au/edu/federation/caliko/visualisation/Line3D.java">Line3D</a>
public class LineDrawHelper {

	private static final String VERTEX_SHADER_GLSL_SOURCE = """
			#version 330
			in vec4 vertexLocation; // Incoming vertex attribute
			out vec4 fragColour;    // Outgoing colour value
			uniform mat4 mvpMatrix; // ModelViewProjectionMatrix
			void main(void) {
				gl_Position = mvpMatrix * vertexLocation; // Project our geometry
			}
			""";

	private static final String FRAGMENT_SHADER_GLSL_SOURCE = """
			#version 330
			out vec4 vOutputColour; // Outgoing colour value
			uniform vec4 colour;
			void main() {
				vOutputColour = colour;
			}
			""";

	private static final Color4fc WHITE_COLOR = new Color4f(1f, 1f, 1f, 0.3f);

	private static final ShaderProgram SHADER_PROGRAM;

	private static final int NUM_VERTICES = 2; // A line has 2 vertices
	private static final int VERTEX_COMPONENTS = 3; // Each vertex has 3 location components (x/y/z)

	private static final FloatBuffer MVP_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer VERTEX_BUFFER = BufferUtils.createFloatBuffer(NUM_VERTICES * VERTEX_COMPONENTS);
	private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(4);
	private static final FloatBuffer LINE_WIDTH_BUFFER = BufferUtils.createFloatBuffer(1);

	private static final int VAO_ID; // The Vertex Array Object ID which holds our shader attributes
	private static final int VBO_ID; // The ID of the vertex buffer containing the grid vertex data

	static {
		SHADER_PROGRAM = new ShaderProgram();
		SHADER_PROGRAM.initFromStrings(VERTEX_SHADER_GLSL_SOURCE, FRAGMENT_SHADER_GLSL_SOURCE);
		SHADER_PROGRAM.addAttribute("vertexLocation");
		SHADER_PROGRAM.addUniform("mvpMatrix");
		SHADER_PROGRAM.addUniform("colour");

		// Get an id for the Vertex Array Object (VAO) and bind to it
		VAO_ID = glGenVertexArrays();
		glBindVertexArray(VAO_ID);

		// Generate an id for our Vertex Buffer Object (VBO) and bind to it
		VBO_ID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO_ID);

		// Place the location data into the VBO...
		glBufferData(GL_ARRAY_BUFFER, VERTEX_BUFFER, GL_DYNAMIC_DRAW);

		// ...and specify the data format.
		glVertexAttribPointer(SHADER_PROGRAM.attribute("vertexLocation"), // Vertex location attribute index
				VERTEX_COMPONENTS, // Number of normal components per vertex
				GL_FLOAT, // Data type
				false, // Normalised?
				VERTEX_COMPONENTS * Float.BYTES, // Stride
				0); // Offset

		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attributes
		glEnableVertexAttribArray(SHADER_PROGRAM.attribute("vertexLocation"));

		// Unbind from our VAO, saving all settings
		glBindVertexArray(0);
	}

	public LineDrawHelper() {
	}

	/// Draw a line with the default colour.
	///
	/// @param p1                  The first point
	/// @param p2                  The second point
	/// @param lineWidth           The width of the line in pixels
	/// @param modelViewProjection The ModelViewProjection matrix with which to draw the line
	public void draw(Vector3fc p1, Vector3fc p2, float lineWidth, Matrix4fc modelViewProjection) {
		draw(p1, p2, WHITE_COLOR, lineWidth, modelViewProjection);
	}

	/// Draw a line with interpolated colours from the first to second vertices.
	///
	/// @param color               The colour to draw the line
	/// @param p1                  The first point
	/// @param p2                  The second point
	/// @param lineWidth           The width of the line in pixels
	/// @param modelViewProjection The ModelViewProjection matrix with which to draw the line
	public void draw(Vector3fc p1, Vector3fc p2, Color4fc color, float lineWidth, Matrix4fc modelViewProjection) {
		p1.get(VERTEX_BUFFER);
		p2.get(3, VERTEX_BUFFER);

		SHADER_PROGRAM.use();
		glBindVertexArray(VAO_ID); //bind to our VAO

		// bind to the vertex buffer object (VBO) and place the new data into it
		glBindBuffer(GL_ARRAY_BUFFER, VBO_ID);
		glBufferData(GL_ARRAY_BUFFER, VERTEX_BUFFER, GL_DYNAMIC_DRAW);

		//provide uniforms to our shader
		glUniformMatrix4fv(SHADER_PROGRAM.getUniformLocation("mvpMatrix"), false, modelViewProjection.get(MVP_MATRIX_BUFFER));
		glUniform4fv(SHADER_PROGRAM.getUniformLocation("colour"), color.get(COLOR_BUFFER));

		glGetFloatv(GL_LINE_WIDTH, LINE_WIDTH_BUFFER); // store current GL_LINE_WIDTH

		glLineWidth(lineWidth);
		glDrawArrays(GL_LINES, 0, NUM_VERTICES); // draw the line

		glLineWidth(LINE_WIDTH_BUFFER.get(0)); // restore previous GL_LINE_WIDTH

		// unbind from VBO & VAO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		SHADER_PROGRAM.disable();
	}

}
