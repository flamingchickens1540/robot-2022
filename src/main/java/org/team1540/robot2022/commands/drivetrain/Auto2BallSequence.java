package org.team1540.robot2022.commands.drivetrain;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import org.team1540.robot2022.commands.hood.Hood;
import org.team1540.robot2022.commands.indexer.Indexer;
import org.team1540.robot2022.commands.intake.Intake;
import org.team1540.robot2022.commands.shooter.ShootSequence;
import org.team1540.robot2022.commands.shooter.Shooter;
import org.team1540.robot2022.utils.AutoHelper;
import org.team1540.robot2022.utils.LIDAR;
import org.team1540.robot2022.utils.Limelight;

public class Auto2BallSequence extends SequentialCommandGroup {

    /**
     * Constructs a new Auto2Ball Sequence
     *
     * @param drivetrain The drivetrain subsystem (For driving)
     * @param intake     The intake subsystem (For collecting balls)
     * @param indexer    The indexer subsystem (For collecting balls and ShootSequence)
     * @param shooter    The shooter subsystem (For ShootSequence)
     * @param limelight  The limelight (For PointToTarget)
     * @param isPosA     If the robot is positioned in the upper right or bottom left starting position
     */
    public Auto2BallSequence(Drivetrain drivetrain, Intake intake, Indexer indexer, Shooter shooter, Hood hood, Limelight limelight, LIDAR lidar, boolean isPosA) {
        String trajectoryFile = "2ball.pos" + (isPosA ? "A" : "B") + ".path1.wpilib.json";
        addCommands(
                AutoHelper.runPath(drivetrain, intake, indexer, shooter, trajectoryFile),  // Follow the path to collect the first ball
                new ShootSequence(shooter, indexer, drivetrain, hood, intake, limelight, lidar, Shooter.ShooterProfile.FAR, false)         // Shoot the 2 indexed balls (starts with one, collects one)
        );
    }
}
