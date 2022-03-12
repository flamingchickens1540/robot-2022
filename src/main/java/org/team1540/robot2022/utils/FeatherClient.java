package org.team1540.robot2022.utils;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import org.team1540.robot2022.commands.util.UpdateMatchInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class FeatherClient {
    private static final Timer timer = new Timer();
//    private static final Mat mat = new Mat();

    private static ShootingParameters lastShot;
    private static boolean isLastShotFirstBall; // else second ball

    private static String matchDirectoryPrefix;

    /**
     * Attempt to update matchId when the robot receives the FMS matchInfo packet
     *
     * @return true if the update was successful
     */
    public static boolean updateMatchId() {
        if (DriverStation.getMatchType() != DriverStation.MatchType.None) {
            String matchId = String.format("%s-%dr%d-%s%d",
                    DriverStation.getEventName(),
                    DriverStation.getMatchNumber(),
                    DriverStation.getReplayNumber(),
                    DriverStation.getAlliance() + "",
                    DriverStation.getLocation());

            // Write shot log to file
            try {
                Files.writeString(Paths.get(matchDirectoryPrefix + "/matchId"), matchId + System.lineSeparator(), StandardOpenOption.CREATE);
                return true;
            } catch (IOException e) {
                DriverStation.reportError("[feather] Unable to create matchId file: " + e, true);
            }
        }
        return false;
    }

    /**
     * Initialize a feather match. This should be called in autonomousInit.
     * - Resets the match timer
     * - Creates the directory structure
     * - Schedules the match info update command
     */
    public static void initialize() {
        timer.reset();
        timer.start();

        matchDirectoryPrefix = "/home/lvuser/feather/matches/" + UUID.randomUUID();

        // Create match directory
        File directory = new File(matchDirectoryPrefix);
        if (!directory.exists()) directory.mkdirs();

        // Create shot log file
        try {
            Files.writeString(Paths.get(matchDirectoryPrefix + "/shots.jsonl"), "", StandardOpenOption.CREATE);
        } catch (IOException e) {
            DriverStation.reportError("[feather] Unable to create shot log file: " + e, true);
        }

        new UpdateMatchInfo().schedule();
    }

    /**
     * Get elapsed timer seconds
     *
     * @return timer value in seconds
     */
    public static double getTimer() {
        return timer.get();
    }

    /**
     * Record a shot and result to a local file
     *
     * @param shot shooting parameters
     */
    private static void commitShot(ShootingParameters shot) {
        String jsonString = String.format("{\"id\": \"%s\", \"timer\": %f, " +
                        "\"limelightDistance\": %f, \"lidarDistance\": %f, " +
                        "\"frontRPM\": %f, \"rearRPM\": %f, \"hoodUp\": %b, \"profile\": \"%s\", " +
                        "\"firstResult\": %s, \"secondResult\": %s}",
                shot.uuid, shot.matchSeconds,
                shot.limelightDistance, shot.lidarDistance,
                shot.frontRPM, shot.rearRPM, shot.hoodUp, shot.profile + "",
                shot.firstBall + "", shot.secondBall + ""
        );

        // Write shot log to file
//        lastShot = null;
        try {
            Files.writeString(Paths.get(matchDirectoryPrefix + "/shots.jsonl"), jsonString + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            DriverStation.reportError("[feather] Unable to append to shot log file: " + e, true);
        }

    }

    /**
     * Record a shot
     *
     * @param parameters shooting parameters
     */
    private static void recordShot(ShootingParameters parameters) {
        if (lastShot != null) { // If the last shot hasn't been committed yet, commit it with unknown results
            lastShot.firstBall = ShotResult.UNKNOWN;
            lastShot.secondBall = ShotResult.UNKNOWN;
            commitShot(lastShot);
        }
        lastShot = parameters;
    }

    /**
     * Set the last shot's result
     *
     * @param result what happened to the ball?
     */
    private static void setLastShotResult(ShotResult result) {
        if (lastShot != null) { // If the last shot's shooting parameters have been recorded...
            if (lastShot.firstBall == null) { // If the first ball's result hasn't been recorded yet, record the result
                isLastShotFirstBall = true;
                lastShot.firstBall = result;
            } else if (lastShot.secondBall == null) { // If the second ball's result hasn't been recorded yet, record the result and commit the shot
                isLastShotFirstBall = false;
                lastShot.secondBall = result;

                commitShot(lastShot);
                lastShot = null;
            }
            // If both balls have been recorded already, ignore this shot result
        }
    }

    /**
     * Undoes the last shot result
     */
    private static void undoSetLastShotResult() {
        if (lastShot != null) {
            if (isLastShotFirstBall) {
                lastShot.firstBall = null;
            } else {
                lastShot.secondBall = null;
            }
        }
    }

    /**
     * Return an instant command that records a shot for the feather backend
     *
     * @param limelightDistance distance from limelight
     * @param lidarDistance     distance from LIDAR
     * @param frontRPM          front RPM setpoint
     * @param rearRPM           rear RPM setpoint
     * @param hoodUp            is the hood up?
     * @param profile           shooter profile
     * @return InstantCommand to record a shot
     */
    public static Command commandRecordShot(double limelightDistance, double lidarDistance,
                                            double frontRPM, double rearRPM, boolean hoodUp,
                                            String profile) {
        return new InstantCommand(() -> recordShot(new ShootingParameters(limelightDistance, lidarDistance, frontRPM, rearRPM, hoodUp, profile)));
    }

    /**
     * What happened to the ball after it left the robot?
     */
    public enum ShotResult {
        /**
         * Shot went in
         */
        OK,

        /**
         * Shot went too far
         */
        OVER,

        /**
         * Shot didn't go far enough
         */
        UNDER,

        /**
         * Shot bounced out
         */
        BOUNCED,

        /**
         * We don't know what happened
         */
        UNKNOWN
    }

    /**
     * Stores shooting configuration values, raw sensor outputs, and ball results
     */
    private static class ShootingParameters {
        public double limelightDistance;
        public double lidarDistance;
        public double frontRPM;
        public double rearRPM;
        public boolean hoodUp;
        public String profile;

        public double matchSeconds;
        public ShotResult firstBall;
        public ShotResult secondBall;

        public String uuid;

        public ShootingParameters(double limelightDistance, double lidarDistance,
                                  double frontRPM, double rearRPM, boolean hoodUp,
                                  String profile) {
            this.limelightDistance = limelightDistance;
            this.lidarDistance = lidarDistance;
            this.frontRPM = frontRPM;
            this.rearRPM = rearRPM;
            this.hoodUp = hoodUp;
            this.profile = profile;

            this.matchSeconds = getTimer();
            this.uuid = UUID.randomUUID() + "";

            // Write limelight image
//            CameraServer.getVideo("limelight").grabFrame(mat);
//            Imgcodecs.imwrite(matchDirectoryPrefix + "/limelight-" + this.uuid + ".png", mat);
        }
    }

    /**
     * Configures button bindings on an xbox controller
     *
     * @param controller xbox controller
     */
    public static void configureController(XboxController controller) {
        // coop:button(Y,Over,pilot)
        new JoystickButton(controller, XboxController.Button.kY.value)
                .whenPressed(new InstantCommand(() -> setLastShotResult(ShotResult.OVER)));
        // coop:button(X,OK,pilot)
        new JoystickButton(controller, XboxController.Button.kX.value)
                .whenPressed(new InstantCommand(() -> setLastShotResult(ShotResult.OK)));
        // coop:button(B,Bounced,pilot)
        new JoystickButton(controller, XboxController.Button.kB.value)
                .whenPressed(new InstantCommand(() -> setLastShotResult(ShotResult.BOUNCED)));
        // coop:button(A,Under,pilot)
        new JoystickButton(controller, XboxController.Button.kA.value)
                .whenPressed(new InstantCommand(() -> setLastShotResult(ShotResult.UNDER)));
        // coop:button(Start,Unknown,pilot)
        new JoystickButton(controller, XboxController.Button.kStart.value)
                .whenPressed(new InstantCommand(() -> setLastShotResult(ShotResult.UNKNOWN)));
        // coop:button(Back,Undo,pilot)
        new JoystickButton(controller, XboxController.Button.kBack.value)
                .whenPressed(new InstantCommand(FeatherClient::undoSetLastShotResult));
    }
}
