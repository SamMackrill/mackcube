package com.mackrill.mackcube;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * This is a port of the {@link http://nehe.gamedev.net} OpenGL 
 * tutorials to the Android 1.5 OpenGL ES platform. Thanks to 
 * NeHe and all contributors for their great tutorials and great 
 * documentation. This source should be used together with the
 * textual explanations made at {@link http://nehe.gamedev.net}.
 * The code is based on the original Visual C++ code with all
 * comments made. It has been altered and extended to meet the
 * Android requirements. The Java code has according comments.
 * 
 * If you use this code or find it helpful, please visit and send
 * a shout to the author under {@link http://www.insanitydesign.com/}
 * 
 * @DISCLAIMER
 * This source and the whole package comes without warranty. It may or may
 * not harm your computer or cell phone. Please use with care. Any damage
 * cannot be related back to the author. The source has been tested on a
 * virtual environment and scanned for viruses and has passed all tests.
 * 
 * 
 * This is an interpretation of "Lesson 07: Texture Mapping"
 * for the Google Android platform.
 * 
 * @author Savas Ziplies (nea/INsanityDesign)
 */
public class MackCube extends GLSurfaceView implements Renderer, OnGestureListener {
	
	/** Cube instance */
	private Cube cube;	
	
	private static final String TAG = "MackCube";
	private GestureDetector gestureScanner;
	
	/* Rotation values */
	private float xrot;					//X Rotation
	private float yrot;					//Y Rotation

	/* Rotation speed values */
	private float xspeed;				//X Rotation Speed ( NEW )
	private float yspeed;				//Y Rotation Speed ( NEW )
	
	private float z = -10.0f;			//Depth Into The Screen ( NEW )
	private float orbitHeight = 5.0f;			//Depth Into The Screen ( NEW )
	
	private int filter = 0;				//Which texture filter? ( NEW )
	
	/** Is light enabled ( NEW ) */
	private boolean light = false;

	/* 
	 * The initial light values for ambient and diffuse
	 * as well as the light position ( NEW ) 
	 */
	private float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
	private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
	private float[] lightPosition = {0.0f, 0.0f, 2.0f, 1.0f};
		
	/* The buffers for our light values ( NEW ) */
	private FloatBuffer lightAmbientBuffer;
	private FloatBuffer lightDiffuseBuffer;
	private FloatBuffer lightPositionBuffer;
	
	/*
	 * These variables store the previous X and Y
	 * values as well as a fix touch scale factor.
	 * These are necessary for the rotation transformation
	 * added to this lesson, based on the screen touches. ( NEW )
	 */
	private float oldX;
    private float oldY;
	private final float TOUCH_SCALE = 0.2f;		//Proved to be good for normal rotation ( NEW )
	private boolean zooming = false;
	
	/** The Activity Context */
	private Context context;

	private float zoomStartDist;

	private float zoomStartZ;
	
	/**
	 * Instance the Cube object and set the Activity Context 
	 * handed over. Initiate the light buffers and set this 
	 * class as renderer for this now GLSurfaceView.
	 * Request Focus and set if focusable in touch mode to
	 * receive the Input from Screen and Buttons  
	 * 
	 * @param context - The Activity Context
	 */
	public MackCube(Context context) {
		super(context);
		
		//Set this as Renderer
		this.setRenderer(this);
		//Request focus, otherwise buttons won't react
		this.requestFocus();
		this.setFocusableInTouchMode(true);
		
		//
		this.context = context;
		gestureScanner = new GestureDetector(this);
		
		//
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		lightAmbientBuffer = byteBuf.asFloatBuffer();
		lightAmbientBuffer.put(lightAmbient);
		lightAmbientBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		lightDiffuseBuffer = byteBuf.asFloatBuffer();
		lightDiffuseBuffer.put(lightDiffuse);
		lightDiffuseBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightPosition.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		lightPositionBuffer = byteBuf.asFloatBuffer();
		lightPositionBuffer.put(lightPosition);
		lightPositionBuffer.position(0);
		
		//
		cube = new Cube();
	}

	/**
	 * The Surface is created/init()
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {		
		//And there'll be light!
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);		//Setup The Ambient Light ( NEW )
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);		//Setup The Diffuse Light ( NEW )
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);	//Position The Light ( NEW )
		gl.glEnable(GL10.GL_LIGHT0);											//Enable Light 0 ( NEW )

		//Settings
		gl.glDisable(GL10.GL_DITHER);				//Disable dithering ( NEW )
		gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping
		gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
		gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do
		
		//Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
				
		//Load the texture for the cube once during Surface creation
		cube.loadGLTexture(gl, this.context);
	}

	/**
	 * Here we do our drawing
	 */
	public void onDrawFrame(GL10 gl) {
		//Clear Screen And Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	
		gl.glLoadIdentity();					//Reset The Current Modelview Matrix
		
		//Check if the light flag has been set to enable/disable lighting
		if(light) {
			gl.glEnable(GL10.GL_LIGHTING);
		} else {
			gl.glDisable(GL10.GL_LIGHTING);
		}
		
		//Drawing
		gl.glTranslatef(0.0f, 0.0f, z);			//Move z units into the screen
		gl.glScalef(0.8f, 0.8f, 0.8f); 			//Scale the Cube to 80 percent, otherwise it would be too large for the screen
		
		//Rotate around the axis based on the rotation matrix (rotation, x, y, z)
		gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);	//X
		gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);	//Y
				
		cube.draw(gl, filter);					//Draw the Cube
		
		gl.glScalef(0.5f, 0.5f, 0.5f); 			//Scale the Cube to 80 percent, otherwise it would be too large for the screen
		gl.glRotatef(yrot, 1.0f, 0.0f, 1.0f);	//X
		gl.glRotatef(xrot, 0.0f, 1.0f, 1.0f);	//Y

		gl.glTranslatef(orbitHeight, 0.0f, 0.0f);			//Move z units into the screen
		cube.draw(gl, filter);					//Draw the Cube	

		gl.glTranslatef(-orbitHeight*2, 0.0f, 0.0f);			//Move z units into the screen
		cube.draw(gl, filter);					//Draw the Cube	

		gl.glTranslatef(orbitHeight, orbitHeight, 0.0f);			//Move z units into the screen
		cube.draw(gl, filter);					//Draw the Cube	

		gl.glTranslatef(0.0f, -orbitHeight*2, 0.0f);			//Move z units into the screen
		cube.draw(gl, filter);					//Draw the Cube	

		//Change rotation factors
		xrot += xspeed;
		yrot += yspeed;
	}		

	/**
	 * If the surface changes, reset the view
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}

		gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
		gl.glLoadIdentity(); 					//Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity(); 					//Reset The Modelview Matrix
	}
	
/* ***** Listener Events ( NEW ) ***** */	
	/**
	 * Override the key listener to receive keyUp events.
	 * 
	 * Check for the DPad presses left, right, up, down and middle.
	 * Change the rotation speed according to the presses
	 * or change the texture filter used through the middle press.
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			yspeed -= 0.1f;
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			yspeed += 0.1f;
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			xspeed -= 0.1f;
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			xspeed += 0.1f;
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			filter += 1;
			if(filter > 2) {
				filter = 0;
			}
		}

		//We handled the event
		return true;
	}
		
	/**
	 * Override the touch screen listener.
	 * 
	 * React to moves and presses on the touchscreen.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (gestureScanner.onTouchEvent(event))
		{
			return true;
		}
    	int upperArea = this.getHeight() / 10;
    	int lowerArea = this.getHeight() - upperArea;
		float x = event.getX();
        float y = event.getY();
              
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
           break;
        case MotionEvent.ACTION_UP:
        	break;
        case MotionEvent.ACTION_POINTER_UP:
         	Log.d(TAG, "ACTION_POINTER_UP");
        	if (zooming) Log.d(TAG, "ZOOM Off");    		
        	zooming = false;
       	break;
        case MotionEvent.ACTION_POINTER_DOWN:
        	zoomStartDist = spacing(event);
        	if (zoomStartDist <= 10f) {
            	if (zooming) Log.d(TAG, "ZOOM Off" );
        		zooming = false;
        		break;
        	}
        	if (!zooming) Log.d(TAG, "ZOOM On" );
        	zooming = true;
        	zoomStartZ = z;
        	break;
        case MotionEvent.ACTION_MOVE:
        	//Calculate the change
        	float dx = x - oldX;
	        float dy = y - oldY;
        	
        	//Zoom in/out if the touch move has been made in the upper
        	if(zooming) {
        		float currentDist = spacing(event);
        		z = zoomStartZ * currentDist / zoomStartDist;
        	
        	//Rotate around the axis otherwise
        	} else {        		
    	        xrot += dy * TOUCH_SCALE;
    	        yrot += dx * TOUCH_SCALE;
         	}
        	break;
     }

        
       
        //Remember the values
        oldX = x;
        oldY = y;
        
        //We handled the event
		return true;
	}
	
	private float spacing(MotionEvent event) {
		   float x = event.getX(0) - event.getX(1);
		   float y = event.getY(0) - event.getY(1);
		   return FloatMath.sqrt(x * x + y * y);
		}

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	Log.d(TAG, "Fling vx=" + velocityX + " vy="+ velocityY);
		yspeed += velocityX / 10000;
		xspeed += velocityY / 10000;
		return true;
	}

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onSingleTapUp(MotionEvent e) {
    	Log.d(TAG, "TAP Up");
    	xspeed = 0.0f;
    	yspeed = 0.0f;
		return true;
	}
}
