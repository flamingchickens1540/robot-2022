package org.team1540.robot2022.commands.intake;

import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import org.team1540.robot2022.Constants;
import org.team1540.robot2022.InterpolationTable;
import org.team1540.robot2022.commands.indexer.Indexer;
import org.team1540.robot2022.commands.shooter.Shooter;

public class IntakeSequence extends SequentialCommandGroup {
    private final Intake intake;
    private final Indexer indexer;

    /**
     * Constructs an IntakeSequence that spins up the shooter when full
     * @param intake
     * @param indexer
     * @param shooter
     */
    public IntakeSequence(Intake intake, Indexer indexer, Shooter shooter) {
        this.intake = intake;
        this.indexer = indexer;
        addRequirements(intake, indexer, shooter);
        addCommands(
                new InstantCommand(() -> {
                    indexer.setStandby(false);
                    if (indexer.isFull()) {
                        indexer.set(Indexer.IndexerState.OFF, Indexer.IndexerState.OFF);
                        shooter.commandSetVelocity(InterpolationTable.shooterSpinupFront, InterpolationTable.shooterSpinupRear);
                    } else if (indexer.getTopSensor()) {
                        indexer.set(Indexer.IndexerState.OFF, Indexer.IndexerState.FORWARD);
                    } else {
                        indexer.set(Indexer.IndexerState.FORWARD, Indexer.IndexerState.FORWARD);
                    }
                }),
                new ConditionalCommand(
                        new InstantCommand(),
                        new IntakeSpinCommand(intake, indexer, Constants.IntakeConstants.SPEED),
                        indexer::isFull
                ),
                parallel(
                    shooter.commandSetVelocity(2500, 2500),
                    intake.commandSetFold(true)
                )
        );
    }

    /**
     * Constructs an IntakeSequence that does not spin up the shooter when full
     * @param intake
     * @param indexer
     */
    public IntakeSequence(Intake intake, Indexer indexer) {
        this.intake = intake;
        this.indexer = indexer;
        addRequirements(intake, indexer);
        addCommands(
                new InstantCommand(() -> {
                    indexer.setStandby(false);
                    if (indexer.isFull()) {
                        indexer.set(Indexer.IndexerState.OFF, Indexer.IndexerState.OFF);
                    } else if (indexer.getTopSensor()) {
                        indexer.set(Indexer.IndexerState.OFF, Indexer.IndexerState.FORWARD);
                    } else {
                        indexer.set(Indexer.IndexerState.FORWARD, Indexer.IndexerState.FORWARD);
                    }
                }),
                new ConditionalCommand(
                        new InstantCommand(),
                        new IntakeSpinCommand(intake, indexer, Constants.IntakeConstants.SPEED),
                        indexer::isFull
                ),
                intake.commandSetFold(true)
        );
    }

    @Override
    public void end(boolean isInterrupted) {
        intake.stop();
        indexer.stop();
    }
}
