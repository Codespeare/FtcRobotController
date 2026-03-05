package org.firstinspires.ftc.teamcode;


import static org.firstinspires.ftc.teamcode.ElsiTelemetry.telemetry;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.util.Range;
import com.seattlesolvers.solverslib.controller.SquIDFController;
import com.seattlesolvers.solverslib.hardware.motors.Motor;
import com.seattlesolvers.solverslib.hardware.motors.MotorEx;

@Configurable
public class ElsiFlywheelController {
    private MotorEx motor;
    private SquIDFController squidf;

    public double targetVelocity = 800;
    public double currentVelocity;

    // ---- Tunables ----
    public double kF = 0.0001;      // start ~0.6/6000, refine after first spin
    public double kP = 0.08;         // tune: double until small oscillation, halve
    public double kI = 0.000;        // small; or kP/Ti with Ti~3–6s
    public double kD = 0.000;        // start ~P/10; adjust ±50%

    public double readyToleranceRpm = .1;    // within 10% counts as ready

    public ElsiFlywheelController(MotorEx motor) {
        this.motor = motor;
        this.squidf = new SquIDFController(kP, kI, kD, kF);

        // FTC flywheel is one-direction → we'll clamp 0..1 at the output stage
        motor.setRunMode(Motor.RunMode.RawPower);
        motor.setInverted(true);
        motor.setZeroPowerBehavior(Motor.ZeroPowerBehavior.FLOAT);
    }

    public ElsiFlywheelController() {}

    /** Call every loop; returns applied motor power [0..1] */
    public double update(double t) {
        targetVelocity = t;

        // Read Velocity
        currentVelocity = motor.getVelocity();

        // Compute control effort

        squidf.setPIDF(kP, kI, kD, kF);

        double u = squidf.calculate(currentVelocity, targetVelocity);

        // Clamp to 0..1 (one-direction flywheel), with anti-windup
        double power = Range.clip(u, 0.0, 1.0);

        if(currentVelocity > targetVelocity * 1.05) {
            //squidf.reset();
            motor.set(0);
        } else {
            motor.set(power);
        }

        return power;
    }

    public boolean isReady() {
        double currentError = Math.abs( targetVelocity - currentVelocity);
        boolean ready = currentError <= (targetVelocity * readyToleranceRpm);

        telemetry.addData("Current Error", currentError);
        telemetry.addData("Error Margin", targetVelocity * readyToleranceRpm);
        telemetry.addData("Ready to Fire", ready);
        return ready;
    }

}
