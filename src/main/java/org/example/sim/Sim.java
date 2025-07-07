package org.example.sim;

public class Sim {

    private int idPeriferica;
    private String iccid;
    private String carrier;
    private String simState;
    private String dataPacketActivity;
    private String statoAtteso;

    public Sim(int idPeriferica, String iccid, String carrier, String simState, String dataPacketActivity,String statoAtteso) {
        this.idPeriferica = idPeriferica;
        this.iccid = iccid;
        this.carrier = carrier;
        this.simState = simState;
        this.dataPacketActivity = dataPacketActivity;
        this.statoAtteso = statoAtteso;
    }

    public Sim(Sim sim) {
        this.idPeriferica = sim.getIdPeriferica();
        this.iccid = sim.getIccid();
        this.carrier = sim.getCarrier();
        this.simState = sim.getSimState();
        this.dataPacketActivity = sim.getDataPacketActivity();
        this.statoAtteso = sim.getStatoAtteso();
    }

    public int getIdPeriferica() {
        return idPeriferica;
    }

    public void setIdPeriferica(int idPeriferica) {
        this.idPeriferica = idPeriferica;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getSimState() {
        return simState;
    }

    public void setSimState(String simState) {
        this.simState = simState;
    }

    public String getDataPacketActivity() {
        return dataPacketActivity;
    }

    public void setDataPacketActivity(String dataPacketActivity) {
        this.dataPacketActivity = dataPacketActivity;
    }

    public String getStatoAtteso() {
        return statoAtteso;
    }

    public void setStatoAtteso(String statoAtteso) {
        this.statoAtteso = statoAtteso;
    }
}
