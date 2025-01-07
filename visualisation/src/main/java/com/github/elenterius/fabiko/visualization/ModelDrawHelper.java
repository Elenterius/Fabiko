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

import com.github.elenterius.fabiko.core.FabrikBone3f;
import com.github.elenterius.fabiko.core.FabrikChain3f;
import com.github.elenterius.fabiko.core.FabrikStructure3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
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
/// Source: <a href="https://github.com/FedUni/caliko/blob/master/caliko-visualisation/src/main/java/au/edu/federation/caliko/visualisation/FabrikModel3D.java">FabrikModel3D</a>
public class ModelDrawHelper {

	private static final String VERTEX_SHADER_SOURCE = """
			#version 330
			in vec3 vertexLocation;   // Incoming vertex attribute
			uniform mat4 mvpMatrix;   // Combined Model/View/Projection matrix
			void main(void) {
				gl_Position = mvpMatrix * vec4(vertexLocation, 1.0); // Project our geometry
			}""";

	private static final String FRAGMENT_SHADER_SOURCE = """
			#version 330
			out vec4 outputColour;
			uniform vec4 colour;
			void main() {
				outputColour = colour;
			}
			""";

	private static final int VERTEX_COMPONENTS = 3; //xyz

	private static final ShaderProgram SHADER_PROGRAM;

	private static final FloatBuffer MVP_MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(4);
	private static final FloatBuffer LINE_WIDTH_BUFFER = BufferUtils.createFloatBuffer(1);

	private static final int VAO_ID;
	private static final int VBO_ID;

	static {
		SHADER_PROGRAM = new ShaderProgram();
		SHADER_PROGRAM.initFromStrings(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
		SHADER_PROGRAM.addAttribute("vertexLocation");
		SHADER_PROGRAM.addUniform("mvpMatrix");
		SHADER_PROGRAM.addUniform("colour");

		// ----- Set up our Vertex Array Object (VAO) to hold the shader attributes -----

		// Create a VAO and bind to it
		VAO_ID = glGenVertexArrays();
		glBindVertexArray(VAO_ID);

		// ----- Vertex Buffer Object (VBO) -----

		// Create a VBO and bind to it
		VBO_ID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO_ID);

		// Note: We do NOT copy the data into the buffer at this time - we do that on draw!

		// Vertex attribute configuration
		glVertexAttribPointer(SHADER_PROGRAM.attribute("vertexLocation"), // Vertex location attribute index
				VERTEX_COMPONENTS, // Number of components per vertex
				GL_FLOAT, // Data type
				false, // Normalised?
				VERTEX_COMPONENTS * Float.BYTES, // Stride
				0); // Offset

		// Unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Enable the vertex attributes
		glEnableVertexAttribArray(SHADER_PROGRAM.attribute("vertexLocation"));

		// Unbind VAO - all the buffer and attribute settings above will now be associated with our VAO
		glBindVertexArray(0);
	}

	// The FloatBuffer which will contain our vertex data - as we may load multiple different models this cannot be static
	private final FloatBuffer vertexBuffer;

	/// The actual Model associated with this FabrikModel3D.
	private final OBJModel model;

	/// The float array storing the axis vertex (including colour) data.
	private float[] modelData;

	/// The line width with which to draw the model in pixels.
	private float lineWidth = 1f;

	/// @param modelFilename the filename of the model to load
	/// @param lineWidth     the width of the lines used to draw the model in pixels
	// Note: width is along +/- x-axis, depth is along +/- z-axis, height is the location on
	// the y-axis, numDivisions is how many lines to draw across each axis
	public ModelDrawHelper(String modelFilename, float lineWidth) {
		// Load the model, get the vertex data and put it into our vertex FloatBuffer
		model = new OBJModel(modelFilename);
		modelData = model.getVertexFloatArray();
		vertexBuffer = BufferUtils.createFloatBuffer(model.getNumVertices() * VERTEX_COMPONENTS);

		this.lineWidth = lineWidth;
	}

	private void drawModel(float lineWidth, Color4fc color, Matrix4fc modelViewProjection) {
		SHADER_PROGRAM.use();

		glBindVertexArray(VAO_ID); // bind to VAO
		glBindBuffer(GL_ARRAY_BUFFER, VBO_ID); // bind VBO

		// Copy the data for this particular model into the vertex float buffer
		// Note: The model is scaled to each individual bone length, hence the GL_DYNAMIC_DRAW performance hint.
		vertexBuffer.put(modelData);
		vertexBuffer.flip();
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);

		// provide uniform data
		glUniformMatrix4fv(SHADER_PROGRAM.getUniformLocation("mvpMatrix"), false, modelViewProjection.get(MVP_MATRIX_BUFFER));
		glUniform4fv(SHADER_PROGRAM.getUniformLocation("colour"), color.get(COLOR_BUFFER));

		glGetFloatv(GL_LINE_WIDTH, LINE_WIDTH_BUFFER); // store current GL_LINE_WIDTH

		glLineWidth(lineWidth); // set GL_LINE_WIDTH
		glDrawArrays(GL_LINES, 0, model.getNumVertices()); // draw the model as lines

		glLineWidth(LINE_WIDTH_BUFFER.get(0)); // restore previous GL_LINE_WIDTH

		glBindBuffer(GL_ARRAY_BUFFER, 0); // unbind from VBO
		glBindVertexArray(0); // unbind from VAO

		SHADER_PROGRAM.disable();
	}

	static Matrix4fc CORRECTION = new Matrix4f().m22(-1);
	Matrix4f m = new Matrix4f();
	Matrix4f mvp = new Matrix4f();

	/**
	 * Draw a bone using the model loaded on this FabrikModel3D.
	 *
	 * @param bone             The bone to draw.
	 * @param viewMatrix       The view matrix, typically retrieved from the camera.
	 * @param projectionMatrix The projection matrix of our scene.
	 * @param color           The colour of the lines used to draw the bone.
	 */
	public void drawBone(FabrikBone3f bone, Matrix4fc viewMatrix, Matrix4fc projectionMatrix, Color4fc color) {
		// Clone the model and scale the clone to be twice as wide and deep, and scaled along the z-axis to match the bone length
		OBJModel modelCopy = OBJModel.clone(model);
		modelCopy.scale(2f, 2f, bone.length());

		// Get our scaled model data
		modelData = modelCopy.getVertexFloatArray();

		Matrix4fc modelMatrix = m.identity().rotation(bone.getOrientation()).setTranslation(bone.getStartLocation()).mul(CORRECTION);
		Matrix4fc modelViewProjection = mvp.set(projectionMatrix).mul(viewMatrix).mul(modelMatrix);

		drawModel(lineWidth, color, modelViewProjection);
	}

	/**
	 * Draw a bone using the model loaded on this FabrikModel3D using a default colour of white at full opacity.
	 *
	 * @param bone             The bone to draw.
	 * @param viewMatrix       The view matrix, typically retrieved from the camera.
	 * @param projectionMatrix The projection matrix of our scene.
	 */
	public void drawBone(FabrikBone3f bone, Matrix4fc viewMatrix, Matrix4fc projectionMatrix) {
		drawBone(bone, viewMatrix, projectionMatrix, Color4fc.WHITE);
	}

	/**
	 * Draw a chain using the model loaded on this FabrikModel3D.
	 *
	 * @param chain            the FabrikChain3f to draw the model as bones on
	 * @param viewMatrix       the view matrix, typically retrieved from the camera
	 * @param projectionMatrix the projection matrix of our scene
	 * @param color            the color of the lines used to draw the model
	 */
	public void drawChain(FabrikChain3f chain, Matrix4fc viewMatrix, Matrix4fc projectionMatrix, Color4fc color) {
		for (FabrikBone3f bone : chain.getBones()) {
			drawBone(bone, viewMatrix, projectionMatrix, color);
		}
	}

	/**
	 * Draw a chain using the model loaded on this FabrikModel3D using a default colour of white at full opacity.
	 *
	 * @param chain            the FabrikChain3f to draw the model as bones on
	 * @param viewMatrix       the view matrix, typically retrieved from the camera
	 * @param projectionMatrix the projection matrix of our scene
	 */
	public void drawChain(FabrikChain3f chain, Matrix4fc viewMatrix, Matrix4fc projectionMatrix) {
		for (FabrikBone3f bone : chain.getBones()) {
			drawBone(bone, viewMatrix, projectionMatrix, Color4fc.WHITE);
		}
	}

	/**
	 * Draw a structure using the model loaded on this FabrikModel3D.
	 *
	 * @param structure        The FabrikStructure3f to draw the model as bones on.
	 * @param viewMatrix       The view matrix, typically retrieved from the camera.
	 * @param projectionMatrix The projection matrix of our scene.
	 * @param color           The colour of the lines used to draw the model.
	 */
	public void drawStructure(FabrikStructure3f structure, Matrix4fc viewMatrix, Matrix4fc projectionMatrix, Color4fc color) {
		for (FabrikChain3f chain : structure.getChains()) {
			drawChain(chain, viewMatrix, projectionMatrix, color);
		}
	}

	/**
	 * Draw a structure using the model loaded on this FabrikModel3D using a default colour of white at full opacity.
	 *
	 * @param structure        The FabrikStructure3f to draw the model as bones on.
	 * @param viewMatrix       The view matrix, typically retrieved from the camera.
	 * @param projectionMatrix The projection matrix of our scene.
	 */
	public void drawStructure(FabrikStructure3f structure, Matrix4fc viewMatrix, Matrix4fc projectionMatrix) {
		for (FabrikChain3f chain : structure.getChains()) {
			drawChain(chain, viewMatrix, projectionMatrix, Color4fc.WHITE);
		}
	}

}
