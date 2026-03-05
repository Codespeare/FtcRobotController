
package jeff.archive;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.List;

@TeleOp(name = "Jeff", group = "Prototype")
@Disabled
public class PrototypeJeffRobot extends OpMode {
    List<LynxModule> allHubs;

    @Override
    public void init() {
        allHubs = hardwareMap.getAll(LynxModule.class);

        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        JeffTelemetry.init(telemetry);

        JeffGamepad.init(gamepad1);

        JeffDrivetrain.init(hardwareMap);

        JeffIntake.init(hardwareMap);

        JeffSpindex.init(hardwareMap);

        /*
        JeffVision.init(hardwareMap.get(WebcamName.class, "Webcam"),
                hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId",
                        "id", hardwareMap.appContext.getPackageName()));
        */

        JeffBallTransfer.init(hardwareMap);


        JeffLauncher.init(hardwareMap);


    }

    @Override
    public void loop() {
        for (LynxModule hub : allHubs) {
            hub.clearBulkCache();
        }

        JeffGamepad.loop();

        JeffDrivetrain.loop();

        JeffIntake.loop();

        JeffSpindex.loop();

        //JeffVision.loop();

        JeffBallTransfer.loop();

        JeffLauncher.loop();

        JeffTelemetry.loop();

        //telemetry.update();
    }




}

