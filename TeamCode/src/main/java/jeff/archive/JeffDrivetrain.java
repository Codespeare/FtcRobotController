package jeff.archive;

import static jeff.archive.JeffTelemetry.telemetry;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

/// Drive System
public class JeffDrivetrain {
    private static DcMotor frontLeftDrive;
    private static DcMotor frontRightDrive;
    private static DcMotor backLeftDrive;
    private static DcMotor backRightDrive;

    // Smoothed outputs that we carry between loop() calls
    private static double xOut = 0.0;
    private static double yOut = 0.0;
    private static double rotOut = 0.0;

    // Tuning knobs
    private static final double DEADBAND = 0.05;
    private static final double MAX_DELTA = 0.10;

    public static void init(HardwareMap hardwareMap){
        frontLeftDrive = hardwareMap.get(DcMotor.class, "front_left_drive");
        frontRightDrive = hardwareMap.get(DcMotor.class, "front_right_drive");
        backLeftDrive = hardwareMap.get(DcMotor.class, "back_left_drive");
        backRightDrive = hardwareMap.get(DcMotor.class, "back_right_drive");

        // We set the left motors in reverse which is needed for drive trains where the left
        // motors are opposite to the right ones.
        //backLeftDrive.setDirection(DcMotor.Direction.REVERSE); //The gear is backwards on this one
        frontLeftDrive.setDirection(DcMotor.Direction.REVERSE);

        // This uses RUN_USING_ENCODER to be more accurate.   If you don't have the encoder
        // wires, you should remove these
        frontLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeftDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRightDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
    public static void loop () {
        drive(JeffGamepad.gamepadEx.getLeftY(), JeffGamepad.gamepadEx.getLeftX(), JeffGamepad.gamepadEx.getRightX());
        telemetry.addLine("The left joystick sets the robot direction");
        telemetry.addLine("Moving the right joystick left and right turns the robot");
    }

    public static void drive(double forward, double right, double rotate) {
        // This calculates the power needed for each wheel based on the amount of forward,
        // strafe right, and rotate
        forward = applyDeadband(forward, DEADBAND);
        right = applyDeadband(right, DEADBAND);
        rotate = applyDeadband(rotate, DEADBAND);

        // --- 2. Non-linear shaping (cubic) ---
        double xTarget = cube(right);
        double yTarget = cube(forward);
        double rotTarget = cube(rotate);

        // --- 3. Slew rate limiting (smooth ramps) ---
        /*
        xOut = slew(xTarget, xOut, MAX_DELTA);
        yOut = slew(yTarget, yOut, MAX_DELTA);
        rotOut = slew(rotTarget, rotOut, MAX_DELTA);
        */
        xOut = xTarget;
        yOut = yTarget;
        rotOut = rotTarget;

        // --- 4. Convert to mecanum wheel powers ---
        double fl = yOut + xOut + rotOut;
        double fr = yOut - xOut - rotOut;
        double bl = yOut - xOut + rotOut;
        double br = yOut + xOut - rotOut;

        // --- 5. Normalize so we never exceed |1.0| ---
        double max = Math.max(
                1.0,
                Math.max(Math.abs(fl),
                        Math.max(Math.abs(fr),
                                Math.max(Math.abs(bl), Math.abs(br)))));

        fl /= max;
        fr /= max;
        bl /= max;
        br /= max;

        // --- 6. Send to motors ---
        frontLeftDrive.setPower(fl);
        frontRightDrive.setPower(fr);
        backLeftDrive.setPower(bl);
        backRightDrive.setPower(br);
    }

    private static double applyDeadband(double x, double db) {
        return (Math.abs(x) < db) ? 0.0 : x;
    }

    // Shape input so small stick movements give very fine control
    private static double cube(double x) {
        return x * x * x;
    }

    // Limit how fast value can change per loop
    private static double slew(double target, double current, double maxDelta) {
        double delta = target - current;
        if (delta > maxDelta) {
            delta = maxDelta;
        } else if (delta < -maxDelta) {
            delta = -maxDelta;
        }
        return current + delta;
    }
}
