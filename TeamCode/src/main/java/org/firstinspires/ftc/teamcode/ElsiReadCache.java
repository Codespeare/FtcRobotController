package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.List;

public class ElsiReadCache {
    private static List<LynxModule> allHubs;

    public static void init (HardwareMap hardwareMap) {
        allHubs = hardwareMap.getAll(LynxModule.class);

        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }
    }

    public static void loop () {
        for (LynxModule hub : allHubs) {
            hub.clearBulkCache();
        }
    }
}
