/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.team1678.frc2020;

import java.util.Optional;

import com.team1678.frc2020.auto.AutoModeExecutor;
import com.team1678.frc2020.auto.modes.AutoModeBase;
import com.team1678.frc2020.controlboard.ControlBoard;
import com.team1678.frc2020.loops.Looper;
import com.team1678.frc2020.paths.TrajectoryGenerator;
import com.team1678.frc2020.controlboard.ControlBoard;
import com.team1678.frc2020.controlboard.GamepadButtonControlBoard;
import com.team1678.frc2020.controlboard.GamepadButtonControlBoard.TurretCardinal;
import com.team1678.frc2020.logger.LoggingSystem;
import com.team254.lib.wpilib.TimedRobot;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.team1678.frc2020.SubsystemManager;
import com.team1678.frc2020.subsystems.*;
import com.team254.lib.util.*;
import com.team254.lib.wpilib.TimedRobot;

import java.util.Optional;

import com.team1678.frc2020.SubsystemManager;
import com.team1678.frc2020.subsystems.*;
import com.team254.lib.util.*;
import com.team254.lib.vision.AimingParameters;
import com.team254.lib.geometry.Rotation2d;
import com.team1678.frc2020.subsystems.Indexer.WantedAction;
import com.team254.lib.geometry.Pose2d;
import com.team254.lib.geometry.Rotation2d;
import com.team254.lib.util.CrashTracker;
import com.team254.lib.wpilib.TimedRobot;

import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    /**
     * This function is run when the robot is first started up and should be used
     * for any initialization code.
     */

    private final Looper mEnabledLooper = new Looper();
    private final Looper mDisabledLooper = new Looper();

    private final ControlBoard mControlBoard = ControlBoard.getInstance();
    private TrajectoryGenerator mTrajectoryGenerator = TrajectoryGenerator.getInstance();

    private final SubsystemManager mSubsystemManager = SubsystemManager.getInstance();
    private final Drive mDrive = Drive.getInstance();
    private final Indexer mIndexer = Indexer.getInstance();
    private final Infrastructure mInfrastructure = Infrastructure.getInstance();
    private final Limelight mLimelight = Limelight.getInstance();

    private final Intake mIntake = Intake.getInstance();
    private final Superstructure mSuperstructure = Superstructure.getInstance();
    private final Turret mTurret = Turret.getInstance();
    private final Shooter mShooter = Shooter.getInstance();
    private final Trigger mTrigger = Trigger.getInstance();
    private final Climber mClimber = Climber.getInstance();
    private final Hood mHood = Hood.getInstance();
    private final Wrangler mWrangler = Wrangler.getInstance();

    //private final Roller mRoller = Roller.getInstance();
    private final Canifier mCanifier = Canifier.getInstance();
    private final LEDs mLEDs = LEDs.getInstance();

    private final RobotState mRobotState = RobotState.getInstance();
    private final RobotStateEstimator mRobotStateEstimator = RobotStateEstimator.getInstance();
    private boolean climb_mode = false;
    private boolean buddy_climb = false;
    private AutoModeExecutor mAutoModeExecutor;
    private AutoModeSelector mAutoModeSelector = new AutoModeSelector();

    // private LoggingSystem mLogger = LoggingSystem.getInstance();

    public Robot() {
        CrashTracker.logRobotConstruction();
        mTrajectoryGenerator.generateTrajectories();
    }

    @Override
    public void robotPeriodic() {
        RobotState.getInstance().outputToSmartDashboard();
        mSubsystemManager.outputToSmartDashboard();
        mAutoModeSelector.outputToSmartDashboard();
        mEnabledLooper.outputToSmartDashboard();

        SmartDashboard.putBoolean("Climb Mode", climb_mode);
    }

    @Override
    public void robotInit() {
        try {
            UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
            camera.setVideoMode(VideoMode.PixelFormat.kMJPEG, 320, 240, 15);
            MjpegServer cameraServer = new MjpegServer("serve_USB Camera 0", Constants.kCameraStreamPort);
            cameraServer.setSource(camera);

            CrashTracker.logRobotInit();

            mSubsystemManager.setSubsystems(
                mRobotStateEstimator,
                mCanifier,
                mDrive, 
                mHood,
                mLimelight, 
                mIntake, 
                mIndexer, 
                mWrangler, 
                mShooter,
                mTrigger,
                mSuperstructure,
                mTurret,
                mInfrastructure,
                mClimber,
                //mRoller,
                mLEDs
            );

            mSubsystemManager.registerEnabledLoops(mEnabledLooper);
            mSubsystemManager.registerDisabledLoops(mDisabledLooper);

            // Robot starts forwards.
            mRobotState.reset(Timer.getFPGATimestamp(), Pose2d.identity());
            mDrive.setHeading(Rotation2d.identity());

            mLimelight.setLed(Limelight.LedMode.OFF);

            mTrajectoryGenerator.generateTrajectories();
        } catch (Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }

    @Override
    public void autonomousInit() {
        SmartDashboard.putString("Match Cycle", "AUTONOMOUS");

        try {
            CrashTracker.logAutoInit();
            mDisabledLooper.stop();
            mLimelight.setLed(Limelight.LedMode.ON);

            RobotState.getInstance().reset(Timer.getFPGATimestamp(), Pose2d.identity());

            Drive.getInstance().zeroSensors();
            mTurret.setNeutralMode(NeutralMode.Brake);
            mHood.setNeutralMode(NeutralMode.Brake);
            mInfrastructure.setIsDuringAuto(true);

            mAutoModeExecutor.start();

            mEnabledLooper.start();
        } catch (Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }

    @Override
    public void autonomousPeriodic() {
        SmartDashboard.putString("Match Cycle", "AUTONOMOUS");
        mLimelight.setLed(Limelight.LedMode.ON);

        if (!mLimelight.limelightOK()) {
            mLEDs.conformToState(LEDs.State.EMERGENCY);
        } else if (mSuperstructure.isOnTarget()) {
            mLEDs.conformToState(LEDs.State.TARGET_TRACKING);
        } else if (mSuperstructure.getLatestAimingParameters().isPresent()) {
            mLEDs.conformToState(LEDs.State.TARGET_VISIBLE);
        } else {
            mLEDs.conformToState(LEDs.State.ENABLED);
        }

        try {

        } catch (Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }

    @Override
    public void teleopInit() {
        try {
            CrashTracker.logTeleopInit();
            mDisabledLooper.stop();
            mClimber.setBrakeMode(true);


            if (mAutoModeExecutor != null) {
                mAutoModeExecutor.stop();
            }

            mInfrastructure.setIsDuringAuto(false);

            //mRobotState.reset(Timer.getFPGATimestamp(), Pose2d.identity());
            mEnabledLooper.start();
            mLimelight.setLed(Limelight.LedMode.ON);
            mLimelight.setPipeline(Constants.kPortPipeline);
            mTurret.setNeutralMode(NeutralMode.Brake);
            mHood.setNeutralMode(NeutralMode.Brake);
            mLEDs.conformToState(LEDs.State.ENABLED);
            
            mControlBoard.reset();
        } catch (Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }
    
    @Override
    public void teleopPeriodic() {
        try {
            double timestamp = Timer.getFPGATimestamp();
            double throttle = mControlBoard.getThrottle();
            double turn = mControlBoard.getTurn();
            double hood_jog = mControlBoard.getJogHood();
            double turret_jog = mControlBoard.getJogTurret();

            if (!mLimelight.limelightOK()) {
                mLEDs.conformToState(LEDs.State.EMERGENCY);
            } else if (mSuperstructure.getTucked()) {
                mLEDs.conformToState(LEDs.State.HOOD_TUCKED);
            } else if (mSuperstructure.isOnTarget()) {
                mLEDs.conformToState(LEDs.State.TARGET_TRACKING);
            } else if (mSuperstructure.getLatestAimingParameters().isPresent()) {
                mLEDs.conformToState(LEDs.State.TARGET_VISIBLE);
            } else {
                mLEDs.conformToState(LEDs.State.ENABLED);
            }

            mDrive.setCheesyishDrive(throttle, turn, mControlBoard.getQuickTurn());

            mLimelight.setLed(Limelight.LedMode.ON);
            TurretCardinal cardinal = mControlBoard.getTurretCardinal();
            
            
            if (Math.abs(turret_jog) > Constants.kJoystickJogThreshold) {
                turret_jog = (turret_jog - Math.signum(turret_jog) * Constants.kJoystickJogThreshold)
                        / (1.0 - Constants.kJoystickJogThreshold);

                mSuperstructure.jogTurret(turret_jog * 3);
            } else if (mControlBoard.getFendorShot()) {
                mSuperstructure.setGoal(1000, 35, 180);
            } else if (cardinal == TurretCardinal.NONE) {
                mSuperstructure.setWantAutoAim(Rotation2d.fromDegrees(180.0));
            } else {
                mSuperstructure.setWantFieldRelativeTurret(cardinal.rotation);
            }
            // mSuperstructure.setWantFieldRelativeTurret(Rotation2d.fromDegrees(180.0));//mControlBoard.getTurretCardinal().rotation);

            if (mControlBoard.climbMode()) {
                climb_mode = true;
            }

            if (!climb_mode){ //TODO: turret preset stuff and jog turret and rumbles
                mSuperstructure.enableIndexer(true);
                mWrangler.setState(Wrangler.WantedAction.RETRACT);

                if (mIndexer.slotsFilled()) {
                    mControlBoard.setRumble(false);
                } else {
                    mControlBoard.setRumble(false);
                }

                if (mControlBoard.getShoot()) {
                    if (mSuperstructure.isAimed()) {
                        mSuperstructure.setWantShoot();
                    }
                } else if (mControlBoard.getPreShot()) {
                    mSuperstructure.setWantPreShot(true);
                } else if (mControlBoard.getSpinUp()) {
                    mSuperstructure.setWantSpinUp();
                } else if (mControlBoard.getTuck()) {
                    mSuperstructure.setWantTuck(true);
                } else if (mControlBoard.getUntuck()) {
                    mSuperstructure.setWantTuck(false);
                } else if (mControlBoard.getTestSpit()) {
                    //mSuperstructure.setWantTestSpit();
                    mRobotState.reset(Timer.getFPGATimestamp(), Pose2d.identity());
                } else if (mControlBoard.getRunIntake()) {
                    if (!mSuperstructure.getWantShoot()) {
                        mIntake.setState(Intake.WantedAction.INTAKE);
                    }
                    mSuperstructure.setAutoIndex(false);
                } else if (mControlBoard.getRetractIntake()) {
                    mIntake.setState(Intake.WantedAction.RETRACT);
                } else if (mControlBoard.getControlPanelRotation()) {
                    //mRoller.setState(Roller.WantedAction.ACHIEVE_POSITION_CONTROL);
                } else if (mControlBoard.getControlPanelPosition()) {
                    //mRoller.setState(Roller.WantedAction.ACHIEVE_POSITION_CONTROL);
                } else {
                    mIntake.setState(Intake.WantedAction.NONE);
                }
            } else {
                mSuperstructure.enableIndexer(false);
                mIntake.setState(Intake.WantedAction.NONE);
                buddy_climb = mWrangler.getWranglerOut();
                if (mControlBoard.getArmExtend()) { // Press A
                    mClimber.setState(Climber.WantedAction.PIVOT);
                } else if (mControlBoard.getStopExtend()) {
                    mClimber.setState(Climber.WantedAction.STOP);
                } else if (mControlBoard.getArmHug()) { // Press B
                    mClimber.setState(Climber.WantedAction.HUG); // hook onto the rung
                } else if (mControlBoard.getBuddyDeploy()) { // Press Back
                    mWrangler.setState(Wrangler.WantedAction.DEPLOY);
                } else if (mControlBoard.getWrangle()) { // Press and hold X
                    mWrangler.setState(Wrangler.WantedAction.WRANGLE);
                    buddy_climb = true;
                } else if (mControlBoard.getClimb()) { // Press Y
                    mClimber.setState(Climber.WantedAction.CLIMB);
                } else if (mControlBoard.getManualArmExtend()) { // Press and hold left joystick
                    mClimber.setState(Climber.WantedAction.MANUAL_EXTEND);
                } else if (mControlBoard.getManualArmRetract()) { // Press and hold right joystick
                    mClimber.setState(Climber.WantedAction.MANUAL_CLIMB);
                } else if (mControlBoard.getBrake()) { // Release Y
                    mClimber.setState(Climber.WantedAction.BRAKE);
                } else if (mControlBoard.getLeaveClimbMode()) {
                    climb_mode = false;
                    buddy_climb = false;
                } else {
                    mWrangler.setState(Wrangler.WantedAction.NONE);
                    mClimber.setState(Climber.WantedAction.NONE);
                }

                if (mClimber.getState() == Climber.State.PIVOTING || mClimber.getState() == Climber.State.EXTENDING) {
                    mLEDs.conformToState(buddy_climb ? LEDs.State.EXTENDING_BUDDY : LEDs.State.EXTENDING);
                } else if (mClimber.getState() == Climber.State.HUGGING) {
                    mLEDs.conformToState(buddy_climb ? LEDs.State.HUGGING_BUDDY : LEDs.State.HUGGING);
                } else if (mClimber.getState() == Climber.State.CLIMBING) {
                    mLEDs.conformToState(buddy_climb ? LEDs.State.CLIMBING_BUDDY : LEDs.State.CLIMBING);
                } else {
                    mLEDs.conformToState(buddy_climb ? LEDs.State.CLIMB_MODE_BUDDY : LEDs.State.CLIMB_MODE);
                }
            }
            mLEDs.writePeriodicOutputs();
        } catch (Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }

    @Override
    public void testInit() {
        SmartDashboard.putString("Match Cycle", "TEST");

        try {
            System.out.println("Starting check systems.");

            mDisabledLooper.stop();
            mEnabledLooper.stop();

            mDrive.checkSystem();

        } catch (Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }

    @Override
    public void testPeriodic() {
    }

    @Override
    public void disabledInit() {
        try {
            CrashTracker.logDisabledInit();
            mEnabledLooper.stop();
            mClimber.setBrakeMode(true);
            if (mAutoModeExecutor != null) {
                mAutoModeExecutor.stop();
            }

            mInfrastructure.setIsDuringAuto(true);

            Drive.getInstance().zeroSensors();
            RobotState.getInstance().reset(Timer.getFPGATimestamp(), Pose2d.identity());

            // Reset all auto mode state.
            mAutoModeSelector.reset();
            mAutoModeSelector.updateModeCreator();
            mAutoModeExecutor = new AutoModeExecutor();

            mDisabledLooper.start();

            mLimelight.setLed(Limelight.LedMode.ON);
            mLimelight.triggerOutputs();

            mTurret.setNeutralMode(NeutralMode.Coast);
            mHood.setNeutralMode(NeutralMode.Coast);
            mDrive.setBrakeMode(false);
            mLimelight.writePeriodicOutputs();
            mLEDs.conformToState(LEDs.State.RAINBOW);
        } catch (Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }

    @Override
    public void disabledPeriodic() {
        SmartDashboard.putString("Match Cycle", "DISABLED");

        // mLimelight.setStream(2);

        try {
            mLimelight.setLed(Limelight.LedMode.OFF);
            mLimelight.writePeriodicOutputs();

            if (!mLimelight.limelightOK()) {
                mLEDs.conformToState(LEDs.State.EMERGENCY);
            } else if (mTurret.isHoming()) {
                mLEDs.conformToState(LEDs.State.RAINBOW);
            } else {
                mLEDs.conformToState(LEDs.State.BREATHING_PINK);
            }

            mAutoModeSelector.updateModeCreator();

            Optional<AutoModeBase> autoMode = mAutoModeSelector.getAutoMode();
            if (autoMode.isPresent() && autoMode.get() != mAutoModeExecutor.getAutoMode()) {
                System.out.println("Set auto mode to: " + autoMode.get().getClass().toString());
                mAutoModeExecutor.setAutoMode(autoMode.get());
            }

            mLEDs.writePeriodicOutputs();

        } catch (Throwable t) {
            CrashTracker.logThrowableCrash(t);
            throw t;
        }
    }
}
