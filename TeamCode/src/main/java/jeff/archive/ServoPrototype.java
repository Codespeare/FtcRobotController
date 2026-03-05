package jeff.archive;


import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

@TeleOp(name = "Servo Testing", group = "Prototype")
@Disabled
public class ServoPrototype extends OpMode {

    private ServoEx feeder;

    @Override
    public void init() {
        JeffGamepad.init(gamepad1);
        JeffBallTransfer.init(hardwareMap);
    }

    @Override
    public void init_loop(){

    }

    @Override
    public void loop() {
        JeffGamepad.loop();
        JeffBallTransfer.loop();
    }
}
