package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.ElsiTelemetry.telemetry;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.seattlesolvers.solverslib.hardware.servos.ServoEx;

import org.firstinspires.ftc.robotcore.external.JavaUtil;

public class ElsiMagazineIndicator {

    private static NormalizedColorSensor colorSensor;

    private static ServoEx indicatorLight1;
    private static ServoEx indicatorLight2;

    private static double Red = 0.28;
    private static double Green = 0.500;


    // 1. Define the window size (e.g., last 10 readings)
    private static final int WINDOW_SIZE = 40;
    private static final boolean[] readings = new boolean[WINDOW_SIZE];
    private static int index = 0;
    private static int count = 0;

    public static void init (HardwareMap hardwareMap) {
        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "color");
        indicatorLight1 = new ServoEx(hardwareMap, "indicatorLight1");
        indicatorLight2 = new ServoEx(hardwareMap, "indicatorLight2");

        indicatorLight1.setCachingTolerance(0);
        indicatorLight2.setCachingTolerance(0);

        indicatorLight1.set(Red);
        indicatorLight2.set(Red);
    }

    public static void loop () {
        // 2. Add the new reading to the buffer
        boolean currentBall = processColor();
        readings[index] = currentBall;
        index = (index + 1) % WINDOW_SIZE;
        if (count < WINDOW_SIZE) count++;

        // 3. Calculate the average (percentage of true)
        double trueCount = 0;
        for (int i = 0; i < count; i++) {
            if (readings[i]) trueCount++;
        }
        double average = trueCount / count;

        // 4. Use a threshold (e.g., 70% true) to trigger the lights
        // This prevents the lights from flickering if the sensor is noisy
        if (average > 0.6) {
            indicatorLight1.set(Green);
            indicatorLight2.set(Green);
        } else {
            indicatorLight1.set(Red);
            indicatorLight2.set(Red);
        }

        telemetry.addData("Ball Confidence: ", average);

    }

    private static boolean processColor () {
        double hue;

        //telemetry.addData("Light Detected", ((OpticalDistanceSensor) colorSensor).getLightDetected());
        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        hue = JavaUtil.colorToHue(colors.toColor());

        boolean ballDetected = hue > 100;

        telemetry.addData("Ball Detected: ", ballDetected);

        return ballDetected;

        //Determining the amount of red, green, and blue
        /*
        telemetry.addData("Red", "%.3f", colors.red);
        telemetry.addData("Green", "%.3f", colors.green);
        telemetry.addData("Blue", "%.3f", colors.blue);

        //Determining HSV and alpha
        telemetry.addData("Hue", JavaUtil.colorToHue(colors.toColor()));
        telemetry.addData("Saturation", "%.3f", JavaUtil.colorToSaturation(colors.toColor()));
        telemetry.addData("Value", "%.3f", JavaUtil.colorToValue(colors.toColor()));
        telemetry.addData("Alpha", "%.3f", colors.alpha);

        //Using hue to detect color
        if(hue < 30){
            telemetry.addData("Color", "Red");
        }
        else if (hue < 60) {
            telemetry.addData("Color", "Orange");
        }
        else if (hue < 90){
            telemetry.addData("Color", "Yellow");
        }
        else if (hue < 150){
            telemetry.addData("Color", "Green");
        }
        else if (hue < 225){
            telemetry.addData("Color", "Blue");
        }
        else if (hue < 350){
            telemetry.addData("Color", "Purple");
        }
        else{
            telemetry.addData("Color", "Red");
        }

         */
    }

}
