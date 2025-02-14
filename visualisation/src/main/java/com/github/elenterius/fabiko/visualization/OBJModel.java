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

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/// Copy from the Caliko library.
///
/// Original Author: Al Lansley
///
/// Source: <a href="https://github.com/FedUni/caliko/blob/master/caliko-visualisation/src/main/java/au/edu/federation/caliko/visualisation/Line3D.java">Line3D</a>
///
///
/// A class to represent and load a 3D model in WaveFront .OBJ format.
///
/// Vertices, normals, and faces with or without normal indices are supported.
///
/// Models must be stored as triangles, not quads.
///
/// There is no support for textures, texture coordinates, or grouped objects at this time.
public class OBJModel {

	private static final String NUMBER_OF_VERTICES_LOG = "Number of vertices in data array: %d (%d bytes)";
	private static final String NUMBER_OF_NORMALS_LOG = "Number of normals  in data array: %d (%d bytes)";
	private static final String WRONG_COMPONENT_COUNT_LOG = "Found %s data with wrong component count at line number: %d - Skipping!";
	private static boolean VERBOSE = false;

	// These are the models values as read from the file - they are not the final, consolidated model data
	private List<Vector3fc> vertices = new ArrayList<>();
	private List<Vector3fc> normals = new ArrayList<>();
	private List<Vector3ic> normalIndices = new ArrayList<>();
	private List<Vector3ic> faces = new ArrayList<>();
	//ArrayList texCoords = new ArrayList<Vector>();

	// The vertexData and normalData arrays are the final consolidated data which we can draw with.
	//
	// Note: If the model has only vertices and/or normals, then the vertexData and normalData will be a direct 'unwrapped'
	// version of the vertices and normals arrays. However, if we're using faces then there will likely be a lower number of
	// vertices / normals as each vertex / normal may be used more than once in the model - the end result of this is that
	// the vertexData and normalData will likely be larger than the 'as-read-from-file' vertices and normals arrays because
	// of this duplication of values from the face data.
	private List<Float> vertexData = new ArrayList<>();
	private List<Float> normalData = new ArrayList<>();
	//ArrayList texCoordData = new ArrayList<Vector>();

	// Counters to keep track of how many vertices, normals, normal indices, texture coordinates and faces
	private int numVertices;
	private int numNormals;
	private int numNormalIndices;
	private int numFaces;
	//private int numTexCoords;

	public OBJModel() {
	}

	/**
	 * Constructor which creates a model object and loads the model from file.
	 *
	 * @param filename The file to load the model data from.
	 */
	public OBJModel(String filename) {
		load(filename);
	}

	/**
	 * Enable verbose messages.
	 */
	public static void enableVerbose() {
		VERBOSE = true;
	}

	/**
	 * Disable verbose messages.
	 */
	public static void disableVerbose() {
		VERBOSE = false;
	}

	// Method provide create a deep copy of a Model so we can say copyOfMyModel.equals(myModel);
	// Note: We only deep copy the vertexData and normalData arrays, not all the vectors!
	public static OBJModel clone(OBJModel sourceModel) {
		// Create a new Model which we will clone across the data to from our source model
		OBJModel model = new OBJModel();

		// Update the counts of vertices and normals for the clone to match the source model
		model.numVertices = sourceModel.getNumVertices();
		model.numNormals = sourceModel.getNumNormals();

		// If the source model has vertices then copy them across to the clone...
		if (model.numVertices > 0) {
			model.vertexData.addAll(sourceModel.getVertexData());
		}
		else {
			throw new RuntimeException("Model created using clone method has 0 vertices!");
		}

		// If the source model has normals then copy them across to the clone...
		if (model.numNormals > 0) {
			model.normalData.addAll(sourceModel.getNormalData());
		}
		else // ...or (potentially) inform the user if they are no normals. This is not necessarily a deal breaker though, so we don't abort.
		{
			if (VERBOSE) {
				System.out.println("Model created using clone method has 0 normals - continuing...");
			}
		}

		if (VERBOSE) {
			System.out.println("Model successfully cloned.");
		}

		return model;
	}

	/**
	 * Load a .OBJ model from file.
	 * <p>
	 * By default, no feedback is provided on the model loading.  If you wish to see what's going on
	 * internally, call Model.enableVerbose() before loading the model - this will display statistics
	 * about any vertices/normals/normal indices/faces found in the model, as well as any malformed data.
	 * <p>
	 * If the model file does not contain any vertices then a RuntimeException is thrown.
	 * If the file cannot be found then a FileNotFoundException is thrown.
	 * If there was a file-system-type error when reading the file then an IOException is thrown.
	 *
	 * @param filename The name of the Wavefront .OBJ format model to load, include the path if necessary.
	 * @return Whether the file loaded successfully or not. Loading with warnings still counts as a
	 * successful load - if necessary enable verbose mode to ensure your model loaded cleanly.
	 */
	public boolean load(String filename) {
		boolean modelLoadedCleanly = loadModel(filename);

		if (VERBOSE) {
			if (modelLoadedCleanly) {
				System.out.println("Model loaded cleanly.");
			}
			else {
				System.out.println("Model loaded with errors.");
			}
		}

		// Do we have vertices? If not then this is a non-recoverable error and we abort!
		if (hasVertices()) {
			if (VERBOSE) {
				System.out.println("Model vertex count: " + getNumVertices());
				if (hasFaces()) {
					System.out.println("Model face         count: " + getNumFaces());
				}
				if (hasNormals()) {
					System.out.println("Model normal       count: " + getNumNormals());
				}
				if (hasNormalIndices()) {
					System.out.println("Model normal index count: " + getNumNormalIndices());
				}
			}
		}
		else {
			throw new RuntimeException("Model has no vertices.");
		}

		// Transfer the loaded data in our vectors to the data arrays
		setupData();

		// Delete the vertices, normals, normalIndices and faces Lists as we now have the final
		// data stored in the vertexData and normalData Lists.
		vertices.clear();
		normals.clear();
		normalIndices.clear();
		faces.clear();
		vertices = null;
		normals = null;
		normalIndices = null;
		faces = null;

		// Indicate that the model loaded successfully
		return true;
	}

	/**
	 * Get the vertex data as a list of floats.
	 *
	 * @return The vertex data.
	 */
	public List<Float> getVertexData() {
		return vertexData;
	}

	/**
	 * Get the vertex normals as a list of floats.
	 *
	 * @return The vertex normal data.
	 */
	public List<Float> getNormalData() {
		return normalData;
	}

	/**
	 * Get the vertex data as a float array suitable for transfer into a FloatBuffer for drawing.
	 *
	 * @return The vertex data as a float array.
	 **/
	public float[] getVertexFloatArray() {
		int numVertexFloats = vertexData.size();

		float[] vertexFloatArray = new float[numVertexFloats];

		for (int i = 0; i < numVertexFloats; i++) {
			vertexFloatArray[i] = vertexData.get(i);
		}

		return vertexFloatArray;
	}

	/**
	 * Get the vertex normal data as a float array suitable for transfer into a FloatBuffer for drawing.
	 *
	 * @return The vertex normal data as a float array.
	 */
	public float[] getNormalFloatArray() {
		// How many floats are there in our list of normal data?
		int numNormalFloats = normalData.size();

		// Create an array big enough to hold them
		float[] normalFloatArray = new float[numNormalFloats];

		// Loop over each item in the list, setting it to the appropriate element in the array
		for (int i = 0; i < numNormalFloats; i++) {
			normalFloatArray[i] = normalData.get(i);
		}

		// Finally, return the float array
		return normalFloatArray;
	}

	// Methods to get the sizes of various data arrays
	// Note: Type.BYTES returns the size of on object of this type in Bytes, and we multiply
	// by 3 because there are 3 components to a vertex (x/y/z), normal (s/t/p) and 3 vertexes comprising a face (i.e. triangle)

	/**
	 * Get the vertex data size in bytes.
	 *
	 * @return The vertex data size in bytes.
	 */
	public int getVertexDataSizeBytes() {
		return numVertices * 3 * Float.BYTES;
	}

	/**
	 * Get the vertex normal data size in bytes.
	 *
	 * @return The vertex normal data size in bytes.
	 */
	public int getNormalDataSizeBytes() {
		return numNormals * 3 * Float.BYTES;
	}

	/**
	 * Get the face data size in bytes.
	 *
	 * @return The face data size in bytes.
	 **/
	public int getFaceDataSizeBytes() {
		return numFaces * 3 * Integer.BYTES;
	}

	/**
	 * Get the number of vertices in this model.
	 *
	 * @return The number of vertices in this model.
	 */
	public int getNumVertices() {
		return numVertices;
	}

	/**
	 * Get the number of vertex normals in this model.
	 *
	 * @return The number of normals in this model.
	 */
	public int getNumNormals() {
		return numNormals;
	}

	/**
	 * Get the number of normal indices in this model.
	 *
	 * @return The number of normal indices in this model.
	 */
	public int getNumNormalIndices() {
		return numNormalIndices;
	}

	/**
	 * Get the number of faces in this model.
	 *
	 * @return The number of faces in this model.
	 */
	public int getNumFaces() {
		return numFaces;
	}

	/**
	 * Scale this model uniformly along the x/y/z axes.
	 *
	 * @param scale The amount to scale the model.
	 **/
	public void scale(float scale) {
		vertexData.replaceAll(f -> f * scale);
	}

	/**
	 * Scale this model on the X axis.
	 *
	 * @param scale The amount to scale the model on the X axis.
	 */
	public void scaleX(float scale) {
		for (int i = 0; i < vertexData.size(); i += 3) {
			vertexData.set(i, vertexData.get(i) * scale);
		}
	}

	/**
	 * Scale this model on the Y axis.
	 *
	 * @param scale The amount to scale the model on the Y axis.
	 */
	public void scaleY(float scale) {
		for (int i = 1; i < vertexData.size(); i += 3) {
			vertexData.set(i, vertexData.get(i) * scale);
		}
	}

	/**
	 * Scale this model on the Z axis.
	 *
	 * @param scale The amount to scale the model on the Z axis.
	 */
	public void scaleZ(float scale) {
		for (int i = 2; i < vertexData.size(); i += 3) {
			vertexData.set(i, vertexData.get(i) * scale);
		}
	}

	/**
	 * Scale this model by various amounts along separate axes.
	 *
	 * @param xScale The amount to scale the model on the X axis.
	 * @param yScale The amount to scale the model on the Y axis.
	 * @param zScale The amount to scale the model on the Z axis.
	 */
	public void scale(float xScale, float yScale, float zScale) {
		for (int i = 0; i < vertexData.size(); i++) {
			switch (i % 3) {
				case 0:
					vertexData.set(i, vertexData.get(i) * xScale);
					break;
				case 1:
					vertexData.set(i, vertexData.get(i) * yScale);
					break;
				case 2:
					vertexData.set(i, vertexData.get(i) * zScale);
					break;
			}
		}
	}

	/**
	 * Print out the vertices of this model.
	 */
	public void printVertices() {
		for (Vector3fc v : vertices) {
			System.out.println("Vertex: " + v.toString());
		}
	}

	/**
	 * Print out the vertex normal data for this model.
	 * <p>
	 * Note: This is the contents of the normals list, not the (possibly expanded) normalData array.
	 */
	public void printNormals() {
		for (Vector3fc n : normals) {
			System.out.println("Normal: " + n.toString());
		}
	}

	/**
	 * Print the face data of this model.
	 * <p>
	 * Note: Faces are ONE indexed, not zero indexed.
	 */
	public void printFaces() {
		for (Vector3ic face : faces) {
			System.out.println("Face: " + face.toString());
		}
	}

	/**
	 * Print the vertex data of this model.
	 * <p>
	 * Note: This is the contents of the vertexlData array which is actually used when drawing - and which may be
	 * different to the 'vertices' list when using faces (where vertices get re-used).
	 */
	public void printVertexData() {
		for (int i = 0; i < vertexData.size(); i += 3) {
			System.out.printf("Vertex data element %d is x: %s\ty: %s\tz: %s%n", i / 3, vertexData.get(i), vertexData.get(i + 1), vertexData.get(i + 2));
		}
	}

	/**
	 * Print the normal data of this model.
	 * <p>
	 * Note: This is the contents of the normalData array which is actually used when drawing - and which may be
	 * different to the 'normals' list when using faces (where normals get re-used).
	 */
	public void printNormalData() {
		for (int i = 0; i < normalData.size(); i += 3) {
			System.out.printf("Normal data element %d is x: %s\ty: %s\tz: %s%n", i / 3, normalData.get(i), normalData.get(i + 1), normalData.get(i + 2));
		}
	}

	// Method to read through the model file adding all vertices, faces and normals to our
	// vertices, faces and normals vectors.

	// Note: This does NOT transfer the data into our vertexData, faceData or normalData arrays!
	//       That must be done as a separate step by calling setupData() after building up the
	//	     arraylists with this method!

	// Also: This method does not decrement the face number of normal index by 1 (because .OBJ
	//	     files start their counts at 1) to put them in a range starting from 0, that job
	//	     is done in the setupData() method performed after calling this method!
	private boolean loadModel(String filename) {
		vertices = new ArrayList<>();
		normals = new ArrayList<>();
		normalIndices = new ArrayList<>();
		faces = new ArrayList<>();
		//texCoords = new ArrayList<Vec2f();

		vertexData = new ArrayList<>();
		normalData = new ArrayList<>();
		//texCoordData = new ArrayList<Vec2f>();

		// Our vectors of attributes are initially empty
		numVertices = 0;
		numNormals = 0;
		numNormalIndices = 0;
		numFaces = 0;
		//numTexCoords = 0;

		boolean loadedCleanly = true;

		// Counter to keep track of what line we're on
		int lineCount = 0;

		// Use this for jar packaged resources
		InputStream is = this.getClass().getResourceAsStream(filename);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is)))
		//try (BufferedReader br = new BufferedReader( new FileReader(filename) ) )  // Use this for loading from file in Eclipse or such
		{
			// We'll read through the file one line at a time - this will hold the current line we're working on
			String line;

			// While there are lines left to read in the file...
			while ((line = br.readLine()) != null) {
				++lineCount;

				// If the line isn't empty (the 1 character is the carriage return), process it...
				if (line.length() > 1) {
					// Split line on spaces into an array of strings.The + on the end of "\\s+" means 'do not accept
					// blank entries' which can occur if you have two consecutive space characters (as happens when
					// you export a .obj from 3ds max - you get "v  1.23 4.56 7.89" etc.)
					String[] token = line.split("\\s+");

					// If the first token is "v", then we're dealing with vertex data
					if ("v".equalsIgnoreCase(token[0])) {
						// As long as there's 4 tokens on the line...
						if (token.length == 4) {
							// ...get the remaining 3 tokens as the x/y/z float values...
							float x = Float.parseFloat(token[1]);
							float y = Float.parseFloat(token[2]);
							float z = Float.parseFloat(token[3]);

							// ... and push them into the vertices vector ...
							vertices.add(new Vector3f(x, y, z));

							// .. then increase our vertex count.
							numVertices++;
						}
						else // If we got vertex data without 3 components - whine!
						{
							loadedCleanly = false;
							System.out.printf(WRONG_COMPONENT_COUNT_LOG, "vertex", lineCount);
						}

					}
					else if ("vn".equalsIgnoreCase(token[0])) // If the first token is "vn", then we're dealing with a vertex normal
					{
						// As long as there's 4 tokens on the line...
						if (token.length == 4) {
							// ...get the remaining 3 tokens as the x/y/z normal float values...
							float normalX = Float.parseFloat(token[1]);
							float normalY = Float.parseFloat(token[2]);
							float normalZ = Float.parseFloat(token[3]);

							// ... and push them into the normals vector ...
							normals.add(new Vector3f(normalX, normalY, normalZ));

							// .. then increase our normal count.
							numNormals++;
						}
						else // If we got normal data without 3 components - whine!
						{
							loadedCleanly = false;
							System.out.printf(WRONG_COMPONENT_COUNT_LOG, "normal", lineCount);
						}

					} // End of vertex line parsing

					// If the first token is "f", then we're dealing with faces
					//
					// Note: Faces can be described in two ways - we can have data like 'f 123 456 789' which means that the face is comprised
					// of vertex 123, vertex 456 and vertex 789. Or, we have have data like f 123//111 456//222 789//333 which means that
					// the face is comprised of vertex 123 using normal 111, vertex 456 using normal 222 and vertex 789 using normal 333.
					else if ("f".equalsIgnoreCase(token[0])) {
						// Check if there's a double-slash in the line
						int pos = line.indexOf("//");

						// As long as there's four tokens on the line and they don't contain a "//"...
						if ((token.length == 4) && (pos == -1)) {
							// ...get the face vertex numbers as ints ...
							int v1 = Integer.parseInt(token[1]);
							int v2 = Integer.parseInt(token[2]);
							int v3 = Integer.parseInt(token[3]);

							// ... and push them into the faces vector ...
							faces.add(new Vector3i(v1, v2, v3));

							// .. then increase our face count.
							numFaces++;
						}
						else if ((token.length == 4) && (pos != -1)) // 4 tokens and found 'vertex//normal' notation?
						{
							// ----- Get the 1st of three tokens as a String -----

							// Find where the double-slash starts in that token
							int faceEndPos = token[1].indexOf("//");

							// Put sub-String from the start to the beginning of the double-slash into our subToken String
							String faceToken1 = token[1].substring(0, faceEndPos);

							// Convert face token value to int
							int ft1 = Integer.parseInt(faceToken1);

							// Mark the start of our next subtoken
							int nextTokenStartPos = faceEndPos + 2;

							// Copy from first character after the "//" to the end of the token
							String normalToken1 = token[1].substring(nextTokenStartPos);

							// Convert normal token value to int
							int nt1 = Integer.parseInt(normalToken1);

							// ----- Get the 2nd of three tokens as a String -----

							// Find where the double-slash starts in that token
							faceEndPos = token[2].indexOf("//");

							// Put sub-String from the start to the beginning of the double-slash into our subToken String
							String faceToken2 = token[2].substring(0, faceEndPos);

							// Convert face token value to int
							int ft2 = Integer.parseInt(faceToken2);

							// Mark the start of our next subtoken
							nextTokenStartPos = faceEndPos + 2;

							// Copy from first character after the "//" to the end of the token
							String normalToken2 = token[2].substring(nextTokenStartPos);

							// Convert normal token value to int
							int nt2 = Integer.parseInt(normalToken2);

							// ----- Get the 3rd of three tokens as a String -----

							// Find where the double-slash starts in that token
							faceEndPos = token[3].indexOf("//");

							// Put sub-String from the start to the beginning of the double-slash into our subToken String
							String faceToken3 = token[3].substring(0, faceEndPos);

							// Convert face token value to int
							int ft3 = Integer.parseInt(faceToken3);

							// Mark the start of our next subtoken
							nextTokenStartPos = faceEndPos + 2;

							// Copy from first character after the "//" to the end
							String normalToken3 = token[3].substring(nextTokenStartPos);

							// Convert normal token value to int
							int nt3 = Integer.parseInt(normalToken3);


							// Finally, add the face to the faces array list and increment the face count...
							faces.add(new Vector3i(ft1, ft2, ft3));
							numFaces++;

							// ...and do the same for the normal indices and the normal index count.
							normalIndices.add(new Vector3i(nt1, nt2, nt3));
							numNormalIndices++;

						}
						else // If we got face data without 3 components - whine!
						{
							loadedCleanly = false;
							System.out.printf(WRONG_COMPONENT_COUNT_LOG, "face", lineCount);
						}

					} // End of if token is "f" (i.e. face indices)

					// IMPLIED ELSE: If first token is something we don't recognise then we ignore it as a comment.

				} // End of line parsing section

			} // End of if line length > 1 check

			// No need to close the file ( i.e. br.close() ) - try-with-resources does that for us.
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		// Return our boolean flag to say whether we loaded the model cleanly or not
		return loadedCleanly;
	}

	/**
	 * Return whether or not this model contains vertex data.
	 *
	 * @return whether or not this model contains vertex data.
	 */
	public boolean hasVertices() {
		return (numVertices > 0);
	}

	/**
	 * Return whether or not this model contains face data.
	 *
	 * @return whether or not this model contains face data.
	 */
	public boolean hasFaces() {
		return (numFaces > 0);
	}

	/**
	 * Return whether or not this model contains normal data.
	 *
	 * @return whether or not this model contains normal data.
	 */
	public boolean hasNormals() {
		return (numNormals > 0);
	}

	/**
	 * Return whether or not this model contains normal index data.
	 *
	 * @return whether or not this model contains normal index data.
	 */
	public boolean hasNormalIndices() {
		return (numNormalIndices > 0);
	}

	/**
	 * Set up our plain arrays of floats for OpenGL to work with.
	 * <p>
	 * Note: We CANNOT have size mismatches! The vertex count must match the
	 * normal count i.e. every vertex must have precisely ONE normal - no more, no less!
	 */
	private void setupData() {
		if (VERBOSE) {
			System.out.println("Setting up model data to draw as arrays.");
		}

		// If we ONLY have vertex data, then transfer just that...
		if ((hasVertices()) && (!hasFaces()) && (!hasNormals())) {
			if (VERBOSE) {
				System.out.println("Model has no faces or normals. Transferring vertex data only.");
			}

			// Reset the vertex count
			numVertices = 0;

			// Transfer all vertices from the vertices vector to the vertexData array
			for (Vector3fc v : vertices) {
				vertexData.add(v.x());
				vertexData.add(v.y());
				vertexData.add(v.z());
				++numVertices;
			}

			// Print a summary of the vertex data
			if (VERBOSE) {
				System.out.printf(NUMBER_OF_VERTICES_LOG, numVertices, getVertexDataSizeBytes());
			}
		}
		// If we have vertices AND faces BUT NOT normals...
		else if ((hasVertices()) && (hasFaces()) && (!hasNormals())) {
			if (VERBOSE) {
				System.out.println("Model has vertices and faces, but no normals. Per-face normals will be generated.");
			}

			// Create the vertexData and normalData arrays from the vector of faces
			// Note: We generate the face normals ourselves.
			int vertexCount = 0;
			int normalCount = 0;

			Vector3f v1 = new Vector3f();
			Vector3f v2 = new Vector3f();

			for (Vector3ic iv : faces) {
				// Get the numbers of the three vertices that this face is comprised of
				int firstVertexNum = iv.x();
				int secondVertexNum = iv.y();
				int thirdVertexNum = iv.z();

				// Now that we have the vertex numbers, we need to get the actual vertices
				// Note: We subtract 1 from the number of the vertex because faces start at
				//       face number 1 in the .OBJ format, while in our code the first vertex
				//       will be at location zero.
				Vector3fc faceVert1 = vertices.get(firstVertexNum - 1);
				Vector3fc faceVert2 = vertices.get(secondVertexNum - 1);
				Vector3fc faceVert3 = vertices.get(thirdVertexNum - 1);

				// Now that we have the 3 vertices, we need to calculate the normal of the face
				// formed by these vertices...

				// Convert this vertex data into a pure form
				v1.set(faceVert2).sub(faceVert1);
				v2.set(faceVert3).sub(faceVert1);

				// Generate the normal as the cross product and normalise it
				Vector3fc normalizedNormal = v1.cross(v2).normalize();

				// Put the vertex data into our vertexData array
				vertexData.add(faceVert1.x());
				vertexData.add(faceVert1.y());
				vertexData.add(faceVert1.z());
				vertexCount++;

				vertexData.add(faceVert2.x());
				vertexData.add(faceVert2.y());
				vertexData.add(faceVert2.z());
				vertexCount++;

				vertexData.add(faceVert3.x());
				vertexData.add(faceVert3.y());
				vertexData.add(faceVert3.z());
				vertexCount++;

				// Put the normal data into our normalData array
				//
				// Note: we put the same normal into the normalData array for each of the 3 vertices comprising the face!
				// This gives use a 'faceted' looking model, but is easy! You could try to calculate an interpolated
				// normal based on surrounding normals, but that's not a trivial task (although 3ds max will do it for you
				// if you load up the model and export it with normals!)
				normalData.add(normalizedNormal.x());
				normalData.add(normalizedNormal.y());
				normalData.add(normalizedNormal.z());
				normalCount++;

				normalData.add(normalizedNormal.x());
				normalData.add(normalizedNormal.y());
				normalData.add(normalizedNormal.z());
				normalCount++;

				normalData.add(normalizedNormal.x());
				normalData.add(normalizedNormal.y());
				normalData.add(normalizedNormal.z());
				normalCount++;

			} // End of loop iterating over the model faces

			numVertices = vertexCount;
			numNormals = normalCount;

			if (VERBOSE) {
				System.out.printf(NUMBER_OF_VERTICES_LOG, numVertices, getVertexDataSizeBytes());
				System.out.printf(NUMBER_OF_NORMALS_LOG, numNormals, getNormalDataSizeBytes());
			}
		}
		// If we have vertices AND faces AND normals AND normalIndices...
		else if ((hasVertices()) && (hasFaces()) && (hasNormals()) && (hasNormalIndices())) {
			if (VERBOSE) {
				System.out.println("Model has vertices, faces, normals & normal indices. Transferring data.");
			}

			//FIXME: Change this to use the numVertices and numNormals directly - I don't see a reason to use separate vertexCount and normalCount vars...

			int vertexCount = 0;
			int normalCount = 0;

			// Look up each vertex specified by each face and add the vertex data to the vertexData array
			for (Vector3ic iv : faces) {
				// Get the numbers of the three vertices that this face is comprised of
				int firstVertexNum = iv.x();
				int secondVertexNum = iv.y();
				int thirdVertexNum = iv.z();

				// Now that we have the vertex numbers, we need to get the actual vertices
				// Note: We subtract 1 from the number of the vertex because faces start at
				//       face number 1 in the .oBJ format, while in our code the first vertex
				//       will be at location zero.
				Vector3fc faceVert1 = vertices.get(firstVertexNum - 1);
				Vector3fc faceVert2 = vertices.get(secondVertexNum - 1);
				Vector3fc faceVert3 = vertices.get(thirdVertexNum - 1);

				// Put the vertex data into our vertexData array
				vertexData.add(faceVert1.x());
				vertexData.add(faceVert1.y());
				vertexData.add(faceVert1.z());
				++vertexCount;

				vertexData.add(faceVert2.x());
				vertexData.add(faceVert2.y());
				vertexData.add(faceVert2.z());
				++vertexCount;

				vertexData.add(faceVert3.x());
				vertexData.add(faceVert3.y());
				vertexData.add(faceVert3.z());
				++vertexCount;
			}

			// Look up each normal specified by each normal index and add the normal data to the normalData array
			for (Vector3ic normInd : normalIndices) {
				// Get the numbers of the three normals that this face uses
				int firstNormalNum = normInd.x();
				int secondNormalNum = normInd.y();
				int thirdNormalNum = normInd.z();

				// Now that we have the normal index numbers, we need to get the actual normals
				// Note: We subtract 1 from the number of the normal because normals start at
				//       number 1 in the .obJ format, while in our code the first vertex
				//       will be at location zero.
				Vector3fc normal1 = normals.get(firstNormalNum - 1);
				Vector3fc normal2 = normals.get(secondNormalNum - 1);
				Vector3fc normal3 = normals.get(thirdNormalNum - 1);

				normalData.add(normal1.x());
				normalData.add(normal1.y());
				normalData.add(normal1.z());
				normalCount++;

				normalData.add(normal2.x());
				normalData.add(normal2.y());
				normalData.add(normal2.z());
				normalCount++;

				normalData.add(normal3.x());
				normalData.add(normal3.y());
				normalData.add(normal3.z());
				normalCount++;
			}

			numVertices = vertexCount;
			numNormals = normalCount;

			if (VERBOSE) {
				System.out.printf(NUMBER_OF_VERTICES_LOG, numVertices, getVertexDataSizeBytes());
				System.out.printf(NUMBER_OF_NORMALS_LOG, numNormals, getNormalDataSizeBytes());
			}
		}
		else {
			System.out.println("Something bad happened in Model.setupData() =(");
		}

	}

}
