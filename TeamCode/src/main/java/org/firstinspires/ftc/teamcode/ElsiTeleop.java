package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Elsi", group = "TeleOp")
public class ElsiTeleop extends OpMode {

    private final boolean shootingEnabled = true;
    private final boolean intakeEnabled = true;
    private final boolean visionEnabled = true;

    public static TeamColor teamColor = new TeamColor();

    public static ElsiRangefind rangefind = new ElsiRangefind();

    public static ElsiVision elsiVision;

    @Override
    public void init() {
        ElsiReadCache.init(hardwareMap);

        ElsiTelemetry.init(telemetry);
        if(shootingEnabled) ElsiPinpoint.init(hardwareMap);
        ElsiGamepad.init(gamepad1);
        if(intakeEnabled) ElsiTransfer.init(hardwareMap, false);
        ElsiDrivetrain.init(hardwareMap);
        if(shootingEnabled) ElsiLauncher.init(hardwareMap, false);

        if(visionEnabled) {
            elsiVision = new ElsiVision(hardwareMap, ElsiDrivetrain.startingPose);
        }

        ElsiMagazineIndicator.init(hardwareMap);
        Drawing.init();
    }

    @Override
    public void start() {
        if(intakeEnabled) ElsiTransfer.start();
        ElsiDrivetrain.start();
    }

    @Override
    public void stop() {
        ElsiDrivetrain.stop();
    }

    @Override
    public void loop() {
        ElsiReadCache.loop();

        if(shootingEnabled) ElsiPinpoint.loop();
        if(visionEnabled) ElsiVision.loop();

        ElsiGamepad.loop();
        if(intakeEnabled) ElsiTransfer.loop();
        ElsiDrivetrain.loop();
        if(shootingEnabled) ElsiLauncher.loop();

        ElsiMagazineIndicator.loop();

        ElsiTelemetry.loop();
    }
}
