package org.example.contratto;

import java.time.LocalDate;

public class Intervento {

    private String tipoIntervento;
    private LocalDate dataIntervento;

    public Intervento(String tipoIntervento, LocalDate dataIntervento) {
        this.tipoIntervento = tipoIntervento;
        this.dataIntervento = dataIntervento;
    }

    public String getTipoIntervento() {
        return tipoIntervento;
    }

    public LocalDate getDataIntervento() {
        return dataIntervento;
    }

}
