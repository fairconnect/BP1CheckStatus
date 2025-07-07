package org.example.contratto;

import java.time.LocalDate;
import static org.example.EnumTypes.EnumStatoContratto;

public class StatoContratto extends Mov {

    private EnumStatoContratto statoContratto; // Attributo aggiuntivo
    private boolean crossCompagnia;
    private int contrattiStato;
    private int statoPolizzeStato;
    private int invioI;
    private int interventiInstallazioneInCorso;
    private int interventiDisinstallazioneInCorso;
    private int interventiManutenzioneSostituzioneInCorso;
    private String ultimoInterventoEseguito;
    private LocalDate dataUltimoIntervento;

    public StatoContratto(Mov mov, EnumStatoContratto statoContratto, boolean crossCompagnia, int contrattiStato, int statoPolizzeStato, int invioI, int interventiInstallazioneInCorso, int interventiDisinstallazioneInCorso, int interventiManutenzioneSostituzioneInCorso, String ultimoInterventoEseguito, LocalDate dataUltimoIntervento) {
        super(mov.getNumVoucher(),
                mov.getIdTracciato(),
                mov.getIdContratto(),
                mov.getIdUnicoMov(),
                mov.getCodStato(),
                mov.getTipoMovimento(),
                mov.getCodCompagnia(),
                mov.getNumPolizza(),
                mov.getNumPolSostituita(),
                mov.getTarga(),
                mov.getVat(),
                mov.getDataDecorrenza(),
                mov.getDataInizioServizio(),
                mov.getDataFineServizio(),
                mov.getDataArrivo(),
                mov.getNomeFileMovim());
        this.statoContratto = statoContratto;
        this.crossCompagnia = crossCompagnia;
        this.contrattiStato = contrattiStato;
        this.statoPolizzeStato = statoPolizzeStato;
        this.invioI = invioI;
        this.interventiInstallazioneInCorso = interventiInstallazioneInCorso;
        this.interventiDisinstallazioneInCorso = interventiDisinstallazioneInCorso;
        this.interventiManutenzioneSostituzioneInCorso = interventiManutenzioneSostituzioneInCorso;
        this.ultimoInterventoEseguito = ultimoInterventoEseguito;
        this.dataUltimoIntervento = dataUltimoIntervento;
    }

    // Getter
    public EnumStatoContratto getStatoContratto() { return statoContratto; }

    public boolean getCrossCompagnia() {
        return crossCompagnia;
    }

    public int getContrattiStato() {
        return contrattiStato;
    }

    public int getStatoPolizzeStato() {
        return statoPolizzeStato;
    }

    public int getInvioI() { return invioI; }

    public int getInterventiInstallazioneInCorso() { return interventiInstallazioneInCorso; }

    public int getInterventiDisinstallazioneInCorso() { return interventiDisinstallazioneInCorso; }

    public int getInterventiManutenzioneSostituzioneInCorso() { return interventiManutenzioneSostituzioneInCorso; }

    public String getUltimoInterventoEseguito() { return ultimoInterventoEseguito; }

    public LocalDate getDataUltimoIntervento() { return dataUltimoIntervento; }

    // Setter
    public void setStatoContratto(EnumStatoContratto statoContratto) { this.statoContratto = statoContratto; }

    public void setCrossCompagnia(boolean crossCompagnia) { this.crossCompagnia = crossCompagnia; }

    public void setContrattiStato(int contrattiStato) { this.contrattiStato = contrattiStato; }

    public void setStatoPolizzeStato(int statoPolizzeStato) { this.statoPolizzeStato = statoPolizzeStato; }

    public void setInvioI(int invioI) { this.invioI = invioI; }

    public void setInterventiInstallazioneInCorso(int interventiInstallazioneInCorso) { this.interventiInstallazioneInCorso = interventiInstallazioneInCorso; }

    public void setInterventiDisinstallazioneInCorso(int interventiDisinstallazioneInCorso) { this.interventiDisinstallazioneInCorso = interventiDisinstallazioneInCorso; }

    public void setInterventiManutenzioneSostituzioneInCorso(int interventiManutenzioneSostituzioneInCorso) { this.interventiManutenzioneSostituzioneInCorso = interventiManutenzioneSostituzioneInCorso; }

    public void setUltimoInterventoEseguito(String ultimoInterventoEseguito) { this.ultimoInterventoEseguito = ultimoInterventoEseguito; }

    public void setDataUltimoIntervento(LocalDate dataUltimoIntervento) { this.dataUltimoIntervento = dataUltimoIntervento; }
}