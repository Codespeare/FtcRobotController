package jeff.archive;

public class PIDF {
    public double PIDF_F = 0.00020;
    public double PIDF_P = 0.06;
    public double PIDF_I = 0.006;
    public double PIDF_D = 0.000;

    public double P() {
        return PIDF_P;
    }
    public double I() {
        return PIDF_I;
    }
    public double D() {
        return PIDF_D;
    }
    public double F() {
        return PIDF_F;
    }

    public PIDF(double P, double I, double D, double F){
        setPIDF(P,I,D,F);
    }

    public PIDF(){}

    public void setPIDF(double P, double I, double D, double F){
        setP(P);
        setI(I);
        setD(D);
        setF(F);
    }

    public void setP(double P) {
        PIDF_P = P;
    }

    public void setI(double I) {
        PIDF_I = I;
    }

    public void setD(double D) {
        PIDF_D = D;
    }

    public void setF(double F) {
        PIDF_F = F;
    }

}
