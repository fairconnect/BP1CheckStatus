package org.example.contratto;

public class TrasmissioneDati {
    private int isDataRawActive;
    private int active;
    private String diffIsDataRawActiveActive;

    private int recordState;
    private int statoComunicazione;
    private String diffRecordStateStatoCom;

    private String statoAllineamento;

    public TrasmissioneDati(int isDataRawActive, int active, String diffIsDataRawActiveActive,
                            int recordState, int statoComunicazione, String diffRecordStateStatoCom,
                            String statoAllineamento) {
        this.isDataRawActive = isDataRawActive;
        this.active = active;
        this.diffIsDataRawActiveActive = diffIsDataRawActiveActive;
        this.recordState = recordState;
        this.statoComunicazione = statoComunicazione;
        this.diffRecordStateStatoCom = diffRecordStateStatoCom;
        this.statoAllineamento = statoAllineamento;
    }

    public int getIsDataRawActive() {
        return isDataRawActive;
    }

    public void setIsDataRawActive(int isDataRawActive) {
        this.isDataRawActive = isDataRawActive;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public String getDiffIsDataRawActiveActive() {
        return diffIsDataRawActiveActive;
    }

    public void setDiffIsDataRawActiveActive(String diffIsDataRawActiveActive) {
        this.diffIsDataRawActiveActive = diffIsDataRawActiveActive;
    }

    public int getRecordState() {
        return recordState;
    }

    public void setRecordState(int recordState) {
        this.recordState = recordState;
    }

    public int getStatoComunicazione() {
        return statoComunicazione;
    }

    public void setStatoComunicazione(int statoComunicazione) {
        this.statoComunicazione = statoComunicazione;
    }

    public String getDiffRecordStateStatoCom() {
        return diffRecordStateStatoCom;
    }

    public void setDiffRecordStateStatoCom(String diffRecordStateStatoCom) {
        this.diffRecordStateStatoCom = diffRecordStateStatoCom;
    }

    public String getStatoAllineamento() {
        return statoAllineamento;
    }

    public void setStatoAllineamento(String statoAllineamento) {
        this.statoAllineamento = statoAllineamento;
    }
}