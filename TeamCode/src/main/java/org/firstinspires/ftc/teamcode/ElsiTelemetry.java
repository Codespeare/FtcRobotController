package org.firstinspires.ftc.teamcode;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ElsiTelemetry {
    public static Telemetry telemetry;
    public static TelemetryManager panelsTelemetry;

    public static void init (Telemetry tel) {
        telemetry = tel;
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();
    }

    public static void loop () {
        telemetry.update();
        panelsTelemetry.update(telemetry);
    }
}
