import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.function.DoubleSupplier;

import dev.nextftc.core.commands.Command;
import dev.nextftc.core.commands.conditionals.IfElseCommand;
import dev.nextftc.core.commands.delays.Delay;
import dev.nextftc.core.commands.groups.ParallelGroup;
import dev.nextftc.core.commands.groups.ParallelRaceGroup;
import dev.nextftc.core.commands.groups.SequentialGroup;
import dev.nextftc.core.commands.utility.InstantCommand;
import dev.nextftc.core.commands.utility.NullCommand;
import dev.nextftc.core.components.BindingsComponent;
import dev.nextftc.core.components.SubsystemComponent;
import dev.nextftc.extensions.pedro.PedroComponent;
import dev.nextftc.extensions.pedro.PedroDriverControlled;
import dev.nextftc.ftc.ActiveOpMode;
import dev.nextftc.ftc.Gamepads;
import dev.nextftc.ftc.NextFTCOpMode;
import dev.nextftc.ftc.components.BulkReadComponent;
import dev.nextftc.hardware.driving.DriverControlledCommand;

@TeleOp(name = "OpalTeleop", group = "Opal")
public class OpalPrototype  extends NextFTCOpMode {
    {
        addComponents(
                new SubsystemComponent(AutoTargetSubsystem.INSTANCE),
                new SubsystemComponent(ParkSubsystem.INSTANCE),
                new SubsystemComponent(FlywheelSubsystem.INSTANCE),
                new SubsystemComponent(IntakeSubsystem.INSTANCE),
                new SubsystemComponent(TriggerSubsystem.INSTANCE),
                new SubsystemComponent(MagazineSubsystem.INSTANCE),
                new PedroComponent(Constants::createFollower),
                BulkReadComponent.INSTANCE,
                BindingsComponent.INSTANCE
        );

    }

    //private final MotorEx fr = new MotorEx("frontRight");

    //private final MotorEx rr = new MotorEx("backRight");

   // private final MotorEx fl = new MotorEx("frontLeft").reversed();

    //private final MotorEx rl = new MotorEx("backLeft").reversed();

    private boolean autoIntakeEnabled = true;
    private boolean autoAimEnabled = true;

    @Override public void onInit() {
        TriggerSubsystem.INSTANCE.close.run();
        follower().setStartingPose(OpalStatic.currentPose);


        // Wait 120 seconds (2 minutes) then stop the OpMode
        /*
        new SequentialGroup(new Delay(128),
                new InstantCommand(ActiveOpMode::terminateOpModeNow)
        ).run();
        */
    }

    @Override public void onStartButtonPressed() {
        ActiveOpMode.telemetry().setAutoClear(true);

        DriverControlledCommand driverControlled = new PedroDriverControlled(
                Gamepads.gamepad1().leftStickY().negate().deadZone(0.05).map(this::cube),
                Gamepads.gamepad1().leftStickX().negate().deadZone(0.05).map(this::cube),
                Gamepads.gamepad1().rightStickX().negate().deadZone(0.05).map(this::cube)
        );

        driverControlled.schedule();

        follower().startTeleopDrive(true);

        Gamepads.gamepad1().rightTrigger().atLeast(0.8)
                .whenBecomesTrue(new SequentialGroup(
                        IntakeSubsystem.INSTANCE.disable,
                        new ParallelGroup(
                                new ParallelRaceGroup(
                                        AutoTargetSubsystem.INSTANCE.beginTargeting(autoAimEnabled),
                                        new Delay(5)),
                                new ParallelRaceGroup(autoAimEnabled ?
                                        FlywheelSubsystem.INSTANCE.spinFlywheelsByDistance(() -> {
                                    return getRangeToGoal(follower().getPose(), OpalStatic.currentGoalPose());})
                                        : FlywheelSubsystem.INSTANCE.spinFlywheels(constantDistance()),
                                        new Delay(5))
                        ),
                        TriggerSubsystem.INSTANCE.open,
                        new Delay(0.2),
                        IntakeSubsystem.INSTANCE.enableTransferForLaunch
                        )
                                .setInterruptible(true)
                                .addRequirements(AutoTargetSubsystem.INSTANCE, FlywheelSubsystem.INSTANCE, IntakeSubsystem.INSTANCE, TriggerSubsystem.INSTANCE)
                )
                .whenBecomesFalse(
                        new ParallelGroup(
                                FlywheelSubsystem.INSTANCE.spinDown(),
                                AutoTargetSubsystem.INSTANCE.endAutoTarget,
                                IntakeSubsystem.INSTANCE.disableTransfer,
                                TriggerSubsystem.INSTANCE.close)
                                .setInterruptible(true)
                                .addRequirements(AutoTargetSubsystem.INSTANCE, FlywheelSubsystem.INSTANCE, IntakeSubsystem.INSTANCE, TriggerSubsystem.INSTANCE)

                )
                .whenFalse(autoIntake());

        Gamepads.gamepad1().b().whenBecomesTrue(
                new ParallelGroup(
                        IntakeSubsystem.INSTANCE.enable,
                        new InstantCommand(() -> {autoIntakeEnabled = false;})) );

        Gamepads.gamepad1().y().whenBecomesTrue(OpalStatic.swapTeamColor);

        Gamepads.gamepad1().x().whenBecomesTrue(new InstantCommand(() -> {autoAimEnabled = !autoAimEnabled;}));

        autoIntake().run();

        Gamepads.gamepad1().leftTrigger().atLeast(0.8)
                .whenBecomesTrue(ParkSubsystem.INSTANCE.beginParkMode)
                .whenBecomesFalse(ParkSubsystem.INSTANCE.endParkMode);

    }

    private final DoubleSupplier constantDistance () { // for AutoAim override
        return new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return 800;
            }
        };
    }

    private final DoubleSupplier idleVelocity () { // for AutoAim override
        return new DoubleSupplier() {
            @Override
            public double getAsDouble() {
                return 500;
            }
        };
    }

    private final SequentialGroup firingSequence = new SequentialGroup(
            new Delay(.3),
            IntakeSubsystem.INSTANCE.enableTransfer,
            TriggerSubsystem.INSTANCE.open,
            new Delay(.2),
            IntakeSubsystem.INSTANCE.disableTransfer
    );

    @Override
    public void onUpdate () {
        ActiveOpMode.telemetry().addData("Current Goal Aim",OpalStatic.teamColor == TeamColor.red ? "Red" : "Blue");
        ActiveOpMode.telemetry().addData("Auto Aiming", autoAimEnabled);
        //ActiveOpMode.telemetry().addData("Drivetrain Debug", fr.getVelocity() + " " + fl.getVelocity() + " " + fl.getVelocity() + " " + rl.getVelocity());
        //ActiveOpMode.telemetry().addData("Range to Goal", getRangeToGoal(follower().getPose(),Elsi3Static.goalPose));
        ActiveOpMode.telemetry().update();
    }

    private Command autoIntake () {
        if(!autoIntakeEnabled) return new NullCommand();
        //ActiveOpMode.telemetry().addData("Magazine Ball Detected", MagazineSubsystem.INSTANCE::getBallDetected);
        return new IfElseCommand(
                MagazineSubsystem.INSTANCE::getBallDetected,
                new ParallelGroup(
                        IntakeSubsystem.INSTANCE.disable,
                        FlywheelSubsystem.INSTANCE.spinFlywheels(idleVelocity())
                ),
                IntakeSubsystem.INSTANCE.enable
                )
                .setInterruptible(true)
                .addRequirements(MagazineSubsystem.INSTANCE, IntakeSubsystem.INSTANCE, FlywheelSubsystem.INSTANCE)
                ;
    }

    public static double getRangeToGoal (Pose current, Pose goal) {
        double x2 = goal.getX();
        double y2 = goal.getY();
        double x1 = current.getX();
        double y1 = current.getY();

        return Math.hypot(x2 - x1, y2 - y1);
    }

    private Double cube (Double value) {
        return value * value * value;
    }
}
