import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.seattlesolvers.solverslib.util.InterpLUT;

import java.util.function.DoubleSupplier;

import dev.nextftc.core.commands.Command;
import dev.nextftc.core.commands.utility.InstantCommand;
import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;
import dev.nextftc.ftc.ActiveOpMode;
import dev.nextftc.hardware.impl.MotorEx;

public class FlywheelSubsystem implements Subsystem {
    public static final FlywheelSubsystem INSTANCE = new FlywheelSubsystem();

    private static InterpLUT rangePowerTable;

    private FlywheelSubsystem() {
        rangePowerTable = new InterpLUT();
        double longPower = 925;
        double basePower = 800;
        double longIncrement = 4;
        double increment = 1.9;
        rangePowerTable.add(0,0); //Absolute Minimum Input
        rangePowerTable.add(50,basePower);
        //rangePowerTable.add(55,basePower); //About 30% of shots successful
        //rangePowerTable.add(60,basePower); //Actual Minimum Shooting Distance
        rangePowerTable.add(70,basePower += increment);
        rangePowerTable.add(80,basePower += increment);
        rangePowerTable.add(90,basePower += increment);
        rangePowerTable.add(100,basePower += increment);
        rangePowerTable.add(110,basePower += increment);
        rangePowerTable.add(120,longPower);
        rangePowerTable.add(130,longPower += longIncrement);
        rangePowerTable.add(140,longPower += longIncrement); //Far Zone
        rangePowerTable.add(150,longPower += longIncrement); //Far Zone
        rangePowerTable.add(250,longPower += longIncrement); //Absolute Maximum Input
        rangePowerTable.createLUT();
    }

    private final MotorEx flywheelTop = new MotorEx("flywheelTop")
            //.reversed()
            .floatMode();
    private final MotorEx flywheelBottom = new MotorEx("flywheelBottom")
            //.reversed()
            .floatMode();

    private VoltageSensor voltSensor;

    //Commented values are home calibrated
    private static final double kSbottom = 0.02113;//0.10050;
    private static final double kVbottom = 0.0003951;//0.0003877;
    private static final double kPbottom = 0.019222;//0.012814;

    private static final double kStop = -0.3675;//0.05228;
    private static final double kVtop = 0.0003927;//0.0003786;
    private static final double kPtop = 0.028833;//0.003797;

    private static final double velocityTolerance = 18;

    private static final double topSpin = -.05;

    private static double velocityTarget = 0;

    private double topTarget () {
        return velocityTarget * (1 + topSpin);
    }

    private double bottomTarget () {
        return velocityTarget * (1 - topSpin);
    }

    public Command spinFlywheelsByDistance (DoubleSupplier distance) {
        return spinFlywheels(()->{
            return rangePowerTable.get(distance.getAsDouble());
        });
    }

    public Command spinFlywheels (DoubleSupplier target) {
        return new LambdaCommand()
                .setStart(() -> {
                    velocityTarget = target.getAsDouble();
                })
                .setIsDone(() -> {
                    return isWithinTolerance(topTarget(),flywheelTop.getVelocity())
                    && isWithinTolerance(bottomTarget(),flywheelBottom.getVelocity());
                })
                .setUpdate(()->{
                    ActiveOpMode.telemetry().addData("spinFlywheels","");
                })
                .setInterruptible(true)
                .requires(this);
    }

    public Command spinDown () {
        return new InstantCommand(()-> {
            velocityTarget = 0;
        })
        .setInterruptible(true)
        .requires(this);
    }

    @Override
    public void initialize() {
        // initialization logic (runs on init)
        voltSensor = ActiveOpMode.hardwareMap().get(VoltageSensor.class,"Control Hub");
    }

    @Override
    public void periodic() {
        // periodic logic (runs every loop)

        ActiveOpMode.telemetry().addData("Top Velocity", flywheelTop.getVelocity());
        ActiveOpMode.telemetry().addData("Bottom Velocity", flywheelBottom.getVelocity());

        /*
        double power = topControl.calculate(new KineticState(
                        flywheelTop.getCurrentPosition(),
                        flywheelTop.getVelocity()));

        ActiveOpMode.telemetry().addData("Top Calculated Power", power);

         */
        ActiveOpMode.telemetry().addData("Target Control", velocityTarget);

        if(velocityTarget > 0) {

            //double topPower = feedforwardBangBang(topTarget(),flywheelTop.getVelocity(),kStop,kVtop);
            //double bottomPower = feedforwardBangBang(bottomTarget(),flywheelBottom.getVelocity(),kSbottom,kVbottom);


            double topPower = feedforwardPlusP(topTarget(), flywheelTop.getVelocity(), kStop, kPtop, kVtop);
            double bottomPower = feedforwardPlusP(bottomTarget(), flywheelBottom.getVelocity(), kSbottom, kPbottom, kVbottom);

            if(topPower < 0) topPower = 0;
            if(bottomPower < 0) bottomPower = 0;

            flywheelTop.setPower(
                   topPower * 12 / voltSensor.getVoltage()
            );
            flywheelBottom.setPower(
                   bottomPower * 12 / voltSensor.getVoltage()
            );


            //ActiveOpMode.telemetry().addData("Top Power", topPower);
            //ActiveOpMode.telemetry().addData("Bottom Power", bottomPower);

        } else {
            flywheelTop.setPower(0);
            flywheelBottom.setPower(0);
        }

    }


    static boolean isWithinTolerance (double target, double current) {
        double flywheelVariance = Math.abs(target - current);
        ActiveOpMode.telemetry().addData("Flywheel Variance", flywheelVariance);
        return Math.abs(target - current) <= velocityTolerance;
    }

    static double feedforwardBangBang(double target, double current, double kS, double kV) {
        if (current < target) return 1.0;
        return Math.max(0, Math.min(1, kS + kV * target));
    }

    double feedforwardPlusP(double target, double current, double kS, double kP, double kV) {
        double p = kS + kV * target + kP * (target - current);
        return Math.max(0, Math.min(1, p));
    }

}
