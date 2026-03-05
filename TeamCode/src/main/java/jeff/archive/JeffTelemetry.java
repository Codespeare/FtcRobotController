package jeff.archive;

//import com.acmerobotics.dashboard.FtcDashboard;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class JeffTelemetry {
    //public static FtcDashboard dashboard = FtcDashboard.getInstance();
    //public static Telemetry dashboardTelemetry = dashboard.getTelemetry();
    //public static TelemetryManager telemetry;
    public static Telemetry telemetry;

    public static void init (Telemetry tel) {
        //telemetry = PanelsTelemetry.INSTANCE.getTelemetry();
        telemetry = tel;
    }
    public static void loop () {
        telemetry.update();
    }
}
