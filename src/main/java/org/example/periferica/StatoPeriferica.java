package org.example.periferica;

import org.example.EnumTypes.EnumStatoPeriferica;

public class StatoPeriferica extends Periferica {

    private EnumStatoPeriferica statoPeriferica;

    public StatoPeriferica(Periferica periferica, EnumStatoPeriferica statoPeriferica) {
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
    }


    public EnumStatoPeriferica getStatoPeriferica() {
        return statoPeriferica;
    }

    public void setStatoPeriferica(EnumStatoPeriferica statoPeriferica) {
        this.statoPeriferica = statoPeriferica;
    }


}
