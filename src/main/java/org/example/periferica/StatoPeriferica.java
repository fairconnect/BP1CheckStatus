package org.example.periferica;

import org.example.EnumTypes.*;

public class StatoPeriferica extends Periferica {

    private EnumStatoPeriferica statoPeriferica;
    private EnumPeriferica anomaliPeriferica;

    public StatoPeriferica(Periferica periferica, EnumStatoPeriferica statoPeriferica, EnumPeriferica anomaliPeriferica) {
        super(  periferica.getIdContratto(),
                periferica.getIdPeriferica(),
                periferica.getIdStatoPeriferica(),
                periferica.getStato(),
                periferica.getCausale(),
                periferica.getCausaleDisattivazione(),
                periferica.getUbicazione(),
                periferica.getDataInizio(),
                periferica.getDataPrimoCollaudo(),
                periferica.getDataCollaudo(),
                periferica.getDataDisattivazione(),
                periferica.getDataDisinstallazione(),
                periferica.getNote()
        );
        this.statoPeriferica = statoPeriferica;
        this.anomaliPeriferica = anomaliPeriferica;
    }


    public EnumStatoPeriferica getStatoPeriferica() {
        return statoPeriferica;
    }

    public void setStatoPeriferica(EnumStatoPeriferica statoPeriferica) {
        this.statoPeriferica = statoPeriferica;
    }

    public EnumPeriferica getAnomaliPeriferica() { return anomaliPeriferica; }

    public void setAnomaliPeriferica(EnumPeriferica anomaliPeriferica) { this.anomaliPeriferica = anomaliPeriferica; }
}
