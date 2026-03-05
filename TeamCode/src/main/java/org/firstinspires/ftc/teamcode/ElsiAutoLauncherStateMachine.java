package org.firstinspires.ftc.teamcode;

import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class ElsiAutoLauncherStateMachine {

    private static boolean fireRequested = false;

    private static Timer actionTimer;
    private static final long waitTime = 5 * 1000;

    public static void init (HardwareMap hardwareMap) {
        ElsiTransfer.init(hardwareMap, true);
        ElsiLauncher.init(hardwareMap, true);
        actionTimer = new Timer();
    }

    public static void loop () {
        ElsiTransfer.loop();
        ElsiLauncher.loop();

        if(fireRequested && !ElsiLauncher.autoLauncherEnabled) {
            ElsiLauncher.autoLauncherEnabled = true;
            actionTimer = new Timer();
        } else if (!fireRequested) {
            ElsiLauncher.autoLauncherEnabled = false;
            ElsiTransfer.autoLaunchOn = false;
        }

        if(fireRequested && ElsiLauncher.launchReady()) {
            ElsiTransfer.autoLaunchOn = true;
        }

        if(actionTimer.getElapsedTime() > waitTime) {
            fireRequested = false;
        }
    }

    public static boolean isBusy () {
        return fireRequested;
    }

    public static void requestFire (double velocity) {
        fireRequested = true;
        ElsiLauncher.targetVelocity = velocity;
    }
}
