import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.JavaUtil;

import dev.nextftc.core.commands.Command;
import dev.nextftc.core.commands.utility.LambdaCommand;
import dev.nextftc.core.subsystems.Subsystem;
import dev.nextftc.ftc.ActiveOpMode;

public class MagazineSubsystem implements Subsystem {
    public static final MagazineSubsystem INSTANCE = new MagazineSubsystem();
    private MagazineSubsystem() { }

    private NormalizedColorSensor colorSensor;

    private ServoEx indicatorLight1,indicatorLight2;

    private final double Red = 0.28;
    private final double Green = 0.500;

    private final int WINDOW_SIZE = 30;
    private final boolean[] readings = new boolean[WINDOW_SIZE];
    private int index = 0;
    private int count = 0;

    private boolean ballDetected = false;

    public boolean getBallDetected () {
        return ballDetected;
    }

    public Command runToBallDetected () {
        return new LambdaCommand()
                .setIsDone(()->{return ballDetected;});
    }

    @Override
    public void initialize() {
        // initialization logic (runs on init)

        indicatorLight1 = new ServoEx(ActiveOpMode.hardwareMap(), "light1");
        indicatorLight2 = new ServoEx(ActiveOpMode.hardwareMap(), "light2");

        colorSensor = ActiveOpMode.hardwareMap().get(NormalizedColorSensor.class, "color");

        indicatorLight1.set(Red);
        indicatorLight2.set(Red);
    }

    @Override
    public void periodic() {
        // periodic logic (runs every loop)

        // Add the new reading to the buffer
        boolean currentBall = processColor();
        readings[index] = currentBall;
        index = (index + 1) % WINDOW_SIZE;
        if (count < WINDOW_SIZE) count++;

        // Calculate the average (percentage of true)
        double trueCount = 0;
        for (int i = 0; i < count; i++) {
            if (readings[i]) trueCount++;
        }
        double average = trueCount / count;

        // Use a threshold (e.g., 70% true) to trigger the lights
        // This prevents the lights from flickering if the sensor is noisy
        if (average > 0.6) {
            ballDetected = true;
            indicatorLight1.set(Green);
            indicatorLight2.set(Green);
        } else {
            ballDetected = false;
            indicatorLight1.set(Red);
            indicatorLight2.set(Red);
        }
    }

    private boolean processColor () {
        double hue;
        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        hue = JavaUtil.colorToHue(colors.toColor());
        return hue > 100;
    }

}
