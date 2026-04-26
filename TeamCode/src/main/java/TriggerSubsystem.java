import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;
import dev.nextftc.ftc.ActiveOpMode;
import dev.nextftc.hardware.impl.ServoEx;

public class TriggerSubsystem  implements Subsystem {
    public static final TriggerSubsystem INSTANCE = new TriggerSubsystem();
    private TriggerSubsystem() { }

    private final ServoEx trigger = new ServoEx("trigger",0.0);

    //public Command open = new SetPosition(trigger, 70).requires(this);
    //public Command close = new SetPosition(trigger, 210).requires(this);

    public LambdaCommand open = new LambdaCommand()
            .setStart(() -> {
                trigger.setPosition(70);
            })
            .setIsDone(()->{return true;});


    public LambdaCommand close = new LambdaCommand()
            .setStart(() -> {
                trigger.setPosition(210);
            })
            .setIsDone(()->{return true;});


    @Override
    public void initialize() {
        // initialization logic (runs on init)

    }

    @Override
    public void periodic() {
        // periodic logic (runs every loop)
        ActiveOpMode.telemetry().addData("Trigger Position", trigger.getPosition());
    }


}
