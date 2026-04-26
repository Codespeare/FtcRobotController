package elsi.archive;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

@TeleOp(name = "ElsiTransferPrototype", group = "Prototype")
@Disabled
public class ElsiTransferPrototype extends OpMode {

    private MotorEx transferMotor;

    private boolean intakeOn = false;

    @Override
    public void init() {
        ElsiReadCache.init(hardwareMap);
        ElsiGamepad.init(gamepad1);
        transferMotor = new MotorEx(hardwareMap, "transfer", Motor.GoBILDA.RPM_1150);
        transferMotor.setRunMode(Motor.RunMode.RawPower);
    }

    @Override
    public void loop() {
        ElsiReadCache.loop();
        ElsiGamepad.loop();

        if(ElsiGamepad.gamepadEx.wasJustPressed(GamepadKeys.Button.A)) {
            intakeOn = !intakeOn;
        }

        if (intakeOn) {
            transferMotor.set(1);
        } else {
            transferMotor.set(0);
        }

    }
}
