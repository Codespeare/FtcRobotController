import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import dev.nextftc.core.commands.Command;
import dev.nextftc.core.commands.conditionals.IfElseCommand;
import dev.nextftc.core.commands.groups.ParallelGroup;
import dev.nextftc.core.commands.groups.SequentialGroup;
import dev.nextftc.core.components.BindingsComponent;
import dev.nextftc.core.components.SubsystemComponent;
import dev.nextftc.extensions.pedro.PedroComponent;
import dev.nextftc.extensions.pedro.PedroDriverControlled;
import dev.nextftc.ftc.ActiveOpMode;
import dev.nextftc.ftc.Gamepads;
import dev.nextftc.ftc.NextFTCOpMode;
import dev.nextftc.ftc.components.BulkReadComponent;
import dev.nextftc.hardware.driving.DriverControlledCommand;


@TeleOp(name = "ElsiFreeRoam", group = "Event")
public class ElsiFreeRoam extends NextFTCOpMode {
    {
        addComponents(
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

    private static double targetVelocity = 700;

    private final Pose redCloseStartPose = new Pose(128.5,112.5, Math.toRadians(90));

    private static double getTargetVelocity () {
        return targetVelocity;
    }

    @Override public void onInit() {
        TriggerSubsystem.INSTANCE.close.run();
    }

    @Override public void onWaitForStart() { }
    @Override public void onStartButtonPressed() {
        ActiveOpMode.telemetry().setAutoClear(true);

        //TODO: Pass ending pose from Auto to Teleop
        follower().setStartingPose(redCloseStartPose);

        DriverControlledCommand driverControlled = new PedroDriverControlled(
                Gamepads.gamepad1().leftStickY().negate().deadZone(0.05).map(this::cube),
                Gamepads.gamepad1().leftStickX().negate().deadZone(0.05).map(this::cube),
                Gamepads.gamepad1().rightStickX().negate().deadZone(0.05).map(this::cube)
        );

        driverControlled.schedule();

        follower().startTeleopDrive(true);

        Gamepads.gamepad1().rightTrigger().atLeast(0.8)
                .whenBecomesTrue(new SequentialGroup(
                        FlywheelSubsystem.INSTANCE.spinFlywheels(ElsiFreeRoam::getTargetVelocity),
                        IntakeSubsystem.INSTANCE.enable,
                        TriggerSubsystem.INSTANCE.open
                ))
                .whenBecomesFalse(
                        new ParallelGroup(
                                FlywheelSubsystem.INSTANCE.spinDown(),
                                IntakeSubsystem.INSTANCE.disable,
                                TriggerSubsystem.INSTANCE.close)
                )
                .whenFalse(autoIntake());

        Gamepads.gamepad1().leftTrigger().atLeast(0.8)
                .whenBecomesTrue(ParkSubsystem.INSTANCE.beginParkMode)
                .whenBecomesFalse(ParkSubsystem.INSTANCE.endParkMode);

        Gamepads.gamepad1().dpadUp().whenBecomesTrue(() -> {
            if(targetVelocity < 1000)
                targetVelocity += 50;
        });

        Gamepads.gamepad1().dpadDown().whenBecomesTrue(() -> {
            if(targetVelocity > 100)
                targetVelocity -= 50;
        });

    }

    private Command autoIntake () {
        return new IfElseCommand(
                MagazineSubsystem.INSTANCE::getBallDetected,
                IntakeSubsystem.INSTANCE.disable,
                IntakeSubsystem.INSTANCE.enable
        );
    }

    @Override public void onUpdate() {
        ActiveOpMode.telemetry().addData("Target Velocity", targetVelocity);
        ActiveOpMode.telemetry().update();
    }

    @Override public void onStop() { }

    private Double cube (Double value) {
        return value * value * value;
    }
}