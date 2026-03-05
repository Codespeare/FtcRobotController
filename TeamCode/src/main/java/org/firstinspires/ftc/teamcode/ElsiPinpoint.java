package org.firstinspires.ftc.teamcode;

import com.pedropathing.geometry.Pose;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class ElsiPinpoint {
    private static GoBildaPinpointDriver pinpoint;
    private static final Pose blueCornerStartingPose = new Pose(8.5, 8.5, Math.toRadians(90));
    private static final Pose redCornerStartingPose = new Pose(135.5, 8.5, Math.toRadians(90));

    public static void init (HardwareMap hardwareMap) {
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
    }
    public static void loop () {
        pinpoint.update();

        if(ElsiGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.DPAD_LEFT)) {
            ElsiDrivetrain.updatePosition(blueCornerStartingPose);
        } else if (ElsiGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.DPAD_RIGHT)) {
            ElsiDrivetrain.updatePosition(redCornerStartingPose);
        }
    }
    public static double getHeading() {
        double heading = pinpoint.getHeading(AngleUnit.DEGREES);
        ElsiTelemetry.telemetry.addData("Pinpoint Heading", heading);
        return heading;
    }
}
