package au.edu.federation.caliko.demo;

import au.edu.federation.caliko.math.Mat4f;

/**
 * @author jsalvo
 */
public interface CalikoDemoStructure {
	
	void setup();
	
	void drawTarget(Mat4f mvpMatrix);
	
}
