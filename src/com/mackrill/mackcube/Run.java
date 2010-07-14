package com.mackrill.mackcube;

import android.app.Activity;
import android.os.Bundle;

/**
 * The initial Android Activity, setting and initiating
 * the OpenGL ES Renderer Class @see Lesson07.java
 * 
 * @author Savas Ziplies (nea/INsanityDesign)
 */
public class Run extends Activity {

	/** Our own OpenGL View overridden */
	private MackCube mackCube;

	/**
	 * Initiate our @see Lesson07.java,
	 * which is GLSurfaceView and Renderer
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Initiate our Lesson with this Activity Context handed over
		mackCube = new MackCube(this);
		//Set the lesson as View to the Activity
		setContentView(mackCube);
	}

	/**
	 * Remember to resume our Lesson
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mackCube.onResume();
	}

	/**
	 * Also pause our Lesson
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mackCube.onPause();
	}

}