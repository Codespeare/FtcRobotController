package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.ElsiTelemetry.telemetry;

import com.pedropathing.geometry.Pose;
import com.seattlesolvers.solverslib.util.InterpLUT;

public class ElsiRangefind {

    private final InterpLUT rangePowerTable = new InterpLUT();

    private double currentPower = 0;

    public static Pose goalPose = new Pose(10,135);

    ElsiRangefind() {
        double longPower = 800;
        double basePower = 700;
        double longIncrement = 4;
        double increment = 1.9; //Maybe 5?
        rangePowerTable.add(0,0); //Absolute Minimum Input
        rangePowerTable.add(50,basePower);
        rangePowerTable.add(55,basePower); //About 30% of shots successful
        rangePowerTable.add(60,basePower); //Actual Minimum Shooting Distance
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

    public void setGoal (TeamColor t) {
        if(t.isMirror()) {
            goalPose = new Pose(10,135).mirror();
        } else {
            goalPose = new Pose(10,135);
        }
    }

    public double getCurrentPower () {
        return currentPower;
    }

    public double getPowerByRange (double range) {
        double power = rangePowerTable.get(range);
        telemetry.addData("Power from Range", power);
        currentPower = power;
        return power;
    }

    public static double getRangeToGoal (Pose current, Pose goal) {
        //double distance = current.distanceFrom(ElsiDrivetrain.goalPose);

        double x2 = goal.getX();
        double y2 = goal.getY();
        double x1 = current.getX();
        double y1 = current.getY();

        double distance = Math.hypot(x2 - x1, y2 - y1);

        telemetry.addData("Range from Goal", distance);

        return distance;
    }

    public static double getTargetAngle (Pose current, Pose goal) {
        //Returns Radians
        double dy = goal.getY() - current.getY();
        double dx = goal.getX() - current.getX();
        double angle = Math.atan2(dy, dx);

        //angle -= Math.PI / 2.0;

        //angle = normalizeAngle(angle);
        telemetry.addData("Computed Target Angle (Rad)", angle);
        return angle;
        //return normalizeAngle(Math.atan2(dy, dx));
    }

    public static double normalizeAngle (double angleRadians) {
        double angle = angleRadians % (Math.PI * 2D);
        if(angle <= -Math.PI) angle += Math.PI * 2D;
        if(angle > Math.PI) angle -= Math.PI * 2D;
        return angle;
    }
}
