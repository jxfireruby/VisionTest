package org.usfirst.frc.team5401.robot;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.vision.VisionThread;
import org.usfirst.frc.team5401.robot.YellowWaterBottleGripPipeline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import edu.wpi.first.wpilibj.vision.VisionPipeline;

import org.opencv.core.*;
import org.opencv.core.Core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;
import org.opencv.objdetect.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	
	
	private static final String WEBCAM_PATH = "/dev/video0";

	public volatile double centerX = 0.0;
	public volatile double centerY = 0.0;
	
	public final Object visionLock = new Object();
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		chooser.addDefault("Default Auto", defaultAuto);
		chooser.addObject("My Auto", customAuto);
		SmartDashboard.putData("Auto choices", chooser);
		
		
		UsbCamera camera = CameraServer.getInstance().startAutomaticCapture("My Webcam", WEBCAM_PATH);
		camera.setResolution(640,480);
		VisionThread visionThread = new VisionThread(camera, new YellowWaterBottleGripPipeline(), pipeline -> {
			ArrayList<MatOfPoint> contours = pipeline.filterContoursOutput();
			if(contours.size() != 0){
				for(MatOfPoint singleContour : contours){
					//This for loop runs the amount of time equal to how many elements are in the array contours
					//Each time this for loop runs, the singleContour is equal to an element in the contours array and keeps changing with each loop run
					Rect boundingBox = Imgproc.boundingRect(singleContour);
					double centerX = (boundingBox.x) + (boundingBox.width/2);
					double centerY = (boundingBox.y) + (boundingBox.height/2);
					synchronized (visionLock) {
						this.centerX = centerX;
						this.centerY = centerY;
					}
					
				}
			}
		});
		visionThread.start();
		
		double centerX, centerY;
		synchronized(visionLock){
			centerX = this.centerX;
			centerY = this.centerY;
		}
		
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		autoSelected = chooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
		switch (autoSelected) {
		case customAuto:
			// Put custom auto code here
			break;
		case defaultAuto:
		default:
			// Put default auto code here
			break;
		}
	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	}
}

