import java.util.function.DoubleSupplier;

import dev.nextftc.control.ControlSystem;
import dev.nextftc.control.KineticState;
import dev.nextftc.core.commands.Command;
import dev.nextftc.core.commands.groups.ParallelGroup;
import dev.nextftc.core.commands.utility.InstantCommand;
import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;
import dev.nextftc.ftc.ActiveOpMode;
import dev.nextftc.hardware.controllable.RunToVelocity;
import dev.nextftc.hardware.impl.MotorEx;

public class FlywheelSubsystem implements Subsystem {
    public static final FlywheelSubsystem INSTANCE = new FlywheelSubsystem();
    private FlywheelSubsystem() { }

    private final double kP = 0.8;
    private final double kV = 0.001;

    private MotorEx flywheelTop = new MotorEx("flywheelTop")
            .reversed()
            .floatMode();
    private MotorEx flywheelBottom = new MotorEx("flywheelBottom")
            .reversed()
            .floatMode();

    private final ControlSystem topControl = ControlSystem.builder()
            .velSquID(kP)
            .basicFF(kV)
            .build();

    private final ControlSystem bottomControl = ControlSystem.builder()
            .velSquID(kP)
            .basicFF(kV)
            .build();

    public Command spinFlywheels (DoubleSupplier target) {
        return new LambdaCommand()
                .setStart(() -> {
                    new ParallelGroup(
                            new RunToVelocity(topControl,target.getAsDouble(),50.0),
                            new RunToVelocity(bottomControl,target.getAsDouble(), 50.0)
                    ).run();
                })
                .setIsDone(() -> {
                    return topControl.isWithinTolerance(new KineticState(Double.POSITIVE_INFINITY, 50.0))
                            && bottomControl.isWithinTolerance(new KineticState(Double.POSITIVE_INFINITY, 50.0));
                });
    }

    public Command spinDown () {
        return new InstantCommand(()-> {
            new ParallelGroup(
                new RunToVelocity(topControl,0),
                new RunToVelocity(bottomControl,0)
            ).run();
        }) ;
    }

    @Override
    public void initialize() {
        // initialization logic (runs on init)
    }

    @Override
    public void periodic() {
        // periodic logic (runs every loop)

        double goal = topControl.getGoal().getVelocity();

        ActiveOpMode.telemetry().addData("Top Velocity", flywheelTop.getVelocity());
        ActiveOpMode.telemetry().addData("Bottom Velocity", flywheelBottom.getVelocity());


        /*
        double power = topControl.calculate(new KineticState(
                        flywheelTop.getCurrentPosition(),
                        flywheelTop.getVelocity()));

        ActiveOpMode.telemetry().addData("Top Calculated Power", power);

         */
        ActiveOpMode.telemetry().addData("Target Control", goal);

        if(goal > 0) {
            double topPower = topControl.calculate(new KineticState(
                    flywheelTop.getCurrentPosition(),
                    flywheelTop.getVelocity()));
            double bottomPower = bottomControl.calculate(new KineticState(
                    flywheelBottom.getCurrentPosition(),
                    flywheelBottom.getVelocity()));

            if(topPower < 0) topPower = 0;
            if(bottomPower < 0) bottomPower = 0;

            flywheelTop.setPower(
                   topPower
            );
            flywheelBottom.setPower(
                   bottomPower
            );

            ActiveOpMode.telemetry().addData("Top Power", topPower);
            ActiveOpMode.telemetry().addData("Bottom Power", bottomPower);

        } else {
            flywheelTop.setPower(0);
            flywheelBottom.setPower(0);
        }

        boolean readyToFire = topControl.isWithinTolerance(new KineticState(Double.POSITIVE_INFINITY, 50.0))
                && bottomControl.isWithinTolerance(new KineticState(Double.POSITIVE_INFINITY, 50.0));


    }

}
