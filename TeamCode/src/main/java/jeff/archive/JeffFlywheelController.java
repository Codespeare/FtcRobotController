package jeff.archive;

import static jeff.archive.RobotVariables.TargetVelocity;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;
import com.seattlesolvers.solverslib.controller.PIDFController;

public class JeffFlywheelController {
    private final DcMotorEx motor;
    private final PIDFController pidf;

    // ---- Tunables ----
    public double kF = 0.00010;      // start ~0.6/6000, refine after first spin
    public double kP = 0.08;         // tune: double until small oscillation, halve
    public double kI = 0.008;        // small; or kP/Ti with Ti~3–6s
    public double kD = 0.006;        // start ~P/10; adjust ±50%
    public double iZoneErrorRpm = 200.0;   // I active only inside this error band
    public double iClamp = 0.2;            // limit integral contribution (±)
    public double lpTimeConstantMs = 30.0; // velocity LPF for stability (10–50ms typical)

    // Ready-to-shoot policy
    public double readyToleranceRpm = 75.0;    // within ±75 RPM counts as “at speed”
    public long   readyHoldMs       = 120L;    // must stay in band this long
    public double shotDipDetectRpm  = 250.0;   // drop from target that flags a shot
    public long   minRecoveryMs     = 180L;    // lockout after a detected shot

    // ---- Internals ----
    private double filtRpm = 0.0;
    private long   lastTs  = 0L;
    private long   lastEnterBand = 0L;
    private long   lastShotTime  = 0L;

    public JeffFlywheelController(DcMotorEx motor) {
        this.motor = motor;
        this.pidf = new PIDFController(kP, kI, kD, kF);

        // FTC flywheel is one-direction → we'll clamp 0..1 at the output stage
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void setGains(double kp, double ki, double kd, double kf) {
        this.kP = kp; this.kI = ki; this.kD = kd; this.kF = kf;
        pidf.setPIDF(kp, ki, kd, kf);
    }

    /** Call every loop; returns applied motor power [0..1] */
    public double update() {
        final long now = System.nanoTime();
        final double dt = (lastTs == 0L) ? 0.02 : (now - lastTs) / 1e9; // seconds (only for LPF)
        lastTs = now;

        // 1) Read RPM (not ticks/s)
        double rpm = getRawRpm();

        // 2) Low-pass filter for stable control (1st-order)
        //    y += alpha*(x - y),  alpha = dt/(tau+dt)
        double tau = lpTimeConstantMs / 1000.0;
        double alpha = (tau <= 0 || dt <= 0) ? 1.0 : dt / (tau + dt);
        filtRpm += alpha * (rpm - filtRpm);

        // 3) I-zone: only let I act when close to setpoint
        double error = TargetVelocity - filtRpm;
        if (Math.abs(error) > iZoneErrorRpm) {
            // crude but effective way to stop I outside zone
            pidf.setPIDF(kP, /*I*/0.0, kD, kF);
        } else {
            pidf.setPIDF(kP, kI, kD, kF);
        }

        // 4) Compute control effort (FTCLib handles dt internally)
        double u = pidf.calculate(filtRpm, TargetVelocity);

        // 5) Clamp to 0..1 (one-direction flywheel), with anti-windup
        double power = Range.clip(u, 0.0, 1.0);
        if (power != u) {
            // Output saturated → avoid windup
            // Option A: bound integral contribution (preferred)
            pidf.setIntegrationBounds(-iClamp, iClamp);
            // Option B (if it runs away): pidf.reset();  // clears I & D state
        }

        // 6) Apply power
        motor.setPower(power);

        // 7) Ready/shot bookkeeping
        updateReadyAndShot(now / 1_000_000L, filtRpm);

        return power;
    }

    private void updateReadyAndShot(long nowMs, double rpm) {
        // Detect a shot by a brief speed dip relative to target
        if (rpm < TargetVelocity - shotDipDetectRpm) {
            lastShotTime = nowMs;
        }

        // Manage ready-hold dwell timing
        if (Math.abs(TargetVelocity - rpm) <= readyToleranceRpm) {
            if (lastEnterBand == 0L) lastEnterBand = nowMs;
        } else {
            lastEnterBand = 0L;
        }
    }

    public boolean isReady(long nowMs) {
        // must be in tolerance for readyHoldMs and past recovery lockout
        boolean dwellOk = (lastEnterBand != 0L) && (nowMs - lastEnterBand >= readyHoldMs);
        boolean recovered = (nowMs - lastShotTime) >= minRecoveryMs;
        return dwellOk && recovered;
    }

    public double getFilteredRpm() { return filtRpm; }

    public double getRawRpm()      {
        double tps = motor.getVelocity();                   // ticks per second

        double rpm = (tps * 60.0) / 28;           // tps -> rev/min
        return rpm;
    }


}
