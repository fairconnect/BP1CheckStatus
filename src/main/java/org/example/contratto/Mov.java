package org.example.contratto;

import java.time.LocalDate;

public class Mov {

    private int numVoucher;
    private int idTracciato;
    private int idContratto;
    private long idUnicoMov;

    private String codStato;
    private String tipoMovimento;
    private String codCompagnia;
    private String numPolizza;
    private String numPolSostituita;
    private String targa;
    private String vat;
    private String nomeFileMovim;

    private LocalDate dataDecorrenza;
    private LocalDate dataInizioServizio;
    private LocalDate dataFineServizio;
    private LocalDate dataArrivo;


    // Costruttore
    public Mov(int numVoucher, int idTracciato, int idContratto,long idUnicoMov, String codStato, String tipoMovimento, String codCompagnia, String numPolizza, String numPolSostituita, String targa, String vat, LocalDate dataDecorrenza, LocalDate dataInizioServizio, LocalDate dataFineServizio, LocalDate dataArrivo, String nomeFileMovim) {
        this.numVoucher = numVoucher;
        this.idTracciato = idTracciato;
        this.idContratto = idContratto;
        this.idUnicoMov = idUnicoMov;
        this.codCompagnia = codCompagnia;
        this.codStato = codStato;
        this.tipoMovimento = tipoMovimento;
        this.numPolizza = numPolizza;
        this.numPolSostituita = numPolSostituita;
        this.targa = targa;
        this.vat = vat;
        this.dataDecorrenza = dataDecorrenza;
        this.dataInizioServizio = dataInizioServizio;
        this.dataFineServizio = dataFineServizio;
        this.dataArrivo = dataArrivo;
        this.nomeFileMovim = nomeFileMovim;
    }


    // Getter e Setter
    public int getIdTracciato() {
        return idTracciato;
    }

    public void setIdTracciato(int idTracciato) {
        this.idTracciato = idTracciato;
    }

    public int getIdContratto() {
        return idContratto;
    }

    public void setIdContratto(int idContratto) {
        this.idContratto = idContratto;
    }

    public long getIdUnicoMov() {
        return idUnicoMov;
    }

    public void setIdUnicoMov(long idUnicoMov) {
        this.idUnicoMov = idUnicoMov;
    }
    public String getCodCompagnia() {
        return codCompagnia;
    }

    public void setCodCompagnia(String codCompagnia) {
        this.codCompagnia = codCompagnia;
    }

    public String getCodStato() {
        return codStato;
    }

    public void setCodStato(String codStato) {
        this.codStato = codStato;
    }

    public String getNumPolizza() {
        return numPolizza;
    }

    public void setNumPolizza(String numPolizza) {
        this.numPolizza = numPolizza;
    }

    public String getTarga() {
        return targa;
    }

    public void setTarga(String targa) {
        this.targa = targa;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public LocalDate getDataDecorrenza() {
        return dataDecorrenza;
    }

    public void setDataDecorrenza(LocalDate dataDecorrenza) {
        this.dataDecorrenza = dataDecorrenza;
    }

    public LocalDate getDataInizioServizio() {
        return dataInizioServizio;
    }

    public void setDataInizioServizio(LocalDate dataInizioServizio) {
        this.dataInizioServizio = dataInizioServizio;
    }

    public LocalDate getDataFineServizio() {
        return dataFineServizio;
    }

    public void setDataFineServizio(LocalDate dataFineServizio) {
        this.dataFineServizio = dataFineServizio;
    }

    public LocalDate getDataArrivo() {
        return dataArrivo;
    }

    public void setDataArrivo(LocalDate dataArrivo) {
        this.dataArrivo = dataArrivo;
    }

    public String getNomeFileMovim() {
        return nomeFileMovim;
    }

    public void setNomeFileMovim(String nomeFileMovim) {
        this.nomeFileMovim = nomeFileMovim;
    }

    public String getNumPolSostituita() {
        return numPolSostituita;
    }

    public void setNumPolSostituita(String numPolSostituita) {
        this.numPolSostituita = numPolSostituita;
    }

    public String getTipoMovimento() {
        return tipoMovimento;
    }

    public void setTipoMovimento(String tipoMovimento) {
        this.tipoMovimento = tipoMovimento;
    }

    public int getNumVoucher() {
        return numVoucher;
    }

    public void setNumVoucher(int numVoucher) {
        this.numVoucher = numVoucher;
    }
}