import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.ArrayList;
import java.util.List;

/**
 * FlywheelAutoTuner
 * -----------------
 * Automated tuning OpMode for a velocity-controlled flywheel shooter.
 *
 * Philosophy: FEEDFORWARD FIRST.
 * A flywheel at steady state is a nearly linear system: power ~ kV * velocity + kS.
 * If feedforward is accurate, it does ~90% of the work and the P term only
 * corrects small disturbances (like a game piece touching the wheel).
 * This gives you fast spin-up with minimal overshoot AND fast shot recovery.
 *
 * Phases:
 *   1. CHARACTERIZE  - Ramp through fixed power levels, record steady-state
 *                      velocity at each, least-squares fit kV (slope) and kS (intercept).
 *   2. TUNE P        - Step the target velocity up/down. Increase kP until
 *                      overshoot exceeds MAX_OVERSHOOT_PCT, then back off 30%.
 *   3. A/B VOLLEY TEST - Simulate a 3-shot volley (brief power cut per shot)
 *                      under TWO controllers: FF+P and FF+Bang-Bang. Reports
 *                      recovery time per shot and velocity ripple for each,
 *                      so you can pick empirically.
 *
 * Output: kS, kV, kP printed to telemetry AND written to Driver Station log.
 * Use them in your custom loop:
 *      power = clamp(kS + kV * target + kP * (target - current), 0, 1)
 *
 * SETUP:
 *   - Set MOTOR_NAME to your config name.
 *   - Set MAX_SAFE_VELOCITY (ticks/sec) below your motor's free speed.
 *     GoBILDA 6000rpm bare motor w/ 28 CPR encoder: ~2800 ticks/sec at output.
 *   - Robot on blocks / flywheel free to spin. Keep hands clear.
 *
 * Controls:
 *   Press A to advance to the next phase when prompted.
 */
@TeleOp(name = "Flywheel Auto Tuner", group = "Tuning")
public class FlywheelAutoTuner extends LinearOpMode {

    // ======================= CONFIGURE THESE =======================
    static final String MOTOR_NAME        = "flywheelBottom";
    static final boolean REVERSED         = false;
    static final double MAX_SAFE_VELOCITY = 2400;   // ticks/sec, ~85% of free speed
    static final double TARGET_VELOCITY   = 800;   // your actual shooting velocity (ticks/sec)

    // Characterization
    static final double[] POWER_LEVELS = {0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
    static final double SETTLE_TIME_S  = 2.0;   // wait this long at each power before sampling
    static final double SAMPLE_TIME_S  = 1.0;   // then average velocity over this window

    // P tuning
    static final double MAX_OVERSHOOT_PCT = 3.0;   // your "no big overshoot" requirement
    static final double KP_START          = 0.0005;
    static final double KP_GROWTH         = 1.5;   // multiply kP by this each iteration
    static final int    MAX_P_ITERATIONS  = 12;

    // Recovery test (simulated volley)
    static final int    NUM_SHOTS            = 3;
    static final double SHOT_DISTURBANCE_S   = 0.5;  // power-cut duration simulating a shot
    static final double RECOVERY_TOLERANCE   = 0.03;  // within 3% of target = "recovered"
    static final double INTER_SHOT_DELAY_S   = 0.3;
    // ================================================================

    DcMotorEx motor;
    double kS = 0, kV = 0, kP = 0;

    @Override
    public void runOpMode() {
        motor = hardwareMap.get(DcMotorEx.class, MOTOR_NAME);
        motor.setDirection(REVERSED ? DcMotor.Direction.REVERSE : DcMotor.Direction.FORWARD);
        // Custom control loop: run WITHOUT the SDK's built-in velocity PID
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT); // never brake a flywheel

        telemetry.addLine("Flywheel Auto Tuner ready.");
        telemetry.addLine("Robot on blocks, wheel clear. Press START.");
        telemetry.update();
        waitForStart();

        // ---------- PHASE 1: FEEDFORWARD CHARACTERIZATION ----------
        List<double[]> samples = new ArrayList<>(); // {velocity, power}
        for (double power : POWER_LEVELS) {
            if (!opModeIsActive()) return;
            motor.setPower(power);
            waitSeconds(SETTLE_TIME_S, "Settling at power " + power);

            double v = averageVelocity(SAMPLE_TIME_S);
            if (v > MAX_SAFE_VELOCITY) {
                telemetry.addLine("Hit max safe velocity, stopping ramp.");
                telemetry.update();
                break;
            }
            samples.add(new double[]{v, power});
            telemetry.addData("power", power);
            telemetry.addData("steady-state vel", "%.1f t/s", v);
            telemetry.update();
        }
        motor.setPower(0);

        // Least-squares fit: power = kV * velocity + kS
        fitFeedforward(samples);
        telemetry.addData("kS (raw fit)", "%.5f", kS);
        telemetry.addData("kV", "%.7f", kV);
        telemetry.update();

        // ---------- PHASE 1.5: FEEDFORWARD TRIM ----------
        // Friction is nonlinear at low powers, so the straight-line fit can be
        // hot or cold at the actual operating point. Run PURE feedforward at
        // the target, measure where it lands, and shift kS so FF alone centers
        // on target. Without this, FF bias reads as "overshoot" in Phase 2 and
        // the kP search punishes the wrong term.
        for (int trim = 0; trim < 2 && opModeIsActive(); trim++) {
            double ffPower = Math.max(0, Math.min(1, kS + kV * TARGET_VELOCITY));
            motor.setPower(ffPower);
            waitSeconds(SETTLE_TIME_S, "FF trim pass " + (trim + 1) + ": settling...");
            double vActual = averageVelocity(SAMPLE_TIME_S);
            double correction = kV * (TARGET_VELOCITY - vActual);
            kS += correction;
            telemetry.addData("FF landed at", "%.1f t/s (target %.1f)", vActual, TARGET_VELOCITY);
            telemetry.addData("kS correction", "%+.5f", correction);
            telemetry.update();
            if (Math.abs(vActual - TARGET_VELOCITY) / TARGET_VELOCITY < 0.01) break; // within 1%
        }
        motor.setPower(0);

        telemetry.addData("kS (trimmed)", "%.5f", kS);
        telemetry.addData("kV", "%.7f", kV);
        telemetry.addLine("Press A for Phase 2 (P tuning).");
        telemetry.update();
        waitForButtonA();

        // ---------- PHASE 2: AUTO-TUNE kP ----------
        kP = KP_START;
        double bestKp = 0;
        for (int i = 0; i < MAX_P_ITERATIONS && opModeIsActive(); i++) {
            spinDown();
            StepResult r = runStepResponse(TARGET_VELOCITY, 3.0);

            telemetry.addData("iteration", i);
            telemetry.addData("kP", "%.6f", kP);
            telemetry.addData("overshoot", "%.2f%%", r.overshootPct);
            telemetry.addData("settle time", "%.2fs", r.settleTime);
            telemetry.update();

            if (r.overshootPct > MAX_OVERSHOOT_PCT) {
                if (bestKp > 0) {
                    kP = bestKp;   // found the ceiling above a known-good value
                    break;
                }
                kP /= 2.0;         // starting guess itself too hot — search DOWN
                if (kP < KP_START / 64.0) {
                    kP = 0;        // give up on P; pure feedforward is fine
                    break;
                }
            } else {
                bestKp = kP;
                kP *= KP_GROWTH;
            }
        }
        if (bestKp > 0) kP = bestKp;
        spinDown();

        telemetry.addData("FINAL kP", "%.6f", kP);
        telemetry.addLine("Press A for Phase 3 (controller A/B volley test).");
        telemetry.update();
        waitForButtonA();

        // ---------- PHASE 3: A/B VOLLEY TEST — FF+P vs FF+BANG-BANG ----------
        // Runs the identical simulated volley under each controller and reports
        // recovery time per shot AND velocity ripple while holding at target.
        // Recovery favors bang-bang; ripple (steadiness) usually favors FF+P.
        VolleyResult ffP = runVolleyTest(this::feedforwardPlusP, "FF + P");
        telemetry.addLine("FF+P done. Press A to run FF+Bang-Bang.");
        telemetry.update();
        waitForButtonA();
        VolleyResult bang = runVolleyTest(this::feedforwardBangBang, "FF + Bang-Bang");

        // ---------- FINAL REPORT ----------
        telemetry.addLine("========== TUNING COMPLETE ==========");
        telemetry.addData("kS", "%.5f", kS);
        telemetry.addData("kV", "%.7f", kV);
        telemetry.addData("kP", "%.6f", kP);
        telemetry.addLine("--- Recovery time per shot (lower = better) ---");
        for (int i = 0; i < NUM_SHOTS; i++) {
            telemetry.addData("Shot " + (i + 1),
                    "P: " + fmtRecovery(ffP.recoveryTimes[i]) + "  BB: " + fmtRecovery(bang.recoveryTimes[i]));
        }
        telemetry.addLine("--- Velocity ripple at hold, std dev t/s (lower = steadier) ---");
        telemetry.addData("FF+P", "%.1f", ffP.holdStdDev);
        telemetry.addData("FF+Bang-Bang", "%.1f", bang.holdStdDev);
        telemetry.addLine("Pick the controller with acceptable ripple and");
        telemetry.addLine("fastest recovery. Control laws:");
        telemetry.addLine(String.format("P:  clamp(%.5f + %.7f*t + %.6f*(t-v), 0, 1)", kS, kV, kP));
        telemetry.addLine(String.format("BB: v < t ? 1.0 : %.5f + %.7f*t", kS, kV));
        telemetry.update();

        while (opModeIsActive()) idle(); // keep results on screen
    }

    // ======================= HELPERS =======================

    /**
     * The control law you'll use in competition code.
     * Clamped to [0, 1]: a one-directional flywheel must NEVER receive reverse
     * power. If we're above target, output floors at 0 and friction/drag bleeds
     * off the excess. This is gentler on the drivetrain and matches how the
     * system behaves in competition, so tuning reflects reality.
     */
    double feedforwardPlusP(double target, double current) {
        double p = kS + kV * target + kP * (target - current);
        return Math.max(0, Math.min(1, p));
    }

    /**
     * FF + bang-bang: full power below target, pure feedforward at/above it.
     * Fastest possible recovery (max torque whenever behind), at the cost of
     * a small oscillation band around the setpoint. The feedforward baseline
     * (instead of dropping to 0 above target) keeps that band tight.
     */
    double feedforwardBangBang(double target, double current) {
        if (current < target) return 1.0;
        return Math.max(0, Math.min(1, kS + kV * target));
    }

    /** A controller maps (targetVel, currentVel) -> motor power. */
    interface Controller {
        double power(double target, double current);
    }

    /**
     * Runs the simulated NUM_SHOTS volley under the given controller.
     * Measures: recovery time after each shot disturbance, and velocity
     * ripple (std dev) while holding at target between shots.
     */
    VolleyResult runVolleyTest(Controller c, String name) {
        VolleyResult result = new VolleyResult();
        result.recoveryTimes = new double[NUM_SHOTS];
        List<Double> holdSamples = new ArrayList<>();

        telemetry.addLine("Volley test: " + name + " — spinning up...");
        telemetry.update();
        double spinEnd = getRuntime() + 4.0;
        while (opModeIsActive() && getRuntime() < spinEnd) {
            motor.setPower(c.power(TARGET_VELOCITY, motor.getVelocity()));
        }

        for (int shot = 0; shot < NUM_SHOTS && opModeIsActive(); shot++) {
            // Simulate the energy a game piece steals: brief power cut
            double cutEnd = getRuntime() + SHOT_DISTURBANCE_S;
            while (opModeIsActive() && getRuntime() < cutEnd) motor.setPower(0);

            // Measure recovery under closed-loop control
            double t0 = getRuntime();
            double recoveredAt = -1;
            while (opModeIsActive() && getRuntime() - t0 < 3.0) {
                double v = motor.getVelocity();
                motor.setPower(c.power(TARGET_VELOCITY, v));
                if (Math.abs(v - TARGET_VELOCITY) / TARGET_VELOCITY < RECOVERY_TOLERANCE) {
                    recoveredAt = getRuntime() - t0;
                    break;
                }
            }
            result.recoveryTimes[shot] = recoveredAt;

            // Hold at target between shots; sample velocity for ripple metric
            double holdEnd = getRuntime() + INTER_SHOT_DELAY_S;
            while (opModeIsActive() && getRuntime() < holdEnd) {
                double v = motor.getVelocity();
                motor.setPower(c.power(TARGET_VELOCITY, v));
                holdSamples.add(v);
            }
        }
        spinDown();

        result.holdStdDev = stdDev(holdSamples);
        return result;
    }

    double stdDev(List<Double> xs) {
        if (xs.size() < 2) return 0;
        double mean = 0;
        for (double x : xs) mean += x;
        mean /= xs.size();
        double ss = 0;
        for (double x : xs) ss += (x - mean) * (x - mean);
        return Math.sqrt(ss / (xs.size() - 1));
    }

    String fmtRecovery(double t) {
        return t < 0 ? "FAIL" : String.format("%.3fs", t);
    }

    static class VolleyResult {
        double[] recoveryTimes;
        double holdStdDev;
    }

    /** Least-squares linear fit of power vs velocity: power = kV*v + kS */
    void fitFeedforward(List<double[]> samples) {
        int n = samples.size();
        double sx = 0, sy = 0, sxx = 0, sxy = 0;
        for (double[] s : samples) {
            sx += s[0]; sy += s[1];
            sxx += s[0] * s[0]; sxy += s[0] * s[1];
        }
        kV = (n * sxy - sx * sy) / (n * sxx - sx * sx);
        kS = (sy - kV * sx) / n;
        if (kS < 0) kS = 0; // static friction can't be negative; noise guard
    }

    /** Run a closed-loop step to target; measure overshoot % and settle time.
     *  Peak/band checks use a 5-sample moving average so encoder noise spikes
     *  (easily 2-3% of target) don't register as false overshoot. */
    StepResult runStepResponse(double target, double timeoutS) {
        double t0 = getRuntime();
        double peak = 0;
        double settledAt = -1;
        double[] window = new double[5];
        int wi = 0, wn = 0;
        while (opModeIsActive() && getRuntime() - t0 < timeoutS) {
            double v = motor.getVelocity();
            motor.setPower(feedforwardPlusP(target, v)); // control on RAW velocity

            // Moving-average filter for MEASUREMENT only
            window[wi] = v;
            wi = (wi + 1) % window.length;
            if (wn < window.length) wn++;
            double vf = 0;
            for (int k = 0; k < wn; k++) vf += window[k];
            vf /= wn;

            peak = Math.max(peak, vf);
            boolean inBand = Math.abs(vf - target) / target < 0.02;
            if (inBand && settledAt < 0) settledAt = getRuntime() - t0;
            if (!inBand) settledAt = -1; // must STAY in band
        }
        StepResult r = new StepResult();
        r.overshootPct = Math.max(0, (peak - target) / target * 100.0);
        r.settleTime = settledAt < 0 ? timeoutS : settledAt;
        return r;
    }

    void runToTarget(double target, double seconds) {
        double end = getRuntime() + seconds;
        while (opModeIsActive() && getRuntime() < end) {
            motor.setPower(feedforwardPlusP(target, motor.getVelocity()));
        }
    }

    double averageVelocity(double seconds) {
        double end = getRuntime() + seconds, sum = 0;
        int count = 0;
        while (opModeIsActive() && getRuntime() < end) {
            sum += motor.getVelocity();
            count++;
        }
        return count > 0 ? sum / count : 0;
    }

    void spinDown() {
        motor.setPower(0);
        ElapsedTime t = new ElapsedTime();
        while (opModeIsActive() && (motor.getVelocity() > 50 || t.seconds() < 1.5)) {
            telemetry.addData("Spinning down", "%.0f t/s", motor.getVelocity());
            telemetry.update();
            if (t.seconds() > 8) break;
        }
    }

    void waitSeconds(double s, String msg) {
        double end = getRuntime() + s;
        while (opModeIsActive() && getRuntime() < end) {
            telemetry.addLine(msg);
            telemetry.addData("velocity", "%.0f t/s", motor.getVelocity());
            telemetry.update();
        }
    }

    void waitForButtonA() {
        while (opModeIsActive() && !gamepad1.a) idle();
        while (opModeIsActive() && gamepad1.a) idle(); // debounce
    }

    static class StepResult {
        double overshootPct;
        double settleTime;
    }
}
