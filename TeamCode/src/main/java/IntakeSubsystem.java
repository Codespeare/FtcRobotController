import dev.nextftc.core.commands.delays.Delay;
import dev.nextftc.core.commands.groups.SequentialGroup;
import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;
import dev.nextftc.hardware.impl.MotorEx;

public class IntakeSubsystem implements Subsystem {
    public static final IntakeSubsystem INSTANCE = new IntakeSubsystem();
    private IntakeSubsystem() { }

    private final MotorEx intake = new MotorEx("intake")
            .reversed()
            .brakeMode();

    private final MotorEx transfer = new MotorEx("transfer")
            .reversed()
            .brakeMode();

    //private static double maxPower = 0.85;
    private static final double maxPower = 1.0;


    public LambdaCommand enable = new LambdaCommand()
            .setStart(() -> {
                intake.setPower(maxPower);
                transfer.setPower(maxPower);
            })
            .setIsDone(()-> true)
            .setInterruptible(true);

    public LambdaCommand disable = new LambdaCommand()
            .setStart(() -> {
                intake.setPower(0);
                transfer.setPower(0);
            })
            .setIsDone(()-> true)
            .setInterruptible(true);

    public LambdaCommand enableTransfer = new LambdaCommand()
            .setStart(() -> transfer.setPower(maxPower))
            .setIsDone(()-> true)
            .setInterruptible(true);

    public LambdaCommand backfeed = new LambdaCommand()
            .setStart(() -> {
                intake.setPower(maxPower);
                transfer.setPower(-0.5);
            })
            .setIsDone(()-> true)
            .setInterruptible(true);

    public LambdaCommand enableTransferForFlywheel = new LambdaCommand()
            .setStart(() -> transfer.setPower(.75)) // 7 may be perfect
            .setIsDone(()-> true)
            .setInterruptible(true);

    public LambdaCommand enableTransferForLaunch = new LambdaCommand()
            .setStart(() -> new SequentialGroup(
                    backfeed,
                    new Delay(0.1),
                    enableTransferForFlywheel
            ).run())
            .setIsDone(()->true)
            .setInterruptible(true);

    public LambdaCommand disableTransfer = new LambdaCommand()
            .setStart(() -> transfer.setPower(0))
            .setIsDone(()-> true)
            .setInterruptible(true);

    @Override
    public void initialize() {
        // initialization logic (runs on init)

    }

    @Override
    public void periodic() {
        // periodic logic (runs every loop)

    }

}
