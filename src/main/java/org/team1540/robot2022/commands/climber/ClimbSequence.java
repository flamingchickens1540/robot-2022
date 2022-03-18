package org.team1540.robot2022.commands.climber;

import edu.wpi.first.wpilibj2.command.*;
import org.team1540.robot2022.utils.NavX;

public class ClimbSequence extends SequentialCommandGroup {
    private static final double SWING_THRESHOLD = 5;
    private Climber climber;
    private NavX navx;
    private double lastPitch;

    public ClimbSequence(Climber climber, NavX navx) {
        this.climber = climber;
        this.navx = navx;
        addCommands(
                // Mid Bar
                runUntilSpike(0.5, 35), // Retract arms as far as they can go
                new WaitCommand(0.2),

                // High Bar
                climber.commandSetPercent(-0.5), // Start extending arms (static hooks attached)
                new WaitCommand(0.2),
                new InstantCommand(() -> climber.setSolenoids(true)), // Retract arm pnuematics

                raiseUntilLimit(-0.8),                                // Raise arms to maximum height
                new WaitUntilCommand(this::isOptimalSwing),           // Wait for robot to be swinging in the right place
                new InstantCommand(() -> climber.setSolenoids(false)),// Move arms to hit high bar
                new WaitCommand(1),                                   // Wait for arms to finish moving TODO can we lower this?
                runUntilSpike(0.5, 35),                                // Retract the arms as far as they can go

                // Traversal Bar
                climber.commandSetPercent(-0.5), // Start extending arms (static hooks attached)
                new WaitCommand(0.2),
                new InstantCommand(() -> climber.setSolenoids(true)), // Retract arm pnuematics

                raiseUntilLimit(-0.8),                                // Raise arms to maximum height
                new WaitUntilCommand(this::isOptimalSwing),           // Wait for robot to be swinging in the right place
                new InstantCommand(() -> climber.setSolenoids(false)),// Move arms to hit traversal bar
                new WaitCommand(1),                                   // Wait for arms to finish moving TODO can we lower this?
                runUntilSpike(0.5, 35)                                // Retract the arms as far as they can go

        );
        addRequirements(climber);
    }

    @Override
    public void end(boolean interrupted) {
        climber.stop();
    }

    /**
     * Returns if the robot is at an optimal swing for transfering bars.
     *
     * @return true if the robot is at an optimal angle and swinging the right direction
     */
    private boolean isOptimalSwing() {
        if (navx.getPitch() - lastPitch > 0 && Math.abs(navx.getPitch() - SWING_THRESHOLD) > 5) { //Check if robot is swinging the right way TODO make sure swing isn't inverted, tune threshold and range.
            lastPitch = navx.getPitch();
            return true;
        }
        lastPitch = navx.getPitch();
        return false;
    }

    private CommandGroupBase runUntilSpike(double speed, double spikeCurrent) {
        return parallel(
                sequence(
                        climber.commandSetPercentLeft(speed),
                        new WaitUntilCommand(() -> climber.getLeftCurrent() > spikeCurrent),
                        climber.commandSetPercentLeft(0)
                ),
                sequence(
                        climber.commandSetPercentRight(speed),
                        new WaitUntilCommand(() -> climber.getRightCurrent() > spikeCurrent),
                        climber.commandSetPercentRight(0)
                ));
    }

    private CommandGroupBase raiseUntilLimit(double speed) {
        return parallel(
                sequence(
                        climber.commandSetPercentLeft(speed),
                        new WaitUntilCommand(climber.motorLeft::getReverseSoftLimitReached),
                        climber.commandSetPercentLeft(0)
                ),
                sequence(
                        climber.commandSetPercentRight(speed),
                        new WaitUntilCommand(climber.motorRight::getReverseSoftLimitReached),
                        climber.commandSetPercentRight(0)
                )
        );
    }
}
