import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;
import dev.nextftc.ftc.ActiveOpMode;

public class TriggerSubsystem  implements Subsystem {
    public static final TriggerSubsystem INSTANCE = new TriggerSubsystem();
    private TriggerSubsystem() { }

    private ServoEx trigger;

    //public Command open = new SetPosition(trigger, 70).requires(this);
    //public Command close = new SetPosition(trigger, 210).requires(this);

    public LambdaCommand open = new LambdaCommand()
            .setStart(() -> {
                trigger.set(70);
            })
            .setIsDone(()->{return true;});


    public LambdaCommand close = new LambdaCommand()
            .setStart(() -> {
                trigger.set(210);
            })
            .setIsDone(()->{return true;});


    @Override
    public void initialize() {
        // initialization logic (runs on init)

        trigger = new ServoEx(ActiveOpMode.hardwareMap(), "trigger",0,300);
        INSTANCE.close.run();
    }

    @Override
    public void periodic() {
        // periodic logic (runs every loop)
        ActiveOpMode.telemetry().addData("Trigger Position", trigger.get());
    }


}
