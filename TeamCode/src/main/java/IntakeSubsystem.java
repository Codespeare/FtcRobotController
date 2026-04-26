import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;
import dev.nextftc.hardware.impl.MotorEx;

public class IntakeSubsystem implements Subsystem {
    public static final IntakeSubsystem INSTANCE = new IntakeSubsystem();
    private IntakeSubsystem() { }

    private MotorEx intake = new MotorEx("intake")
            .brakeMode()
            .reversed();
    private MotorEx transfer = new MotorEx("transfer")
            .brakeMode();

    private static double maxPower = 1.0;

    public LambdaCommand enable = new LambdaCommand()
            .setStart(() -> {
                intake.setPower(maxPower);
                transfer.setPower(maxPower);
            })
            .setIsDone(()->{return true;});

    public LambdaCommand disable = new LambdaCommand()
            .setStart(() -> {
                intake.setPower(0);
                transfer.setPower(0);
            })
            .setIsDone(()->{return true;});

    @Override
    public void initialize() {
        // initialization logic (runs on init)
    }

    @Override
    public void periodic() {
        // periodic logic (runs every loop)

    }

}
