package org.team1540.robot2022.commands.climber;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import org.team1540.robot2022.Constants;
import org.team1540.robot2022.utils.ChickenShuffleboard;

/**
 * Moves the climber down until the current spikes, then zeroes it
 */
public class ClimberZeroCommand extends SequentialCommandGroup {
    private final Climber climber;

    public ClimberZeroCommand(Climber climber) {
        this.climber = climber;
        addRequirements(climber);
        addCommands(
                climber.commandDisableLimits(),
                parallel(
                        // Zero left climber
                        sequence(
                                climber.commandSetPercentLeft(Constants.ClimberConstants.ZERO_DOWN_SPEED),
                                new WaitUntilCommand(() -> climber.getLeftCurrent() > ChickenShuffleboard.ClimberTab.currentSpike.getDouble(Constants.ClimberConstants.ZERO_SPIKE_CURRENT)),
                                climber.commandSetPercentLeft(0)
                        ),

                        // Zero right climber
                        sequence(
                                climber.commandSetPercentRight(Constants.ClimberConstants.ZERO_DOWN_SPEED),
                                new WaitUntilCommand(() -> climber.getRightCurrent() > ChickenShuffleboard.ClimberTab.currentSpike.getDouble(Constants.ClimberConstants.ZERO_SPIKE_CURRENT)),
                                climber.commandSetPercentRight(0)
                        )
                ),
                climber.commandZeroEncoders(),
                climber.commandUpdateLimits()
        );
    }

    @Override
    public void end(boolean isInterrupted) {
        climber.stop();
    }
}
