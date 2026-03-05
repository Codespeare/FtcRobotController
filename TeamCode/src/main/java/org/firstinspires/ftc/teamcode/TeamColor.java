package org.firstinspires.ftc.teamcode;

public class TeamColor {
    private boolean isBlue = true;
    private boolean isUpper = true;

    public void setBlue () {
        isBlue = true;
    }

    public void setRed () {
        isBlue = false;
    }

    public void setUpper () {
        isUpper = true;
    }

    public void setLower () {
        isUpper = false;
    }

    public boolean isMirror() {
        return !isBlue;
    }

    public boolean isUpper () {
        return isUpper;
    }

    public String getTeamColor () {
        if(isBlue) return "Blue";
        return "Red";
    }

    public String getUpper () {
        if(isUpper) return "Upper";
        return "Lower";
    }
}
