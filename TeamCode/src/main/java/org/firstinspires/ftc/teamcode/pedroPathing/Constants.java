package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.control.PredictiveBrakingCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    public static FollowerConstants followerConstants = new FollowerConstants()
            //.mass(11.476)
            //.forwardZeroPowerAcceleration(-39.13)
            //.lateralZeroPowerAcceleration(-63.104)
            .predictiveBrakingCoefficients(new PredictiveBrakingCoefficients(0.1, 0.06761423961392848, 0.0014984846074243597))
            //.translationalPIDFCoefficients(new PIDFCoefficients(0.008, 0, 0.0, 0.08))
            .headingPIDFCoefficients(new PIDFCoefficients(2, 0, 0.0, 0.024)) // p = 1.5?
            //.drivePIDFCoefficients(new FilteredPIDFCoefficients(0.008,0.0,0.000,0.6,0.08))
            .centripetalScaling(0.0);
    ;

    //public static PathConstraints pathConstraints = new PathConstraints(0.995, 0.1, 0.1, 0.005,100,.8,10,1);

    public static MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1)
            .rightFrontMotorName("frontRight")
            .rightRearMotorName("backRight")
            .leftRearMotorName("backLeft")
            .leftFrontMotorName("frontLeft")
            .useBrakeModeInTeleOp(true)
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .xVelocity(70.618)
            .yVelocity(49.583);


    public static PinpointConstants localizerConstants = new PinpointConstants()
            .distanceUnit(DistanceUnit.MM)
            //.forwardPodY(125.528) // Elsi
            //.strafePodX(-194.607) // Elsi
            //.forwardPodY(-103.214) // Opal
            //.strafePodX(-83.511) // Opal
            .forwardPodY(-83.511) // Opal
            .strafePodX(-103.214) // Opal
            //.forwardPodY(-88.45) // Opal -3.4813in
            //.strafePodX(-104.7877) // Opal -4.1255in
            .hardwareMapName("pinpoint")
            .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
            .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.REVERSED)
            .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD);


    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                //.pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .pinpointLocalizer(localizerConstants)
                .build();
    }
}