// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package org.team1540.robot2022;

import com.ctre.phoenix.motorcontrol.NeutralMode;

import org.team1540.robot2022.commands.drivetrain.DriveTrain;
import org.team1540.robot2022.commands.drivetrain.TankDriveCommand;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.XboxController.Axis;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;


//import org.team1540.robot2022.Commands.Intake.Intake;
import org.team1540.robot2022.commands.Shooter.ShooterManualControl;
import org.team1540.robot2022.commands.Shooter.ShooterTesting;
//import org.team1540.robot2022.PneumaticsTest;

/**
* The VM is configured to automatically run this class, and to call the functions corresponding to
* each mode, as described in the TimedRobot documentation. If you change the name of this class or
* the package after creating this project, you must also update the build.gradle file in the
* project.
*/

public class Robot extends TimedRobot {
  private XboxController pilot = new XboxController(0);
  private ShooterTesting shooter = new ShooterTesting();
  //ShooterManualControl shooterManualControl = new ShooterManualControl(shooter, pilot); 
  ShooterManualControl shooterManualControl = new ShooterManualControl(shooter, pilot); 

    private RobotContainer robotContainer;
    private DriveTrain driveTrain;
    private XboxController driverXbox;

    private Command autonomousCommand;

    /**
     * This function is run when the robot is first started up and should be used
     * for any
     * initialization code.
     */
    @Override
    public void robotInit() {
        // Instantiate our RobotContainer. This will perform all our button bindings,
        // and put our
        // autonomous chooser on the dashboard.
        this.robotContainer = new RobotContainer();
        this.driveTrain = robotContainer.driveTrain;
        this.driverXbox = robotContainer.driverController;
    }

    /**
     * This function is called every robot packet, no matter the mode. Use this for
     * items like
     * diagnostics that you want ran during disabled, autonomous, teleoperated and
     * test.
     *
     * <p>
     * This runs after the mode specific periodic functions, but before LiveWindow
     * and
     * SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        // Runs the Scheduler. This is responsible for polling buttons, adding
        // newly-scheduled
        // commands, running already-scheduled commands, removing finished or
        // interrupted commands,
        // and running subsystem periodic() methods. This must be called from the
        // robot's periodic
        // block in order for anything in the Command-based framework to work.
        CommandScheduler.getInstance().run();
    }

    /** This function is called once each time the robot enters Disabled mode. */
    @Override
    public void disabledInit() {
    }

    @Override
    public void disabledPeriodic() {
    }

    @Override
    public void autonomousInit() {
        driveTrain.setNeutralMode(NeutralMode.Brake);
        autonomousCommand = robotContainer.getAutonomousCommand();
        if (autonomousCommand != null) {
            autonomousCommand.schedule();
        }
    }

    /** This function is called periodically during autonomous. */
    @Override
    public void autonomousPeriodic() {
    }

    @Override
    public void teleopInit() {
        if (autonomousCommand != null) {
            autonomousCommand.cancel();
        }
        driveTrain.setNeutralMode(NeutralMode.Brake);
        driveTrain.setDefaultCommand(new TankDriveCommand(driveTrain, driverXbox));
    }

    /** This function is called periodically during operator control. */
    @Override
    public void teleopPeriodic() {
      System.out.println("running periodic"); 
    shooter.setDefaultCommand(shooterManualControl);
    }


  }
