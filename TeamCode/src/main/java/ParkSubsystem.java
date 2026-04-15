import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;

public class ParkSubsystem implements Subsystem {
    public static final ParkSubsystem INSTANCE = new ParkSubsystem();
    private ParkSubsystem() { }

    private static double maxPower = 1;

    private static double parkPower = 0.3;

    public LambdaCommand beginParkMode = new LambdaCommand()
            .setStart(() -> {
                follower().setMaxPower(parkPower);
                follower().startTeleopDrive(true);
            });

    public LambdaCommand endParkMode = new LambdaCommand().setStart(() -> {
        follower().setMaxPower(maxPower);
        follower().startTeleopDrive(true);
    });


    @Override
    public void initialize() {
        // initialization logic (runs on init)
    }

    @Override
    public void periodic() {
        // periodic logic (runs every loop)

    }
}