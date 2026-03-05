package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.ElsiTelemetry.telemetry;

import com.pedropathing.ftc.FTCCoordinates;
import com.pedropathing.geometry.PedroCoordinates;
import com.pedropathing.geometry.Pose;
import com.pedropathing.math.Vector;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

public class ElsiVision {

    private static Limelight3A limelight;

    private static final double METERS_TO_INCHES = 39.3701;

    private final KalmanFilter xFilter;
    private final KalmanFilter yFilter;

    private static Pose lastPose;

    public static void init (HardwareMap hardwareMap) {
        try {
            limelight = hardwareMap.get(Limelight3A.class, "limelight");

            //limelight.pipelineSwitch(0);


            limelight.start();
        } catch (Exception e) {
            //Limelight not found
        }
    }

    public static void loop () {
        try {
            if(limelight == null) return;

            LLResult result = limelight.getLatestResult();
            Pose3D botpose3D = result.getBotpose();
            Pose currentPose = ElsiDrivetrain.follower.getPose();


            if (result != null && result.isValid()
                    && botpose3D != null
                    && result.getBotposeTagCount() > 0) {

                Vector velocity = ElsiDrivetrain.follower.getVelocity();
                double angularVelocity = ElsiDrivetrain.follower.getAngularVelocity();

                double linearVelocity = Math.hypot(
                        velocity.getXComponent(),
                        velocity.getYComponent()
                );

                if (linearVelocity < 2 && Math.abs(angularVelocity) < 2) {

                    Pose limelightPose = convertLimelightToPedro(
                            botpose3D.getPosition().x,
                            botpose3D.getPosition().y,
                            currentPose.getHeading()
                    );

                    telemetry.addData("Location from Limelight", limelightPose.getX() + ", " + limelightPose.getY());
                    //ElsiDrivetrain.follower.setPose(limelightPose);
                }
            }


        } catch (Exception e) {

        }
    }

    public static Pose getRobotPosFromTarget() {
        double yaw = ElsiPinpoint.getHeading();

        limelight.updateRobotOrientation(yaw);

        LLResult result = limelight.getLatestResult();

        //double heading = current.getAsCoordinateSystem(FTCCoordinates.INSTANCE).getHeading();

        if (result != null && result.isValid()) {
            Pose3D robotPosMT2 = result.getBotpose_MT2();
            Pose3D robotPosMT1 = result.getBotpose();

            if (robotPosMT1 != null) {
                telemetry.addData("MT1 X", robotPosMT1.getPosition().x);
                telemetry.addData("MT1 Y", robotPosMT1.getPosition().y);
                double angle = robotPosMT1.getOrientation().getYaw(AngleUnit.DEGREES);
                Pose translatedMT1 = new Pose(robotPosMT1.getPosition().x, robotPosMT1.getPosition().y, Math.toRadians(angle), FTCCoordinates.INSTANCE).getAsCoordinateSystem(PedroCoordinates.INSTANCE);
                telemetry.addData("MT1 Translated Pose", translatedMT1.getX() + ", " + translatedMT1.getY() + ", " + Math.toDegrees(translatedMT1.getHeading()));
            }
            if(robotPosMT2 != null) {
                telemetry.addData("MT2 X", robotPosMT2.getPosition().x);
                telemetry.addData("MT2 Y", robotPosMT2.getPosition().y);
                Pose translatedMT2 = new Pose(robotPosMT2.getPosition().x, robotPosMT2.getPosition().y, robotPosMT2.getOrientation().getYaw(AngleUnit.RADIANS), FTCCoordinates.INSTANCE).getAsCoordinateSystem(PedroCoordinates.INSTANCE);
                telemetry.addData("MT2 Translated Pose", translatedMT2.getX() + ", " + translatedMT2.getY() + ", " + Math.toDegrees(translatedMT2.getHeading()));
            }

            /*
            double angle = robotPosMT1.getOrientation().getYaw(AngleUnit.DEGREES);

            if (angle > 360)
                angle -= 360;
            if(angle < 0)
                angle += 360;

             */

            return new Pose();

            /*
            return new Pose(robotPosMT1.getPosition().y / 0.0254 + 70.625, -robotPosMT1.getPosition().x / 0.0254 + 70.625,
                    Math.toRadians(angle));

             */


        }

        return null;
    }

    public ElsiVision(HardwareMap hardwareMap, Pose initialPose) {
        this.xFilter = new KalmanFilter(0.2, 0.5, initialPose.getX());
        this.yFilter = new KalmanFilter(0.2, 0.5, initialPose.getY());
        init(hardwareMap);
    }


    public Pose getFusedPose(Pose limelightPose, double currentHeading) {
        double fusedX = xFilter.update(limelightPose.getX());
        double fusedY = yFilter.update(limelightPose.getY());

        return new Pose(fusedX, fusedY, currentHeading);
    }


    public static Pose convertLimelightToPedro(double limelightX,
                                               double limelightY,
                                               double currentHeading) {

        double xInches = limelightX * METERS_TO_INCHES;
        double yInches = limelightY * METERS_TO_INCHES;

        double pedroX = yInches + 72;
        double pedroY = -xInches + 72;

        return new Pose(pedroX, pedroY, currentHeading);
    }


    private static class KalmanFilter {
        private final double processNoise;
        private final double measurementNoise;

        private double estimate;
        private double errorCovariance = 1.0;

        public KalmanFilter(double processNoise,
                            double measurementNoise,
                            double initialEstimate) {
            this.processNoise = processNoise;
            this.measurementNoise = measurementNoise;
            this.estimate = initialEstimate;
        }


        public double update(double measurement) {
            errorCovariance += processNoise;

            double kalmanGain =
                    errorCovariance / (errorCovariance + measurementNoise);

            estimate = estimate + kalmanGain * (measurement - estimate);
            errorCovariance = (1.0 - kalmanGain) * errorCovariance;

            return estimate;
        }
    }
}
