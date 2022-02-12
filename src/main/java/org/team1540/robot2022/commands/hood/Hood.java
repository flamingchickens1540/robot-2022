package org.team1540.robot2022.commands.hood;

import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import org.team1540.robot2022.Constants;

public class Hood extends SubsystemBase {
    private final Solenoid solenoid = new Solenoid(PneumaticsModuleType.REVPH, Constants.HoodConstants.solenoidChannel);

    public Hood() {
    }

    public void set(Boolean state) {
        solenoid.set(state);
    }
}
