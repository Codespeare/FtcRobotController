package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.pedroPathing.Tuning.follower;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import static dev.nextftc.extensions.pedro.PedroComponent.follower;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.function.Supplier;

import dev.nextftc.core.commands.Command;
import dev.nextftc.core.components.BindingsComponent;
import dev.nextftc.core.components.SubsystemComponent;
import dev.nextftc.extensions.pedro.PedroComponent;
import dev.nextftc.extensions.pedro.PedroDriverControlled;
import dev.nextftc.ftc.Gamepads;
import dev.nextftc.ftc.NextFTCOpMode;
import dev.nextftc.ftc.components.BulkReadComponent;
import dev.nextftc.hardware.driving.DriverControlledCommand;
import dev.nextftc.hardware.driving.MecanumDriverControlled;
import dev.nextftc.hardware.impl.MotorEx;

@TeleOp(name = "opal", group = "TeleOp")
public class TestDriveCode extends NextFTCOpMode {

    public TestDriveCode() {
        addComponents(
                BulkReadComponent.INSTANCE,
                BindingsComponent.INSTANCE,
                new PedroComponent(Constants::createFollower),
                new SubsystemComponent(AimingSubSystem.INSTANCE)
        );
    }

    @Override public void onInit() {
    }

    @Override public void onWaitForStart() {
    }
    @Override public void onStartButtonPressed() {
        DriverControlledCommand driverControlled = new PedroDriverControlled(
                getCubeValue(Gamepads.gamepad1().leftStickY().negate()),
                getCubeValue(Gamepads.gamepad1().leftStickX().negate()),
                getCubeValue(Gamepads.gamepad1().rightStickX().negate())
        );
        driverControlled.schedule();
        Gamepads.gamepad1().rightTrigger().atLeast(0.8).whenBecomesTrue(AimingSubSystem.INSTANCE.activateAim);
        Gamepads.gamepad1().rightTrigger().lessThan(0.8).whenBecomesTrue(AimingSubSystem.INSTANCE.deactivateAim);
        follower().setPose(new Pose(72,72,Math.toRadians(90)));
    }

    @Override public void onUpdate() {
    }

    @Override public void onStop() {
    }

    Supplier<Double> getCubeValue (Supplier<Double> rootValue) {
        return () -> {
                Double value = rootValue.get();
            return value * value * value; };
    }
}
