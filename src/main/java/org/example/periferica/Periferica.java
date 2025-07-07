package org.example.periferica;

import java.time.LocalDate;

public class Periferica {
    private int idContratto;
    private int idPeriferica;
    private int idStatoPeriferica;

    private String stato;
    private String causale;
    private String note;
    private String causale_disattivazione;
    private String ubicazione;

    private LocalDate dataInizio;
    private LocalDate dataPrimoCollaudo;
    private LocalDate dataCollaudo;
    private LocalDate dataDisattivazione;
    private LocalDate dataDisinstallazione;

    // Costruttore con tutti i parametri
    public Periferica(int idContratto, int idPeriferica, int idStatoPeriferica, String stato,
                      String causale, String causale_disattivazione, String ubicazione,
                      LocalDate dataInizio, LocalDate dataPrimoCollaudo, LocalDate dataCollaudo,
                      LocalDate dataDisattivazione, LocalDate dataDisinstallazione,String note) {
        this.idContratto = idContratto;
        this.idPeriferica = idPeriferica;
        this.idStatoPeriferica = idStatoPeriferica;
        this.stato = stato;
        this.causale = causale;
        this.causale_disattivazione = causale_disattivazione;
        this.ubicazione = ubicazione;
        this.dataInizio = dataInizio;
        this.dataPrimoCollaudo = dataPrimoCollaudo;
        this.dataCollaudo = dataCollaudo;
        this.dataDisattivazione = dataDisattivazione;
        this.dataDisinstallazione = dataDisinstallazione;
        this.note = note;
    }

    // Getter e Setter

    public int getIdContratto() {
        return idContratto;
    }

    public void setIdContratto(int idContratto) {
        this.idContratto = idContratto;
    }

    public int getIdPeriferica() {
        return idPeriferica;
    }

    public void setIdPeriferica(int idPeriferica) {
        this.idPeriferica = idPeriferica;
    }

    public int getIdStatoPeriferica() {
        return idStatoPeriferica;
    }

    public void setIdStatoPeriferica(int idStatoPeriferica) {
        this.idStatoPeriferica = idStatoPeriferica;
    }

    public String getStato() {
        return stato;
    }

    public void setStato(String stato) {
        this.stato = stato;
    }

    public String getCausale() {
        return causale;
    }

    public void setCausale(String causale) {
        this.causale = causale;
    }

    public String getCausaleDisattivazione() {
        return causale_disattivazione;
    }

    public void setCausaleDisattivazione(String causale_disattivazione) {
        this.causale_disattivazione = causale_disattivazione;
    }

    public String getUbicazione() {
        return ubicazione;
    }

    public void setUbicazione(String ubicazione) {
        this.ubicazione = ubicazione;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
    }

    public LocalDate getDataPrimoCollaudo() {
        return dataPrimoCollaudo;
    }

    public void setDataPrimoCollaudo(LocalDate dataPrimoCollaudo) {
        this.dataPrimoCollaudo = dataPrimoCollaudo;
    }

    public LocalDate getDataCollaudo() {
        return dataCollaudo;
    }

    public void setDataCollaudo(LocalDate dataCollaudo) {
        this.dataCollaudo = dataCollaudo;
    }

    public LocalDate getDataDisattivazione() {
        return dataDisattivazione;
    }

    public void setDataDisattivazione(LocalDate dataDisattivazione) {
        this.dataDisattivazione = dataDisattivazione;
    }

    public LocalDate getDataDisinstallazione() {
        return dataDisinstallazione;
    }

    public void setDataDisinstallazione(LocalDate dataDisinstallazione) {
        this.dataDisinstallazione = dataDisinstallazione;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}