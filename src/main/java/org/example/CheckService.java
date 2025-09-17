package org.example;

import org.example.contratto.*;
import org.example.periferica.Periferica;
import org.example.periferica.PerifericaDAO;
import org.example.periferica.StatoPeriferica;
import org.example.sim.Sim;
import org.example.sim.SimDAO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.example.EnumTypes.*;

public class CheckService {

    private final ContrattoDAO contrattoDAO = new ContrattoDAO();
    private final PerifericaDAO perifericaDAO = new PerifericaDAO();
    private final SimDAO simDAO = new SimDAO();

    public CheckService() {}


    public int getIdPerifericaBySim(String sim) {
        return simDAO.findIdPerifericaByICCID(sim);
    }

    public int getIdContratto(int idPeriferica) {
        int idContratto = 0;

        List<Periferica> perifericheCompleta = perifericaDAO.findPerifericheById(idPeriferica);
        List<Periferica> perifericheExtCompleta = perifericaDAO.findPerifericheExtById(idPeriferica);

        if (!perifericheCompleta.isEmpty()) {
            int countStateA= 0;
            for (Periferica periferica : perifericheExtCompleta) {
                if ("A".equals(periferica.getStato())) {
                    idContratto = periferica.getIdContratto();
                    countStateA++;
                }
            }
            if (countStateA == 1) {
                return idContratto;
            } else if (countStateA > 1) {
                return -3;
            } else if (countStateA == 0) {
                idContratto = perifericheCompleta.get(perifericheCompleta.size() - 1).getIdContratto();
            }
        }
        return idContratto;
    }

    public void step1(int idContratto, String datoIniziale) {
        //System.out.println("INIZIO step1:" + idContratto);

        StatoContratto statoContratto = new StatoContratto(new Mov(0,0,0,0,"","","","","","","",null,null,null,null, ""), EnumStatoContratto.AMBIGUO,null, -1, -1,0, 0, 0, 0,"",null);

        List<String> compMov = contrattoDAO.findCompagnieByIdContratto(idContratto);
        if (compMov.size() > 1) {
            statoContratto.setAnomaliaContratto(EnumContratto.CROSSCOMPAGNIA);
        }
        String compagnia = (compMov != null && !compMov.isEmpty() && compMov.get(0) != null) ? compMov.get(0) : "";
        if ("MES".equalsIgnoreCase(compagnia) || "CDS".equalsIgnoreCase(compagnia)) {
            statoContratto = checkStatoContratto(idContratto);
        } else if ("057".equalsIgnoreCase(compagnia) || "057G".equalsIgnoreCase(compagnia) || "057J".equalsIgnoreCase(compagnia) || "247".equalsIgnoreCase(compagnia)
                    || "429".equalsIgnoreCase(compagnia) || "429J".equalsIgnoreCase(compagnia) || "014".equalsIgnoreCase(compagnia)) {
                statoContratto = contrattiJeniot(idContratto);
        } else if ("440".equalsIgnoreCase(compagnia)) {
            statoContratto = contrattiQuixa(idContratto);
        } else {
            statoContratto.setAnomaliaContratto(EnumContratto.COMPAGNIANONGESTITA);
        }
        //System.out.println("FINE step1:" + idContratto);
        step2(statoContratto, datoIniziale);
    }

    public void step2(StatoContratto statoContratto, String datoIniziale) {
        //System.out.println("INIZIO step2");

        StatoPeriferica statoPeriferica = checkStatoPeriferica(statoContratto.getIdContratto());

        //System.out.println("FINE step2");

        step3(statoContratto, statoPeriferica, datoIniziale);
    }

    public void step3(StatoContratto statoContratto, StatoPeriferica statoPeriferica, String datoIniziale) {
        //System.out.println("INIZIO step3");
        ///Recupero i valori dalla tabella contratti e statopolizze
        statoContratto.setContrattiStato(contrattoDAO.findStatoContrattoById(statoContratto.getIdContratto()));
        statoContratto.setStatoPolizzeStato(contrattoDAO.findStatoPolizzeByIdContratto(statoContratto.getIdContratto()));

        /// Verifica rawdata
        TrasmissioneDati rawData = null;
        if (statoContratto.getNumVoucher() != 0) {
            rawData = contrattoDAO.verificaRaw(statoContratto.getNumVoucher());
        }
        //Verifica UbicazionePeriferica
        String posizionePeriferica = perifericaDAO.trovaClienteDealerByUbicazione(String.valueOf(statoPeriferica.getUbicazione()));

        boolean aggContratto = false;
        boolean aggPeriferica = false;
        String azionePeriferica = "";
        if (EnumStatoContratto.SCADUTO.equals(statoContratto.getStatoContratto()) || EnumStatoContratto.STORNATO.equals(statoContratto.getStatoContratto())) {
            if (2 == statoContratto.getContrattiStato() || 1 == statoContratto.getStatoPolizzeStato()) {
                //contrattoDAO.updateStatoContrattoById(statoContratto.getIdContratto(), 0);
                //contrattoDAO.updateStatoPolizzeByIdContratto(statoContratto.getIdContratto(), 3);
                aggContratto = true;
            }
            if (statoPeriferica.getStatoPeriferica().equals(EnumStatoPeriferica.ATTIVA)) {
                //perifericaDAO.updatePerifericaById(statoPeriferica.getIdPeriferica(), statoPeriferica.getIdStatoPeriferica(), "D");
                aggPeriferica = true;
                azionePeriferica = "Da disattivare";
            } else if (statoPeriferica.getStatoPeriferica().equals(EnumStatoPeriferica.ABBINATA)) {
                aggPeriferica = true;
                azionePeriferica = "Da rimuovere abbinamento";
            }
        } else if (EnumStatoContratto.ATTIVO.equals(statoContratto.getStatoContratto())) {
            if (2 != statoContratto.getContrattiStato() || 1 != statoContratto.getStatoPolizzeStato()) {
                aggContratto = true;
            }
            if (statoPeriferica.getStatoPeriferica().equals(EnumStatoPeriferica.DISATTIVA) || statoPeriferica.getStatoPeriferica().equals(EnumStatoPeriferica.DISINSTALLATA)) {
                aggPeriferica = true;
                azionePeriferica = "Da attivare";
            }
        }
        //System.out.println("Fine step3");
        step4(statoContratto, statoPeriferica, aggContratto, aggPeriferica, azionePeriferica, rawData, posizionePeriferica, datoIniziale);
    }

    public void step4(StatoContratto statoContratto, StatoPeriferica statoPeriferica, boolean aggContratto, boolean aggPeriferica,String azionePeriferica,TrasmissioneDati rawData,String posizionePeriferica, String datoIniziale) {
        //System.out.println("INIZIO step4");

        Sim sim = checkSim(statoPeriferica.getIdPeriferica());
        String statoAtteso = "";

        if (sim.getCarrier() != null && sim.getCarrier().equals("VODAFONE")) {
            if (sim.getSimState().equalsIgnoreCase("ACTIVE.LIVE") || sim.getSimState().equalsIgnoreCase("ACTIVE.READY") || sim.getSimState().equalsIgnoreCase("ACTIVE.TEST")) {
                if (!EnumStatoContratto.ATTIVO.equals(statoContratto.getStatoContratto())) {
                    if (!sim.getSimState().equalsIgnoreCase("ACTIVE.TEST")) {
                        statoAtteso = "Sospendi SIM";
                    } else {
                        statoAtteso = "Indifferente SIM (Test)";
                    }
                } else {
                    statoAtteso = "nessunazione";
                }
            } else {
                if (EnumStatoContratto.ATTIVO.equals(statoContratto.getStatoContratto())) {
                    statoAtteso = "Attiva SIM";
                } else {
                    statoAtteso = "nessunazione";
                }
            }
        } else if (sim.getCarrier() != null && sim.getCarrier().equals("TIM")) {
            if (sim.getSimState().equalsIgnoreCase("ACTIVATED") || sim.getSimState().equalsIgnoreCase("ACTIVATION READY") || sim.getSimState().equalsIgnoreCase("TEST READY")) {
                if (!EnumStatoContratto.ATTIVO.equals(statoContratto.getStatoContratto())) {
                    if (!sim.getSimState().equalsIgnoreCase("ACTIVE.TEST")) {
                        statoAtteso = "Sospendi SIM";
                    } else {
                        statoAtteso = "Indifferente SIM (Test)";
                    }
                } else {
                    statoAtteso = "nessunazione";
                }
            } else {
                if (EnumStatoContratto.ATTIVO.equals(statoContratto.getStatoContratto())) {
                    statoAtteso = "Attiva SIM";
                } else {
                    statoAtteso = "nessunazione";
                }
            }
        } else {
            System.err.println("step4 provider sim non riconosciuto" + sim.getCarrier());
        }
        sim.setStatoAtteso(statoAtteso);
        //simDAO.insertSimLog(statoContratto, statoPeriferica, sim, aggiornatoContratto, aggiornatoPeriferica);
        //System.out.println("FINE step4");
        insertLogCSV(statoContratto,statoPeriferica ,sim ,aggContratto, aggPeriferica, azionePeriferica,rawData ,posizionePeriferica, datoIniziale);
    }

    private void step5(StatoContratto statoContratto, StatoPeriferica statoPeriferica) {
        //gestione api sim
    }

    public StatoContratto checkStatoContratto(int idContratto) {
        ///Recupero la lista dei mov legato a un contratto
        List<Mov> contratti = contrattoDAO.findMovByIdContratto(idContratto);

        StatoContratto statoContratto;

        if (contratti.isEmpty()) {
            statoContratto = new StatoContratto(new Mov(0,0,0,0,"","","","","","","",null,null,null,null, ""), EnumStatoContratto.AMBIGUO, EnumContratto.NESSUNCONTRATTO, -1, -1,0,0,0,0,"",null);
        } else {
            statoContratto = new StatoContratto(contratti.get(0), EnumStatoContratto.AMBIGUO, null, -1, -1,0,0,0,0,"",null);

            for (Mov contratto : contratti) {
                if (contratto.getIdTracciato() > statoContratto.getIdTracciato()) {
                    statoContratto = new StatoContratto(contratto, EnumStatoContratto.AMBIGUO, EnumContratto.IDULTIMOMOVERRATO, -1, -1,0,0,0,0,"",null);
                }
            }

            /*
            String valoreRiferimento = statoContratto.getCodCompagnia();
            for (Mov contratto : contratti) {
                if (!contratto.getCodCompagnia().equals(valoreRiferimento)) {
                    statoContratto.setStatoContratto(EnumStatoContratto.CROSSCOMPAGNIA);
                    statoContratto.setCrossCompagnia(true);//Attenzione a rinominare parametro crossCompagnia
                }
            } */
            ///Determina lo stato in base alla compagnia e la data
            LocalDate oggi = LocalDate.now();
            if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio())) {
                statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
            } else {
                statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
            }
        }

        ///Log statoContratto
        //contrattoDAO.insertContrattoLog(statoContratto);

        return statoContratto;
    }

    public StatoContratto contrattiJeniot(int idContratto) {
        StatoContratto statoContratto;
        LocalDate oggi = LocalDate.now();

        List<Mov> movList = contrattoDAO.findMovGeneraliIdContratto(idContratto);

        if (movList.isEmpty()) {
            statoContratto = new StatoContratto(new Mov(0,0,0,0,"","","","","","","",null,null,null,null, ""), EnumStatoContratto.AMBIGUO, EnumContratto.NESSUNCONTRATTO, -1, -1,0,0,0,0, "", null);
        } else {
            ///Prendo l'ultimo mov per il contratto
            statoContratto = new StatoContratto(movList.get(0), EnumStatoContratto.AMBIGUO, null, -1, -1,0,0,0,0,"",null);
            int emissioni = 0;
            Mov ultimoMovZero = null;

            for (Mov contratto : movList) {
                if (contratto.getIdUnicoMov() > statoContratto.getIdUnicoMov()) {
                    statoContratto = new StatoContratto(contratto, EnumStatoContratto.AMBIGUO, EnumContratto.IDULTIMOMOVERRATO, -1, -1,0,0,0,0,"",null);
                }
                if ("0".equals(statoContratto.getTipoMovimento())) {
                    emissioni++;
                    ultimoMovZero = contratto;
                }
            }
            boolean scarti = contrattoDAO.findScartiGenerali(statoContratto.getTarga());

            if (scarti) {
                statoContratto = new StatoContratto(movList.get(0), EnumStatoContratto.AMBIGUO, EnumContratto.SCARTIDARECUPERARE, -1, -1,0,0,0,0,"",null);
            }
                if ("0".equals(statoContratto.getTipoMovimento()) || "18".equals(statoContratto.getTipoMovimento()) || "14".equals(statoContratto.getTipoMovimento()) || "6".equals(statoContratto.getTipoMovimento()) || "9".equals(statoContratto.getTipoMovimento()) || "13".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio().plusDays(90))) {
                        statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                    } else {
                        statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                    }
                } else if ("11".equals(statoContratto.getTipoMovimento()) || "2".equals(statoContratto.getTipoMovimento()) || "4".equals(statoContratto.getTipoMovimento()) || "7".equals(statoContratto.getTipoMovimento()) || "10".equals(statoContratto.getTipoMovimento())) {
                    if (emissioni <= 1) {
                        if (!oggi.isBefore(statoContratto.getDataDecorrenza())) {
                            statoContratto.setStatoContratto(EnumStatoContratto.STORNATO);
                        } else {
                            statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                            statoContratto.setAnomaliaContratto(EnumContratto.DASTORNARE);
                        }
                    } else {
                        if (!oggi.isBefore(ultimoMovZero.getDataInizioServizio()) && !oggi.isAfter(ultimoMovZero.getDataFineServizio().plusDays(90))) {
                            statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                        } else {
                            statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                        }
                        statoContratto.setAnomaliaContratto(EnumContratto.CASE0011);
                    }
                } else if ("8".equals(statoContratto.getTipoMovimento()) || "17".equals(statoContratto.getTipoMovimento()) || "19".equals(statoContratto.getTipoMovimento()) || "20".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataDecorrenza().plusDays(90))) {
                        statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                    } else {
                        statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                        statoContratto.setAnomaliaContratto(EnumContratto.DASTORNARE);
                    }
                } else if ("5".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio())) {
                        if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio().plusDays(365))) {
                            statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                        } else {
                            statoContratto.setStatoContratto(EnumStatoContratto.SOSPESO);
                        }
                    }
                } else if ("1".equals(statoContratto.getTipoMovimento()) || "3".equals(statoContratto.getTipoMovimento()) || "21".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio().plusDays(90))) {
                        statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                    } else {
                        statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                    }
                } else if ("31".equals(statoContratto.getTipoMovimento()) || "33".equals(statoContratto.getTipoMovimento()) || "41".equals(statoContratto.getTipoMovimento()) || "43".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio().plusDays(90))) {
                        statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                    } else {
                        statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                    }
                } else {
                    if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio().plusDays(90))) {
                        statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                    } else {
                        statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                    }
            }

            /*for (Mov mov : movList) {
                /// Case 2 FIX TIPOMOV
                /*if ("01".equals(mov.getTipoMovimento()) || "03".equals(mov.getTipoMovimento()) || "21".equals(mov.getTipoMovimento()) || "33".equals(mov.getTipoMovimento())) {
                    statoPolizza = EnumStatoPolizza.MODIFICA;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                }
                //vERIFICARE CASE rispetto a ultimomov
                EnumStatoPolizza statoPolizza = EnumStatoPolizza.AMBIGUO;
                /// Case 1
                if ("0".equals(mov.getTipoMovimento()) || "18".equals(mov.getTipoMovimento())) {
                    statoPolizza = EnumStatoPolizza.EMISSIONE;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                    //CheckInstallazione();
                } else if (mov.getNumPolSostituita() != null && !mov.getNumPolSostituita().isEmpty() && !Objects.equals(mov.getNumPolizza(), mov.getNumPolSostituita())) {
                    /// Case 3
                    statoPolizza = EnumStatoPolizza.MODIFICA;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                } else if ("11".equals(mov.getTipoMovimento())) {
                    /// Case 4
                    statoPolizza = EnumStatoPolizza.STORNO;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                } else if ("9".equals(mov.getTipoMovimento())) {
                    /// Case 5 riammisione in vigore
                    statoPolizza = EnumStatoPolizza.RIAMMISSIONE;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                } else if ("14".equals(mov.getTipoMovimento())) {
                    /// Case 5 riammisione in vigore
                    statoPolizza = EnumStatoPolizza.INCORSO;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                } else if ("2".equals(statoContratto.getTipoMovimento()) || "4".equals(statoContratto.getTipoMovimento()) || "7".equals(statoContratto.getTipoMovimento()) || "10".equals(statoContratto.getTipoMovimento())) {
                    statoPolizza = EnumStatoPolizza.SOSPESA;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                }else if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio())) {
                    /// Case 6 in corso
                    statoPolizza = EnumStatoPolizza.INCORSO;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                } else {
                    statoPolizza = EnumStatoPolizza.CASENONIDENTIFICATO;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                }
            }*/

        }
        ///verificare installazione ed invio I
        statoContratto.setInvioI(contrattoDAO.findInvioIByTargaVoucher(statoContratto.getTarga(), statoContratto.getNumVoucher()));


        /// Recupero interventi in corso legati al contratto da modificare, prendere l'intera lista di interventi legati al contratto verificare ogni operazione
        /*statoContratto.setInterventiInstallazioneInCorso(contrattoDAO.findInterventiGenerali(idContratto, "installazioni"));
        statoContratto.setInterventiInstallazioneInCorso(contrattoDAO.findInterventiGenerali(idContratto, "disinstallazioni"));
        statoContratto.setInterventiInstallazioneInCorso(contrattoDAO.findInterventiGenerali(idContratto, "altro"));*/


        //Verificare lista interventi eseguiti
        List<Intervento> interventi = contrattoDAO.findInterventiGenerali(idContratto);



        //Verificare ultimo intervento
        Intervento intervento = contrattoDAO.findUltimoIntervento(idContratto);
        if (intervento != null) {
            statoContratto.setUltimoInterventoEseguito(intervento.getTipoIntervento());
            statoContratto.setDataUltimoIntervento(intervento.getDataIntervento());
        }


        ///Log statoContratto
        //contrattoDAO.insertContrattoLog(statoContratto);

        return statoContratto;
    }

    public StatoContratto contrattiQuixa(int idContratto) {
        StatoContratto statoContratto;
        LocalDate oggi = LocalDate.now();

        List<Mov> movList = contrattoDAO.findMovQuixaIdContratto(idContratto);

        if (movList.isEmpty()) {
            statoContratto = new StatoContratto(new Mov(0,0,0,0,"","","","","","","",null,null,null,null, ""), EnumStatoContratto.AMBIGUO, EnumContratto.NESSUNCONTRATTO, -1, -1,0,0,0,0, "", null);
        } else {
            ///Prendo l'ultimo mov per il contratto
            statoContratto = new StatoContratto(movList.get(0), EnumStatoContratto.AMBIGUO, null, -1, -1, 0, 0, 0, 0, "", null);
            String tipoMovimento = statoContratto.getTipoMovimento();

            if ("NEW".equalsIgnoreCase(tipoMovimento)
                    || "RENEWAL".equalsIgnoreCase(tipoMovimento)
                    || "RECT".equalsIgnoreCase(tipoMovimento)) {
                if (!oggi.isBefore(statoContratto.getDataInizioServizio())
                        && !oggi.isAfter(statoContratto.getDataFineServizio().plusDays(90))) {
                    statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                } else {
                    statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                }
            } else if ("CLOSURE".equalsIgnoreCase(tipoMovimento)) {
                if (!oggi.isBefore(statoContratto.getDataDecorrenza())) {
                    statoContratto.setStatoContratto(EnumStatoContratto.STORNATO);
                } else {
                    statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                    statoContratto.setAnomaliaContratto(EnumContratto.DASTORNARE);
                }
            } else if ("REPLACE".equalsIgnoreCase(tipoMovimento)) {
                if (!oggi.isBefore(statoContratto.getDataDecorrenza())) {
                    statoContratto.setStatoContratto(EnumStatoContratto.STORNATO);
                } else {
                    statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                    statoContratto.setAnomaliaContratto(EnumContratto.DASTORNARE);
                }
            } else if ("SUBVE".equalsIgnoreCase(tipoMovimento)) {
                if (!oggi.isBefore(statoContratto.getDataInizioServizio())
                        && !oggi.isAfter(statoContratto.getDataFineServizio().plusDays(90))) {
                    statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                } else {
                    statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                }
            }
        }
        return statoContratto;
    }

    public StatoPeriferica checkStatoPeriferica(int idContratto) {
        List<Periferica> periferiche = perifericaDAO.findPerifericheByIdContratto(idContratto);
        List<Periferica> perifericheExt = perifericaDAO.findPerifericheExtByIdContratto(idContratto);

        StatoPeriferica statoPeriferica;

        if (periferiche.isEmpty()) {
            statoPeriferica = new StatoPeriferica(new Periferica(0,0,0,"","","",0,null,null,null,null,null,""),EnumStatoPeriferica.AMBIGUA, EnumPeriferica.NESSUNAPERIFERICA);
        } else if (perifericheExt.size() != periferiche.size()) {
            statoPeriferica = new StatoPeriferica(new Periferica(0,0,0,"","","",0,null,null,null,null,null,""),EnumStatoPeriferica.AMBIGUA, EnumPeriferica.SANAREEXT);
        } else {
            statoPeriferica = new StatoPeriferica(perifericheExt.get(perifericheExt.size() - 1), EnumStatoPeriferica.AMBIGUA, null);
            int countStateA = 0;

            for (Periferica periferica : perifericheExt) {
                if ("A".equals(periferica.getStato())) {
                    countStateA++;
                }
            }
            if (countStateA == 1) {
                if (!statoPeriferica.getStato().equals("A")) {
                    for (Periferica periferica : perifericheExt) {
                        if ("A".equals(periferica.getStato())) {
                            statoPeriferica = new StatoPeriferica(periferica, EnumStatoPeriferica.AMBIGUA,null);
                            if (statoPeriferica.getDataCollaudo() != null && statoPeriferica.getDataDisattivazione() == null && statoPeriferica.getDataDisinstallazione() == null) {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.ATTIVA);
                            } else if (statoPeriferica.getDataCollaudo() == null && statoPeriferica.getDataDisattivazione() == null && statoPeriferica.getDataDisinstallazione() == null) {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.ABBINATA);
                            }
                        }
                    }
                } else if (statoPeriferica.getStato().equals("A")) {
                    if (statoPeriferica.getDataCollaudo() != null && statoPeriferica.getDataDisattivazione() == null && statoPeriferica.getDataDisinstallazione() == null) {
                        statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.ATTIVA);
                    } else if (statoPeriferica.getDataCollaudo() == null && statoPeriferica.getDataDisattivazione() == null && statoPeriferica.getDataDisinstallazione() == null) {
                        statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.ABBINATA);
                    } else if (statoPeriferica.getDataDisattivazione() != null || statoPeriferica.getDataDisinstallazione() != null) {//Da stabilire meglio nodate
                        statoPeriferica.setAnomaliPeriferica(EnumPeriferica.NODATE);
                    } else {
                        statoPeriferica.setAnomaliPeriferica(EnumPeriferica.NODATE);
                    }
                }
                /*else {
                    statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.STATOANOULTIMORECORD);
                }*/

            } else if (countStateA > 1) {
                statoPeriferica.setAnomaliPeriferica(EnumPeriferica.DOPPIAA);
            } else {
                List<Periferica> perifericheCompleta = perifericaDAO.findPerifericheById(statoPeriferica.getIdPeriferica());
                List<Periferica> perifericheExtCompleta = perifericaDAO.findPerifericheExtById(statoPeriferica.getIdPeriferica());

                if (perifericheCompleta.isEmpty()) {
                    statoPeriferica.setAnomaliPeriferica(EnumPeriferica.NESSUNAPERIFERICA);
                } else if (perifericheExtCompleta.size() != perifericheCompleta.size()) {
                    statoPeriferica.setAnomaliPeriferica(EnumPeriferica.SANAREEXT);
                } else {
                    int countStateA2 = 0;

                    for (Periferica periferica : perifericheExtCompleta) {
                        if ("A".equals(periferica.getStato())) {
                            countStateA2++;
                        }
                    }
                    if (countStateA2 == 1) {
                        statoPeriferica.setAnomaliPeriferica(EnumPeriferica.NONDISPONIBILE);
                    } else if (countStateA2 > 1) {
                        statoPeriferica.setAnomaliPeriferica(EnumPeriferica.DOPPIAA);
                    } else {
                        int contrattoPrincipale = statoPeriferica.getIdContratto();
                        statoPeriferica = new StatoPeriferica(perifericheExtCompleta.get(perifericheExtCompleta.size() - 1),EnumStatoPeriferica.AMBIGUA, null);

                        if (statoPeriferica.getIdContratto() == contrattoPrincipale) {
                            if (statoPeriferica.getStato().equals("R")) {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.RIENTRATA);
                            } else if (statoPeriferica.getStato().equals("W")) {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.INSOSTITUZIONE);
                            } else if (statoPeriferica.getDataDisattivazione() == null && statoPeriferica.getDataDisinstallazione() == null) {
                                statoPeriferica.setAnomaliPeriferica(EnumPeriferica.NODATE);
                            } else if (statoPeriferica.getDataDisattivazione() != null && statoPeriferica.getDataDisinstallazione() == null) {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.DISATTIVA);
                            } else if (statoPeriferica.getDataDisinstallazione() != null) {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.DISINSTALLATA);
                            } else {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.AMBIGUA);
                            }
                        } else {
                            statoPeriferica.setAnomaliPeriferica(EnumPeriferica.NONDISPONIBILE);
                        }
                    }
                }
            }
        }
        //Log statoContratto
        //perifericaDAO.insertPerifericaLog(statoPeriferica);

        return statoPeriferica;
    }

    public Sim checkSim (int idPeriferica) {
        if (idPeriferica == 0) {
            return new Sim(0,"","","","","");
        }
        List<Sim> statoSim = simDAO.findSimByIdPeriferica(idPeriferica);
        Sim sim = new Sim(idPeriferica,""+ idPeriferica,"","","","");

        if (statoSim.size() == 1) {
            sim = new Sim(statoSim.get(0));
        } else {
            sim.setCarrier("Carrier non trovato");
            System.err.println("checkSim : Errore lista sim");
        }
        return sim;
    }


    public void insertLogCSV(StatoContratto contratto, StatoPeriferica periferica, Sim sim,boolean aggContratto, boolean aggPeriferica, String azionePeriferica, TrasmissioneDati raw,String posizionePeriferica, String datoIniziale) {
        String csvFile = "out_log.csv";
        File file = new File(csvFile);
        boolean fileExists = file.exists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
            // Se il file non esiste, scrive le intestazioni
            if (!fileExists) {
                writer.write("numVoucher;idContratto;Targa;Vat;Compagnia;TipoMovimento;DescrizioneMov;NumPolizza;DataDecorrenza;DataInizio;DataFine;AnniServizio;StatoContratto;AnomaliaContratto;InvioI;InstallazioniInCorso;DisinstallazioniInCorso;ManutenzioneInCorso;UltimoInterventoEseguito;DataUltimoInterventoEseguito;idPeriferica;DataPrimoCollaudo;Stato;DescrizioneStato;DataCollaudo;DataDisattivazione;DataDisinstallazione;Causale;CausaleDisattivazione;Note;StatoPeriferica;AnomaliaPeriferica;Ubicazione;TipoUbicazione;Iccid;StatoSim;Carrier;AggiornamentoContratto;AggiornamentoPeriferica;AzionePeriferica;AzioneSim;datoIniziale;Raw_isActive;R_isActive;Diff_isActive;Raw_RecordState;R_RecordState;Diff_RecordState;DissalineamentoRawRegistry;");
                writer.newLine();
            }
            // Costruisce la riga CSV
            StringBuilder sb = new StringBuilder();

            sb.append(contratto.getNumVoucher()).append(";");
            sb.append(contratto.getIdContratto()).append(";");
            sb.append(contratto.getTarga()).append(";");
            sb.append("'").append(contratto.getVat()).append(";");
            sb.append(getNomeCompagnia(contratto.getCodCompagnia())).append(";");
            if (contratto.getTipoMovimento() == null) {
                sb.append(contratto.getCodStato() != null ? contratto.getCodStato() : "").append(";");
                sb.append(getDescrizioneMovimentoJeniot(contratto.getCodStato() != null ? contratto.getCodStato() : "")).append(";");
            } else {
                sb.append(contratto.getTipoMovimento() != null ? contratto.getTipoMovimento() : "").append(";");
                sb.append(getDescrizioneMovimentoJeniot(contratto.getTipoMovimento() != null ? contratto.getTipoMovimento() : "")).append(";");
            }
            sb.append("'").append(contratto.getNumPolizza()).append(";");
            sb.append(contratto.getDataDecorrenza() != null ? contratto.getDataDecorrenza().toString() : "").append(";");
            sb.append(contratto.getDataInizioServizio() != null ? contratto.getDataInizioServizio().toString() : "").append(";");
            sb.append(contratto.getDataFineServizio() != null ? contratto.getDataFineServizio().toString() : "").append(";");
            LocalDate dataInizio = contratto.getDataInizioServizio();
            LocalDate dataFine = contratto.getDataFineServizio();
            int anniTrascorsi = 0;
            if (dataInizio != null && dataFine != null) { anniTrascorsi = Period.between(dataInizio, dataFine).getYears(); }
            sb.append(anniTrascorsi).append(";");
            sb.append(String.valueOf(contratto.getStatoContratto())).append(";");
            if ("SCADUTO" == contratto.getStatoContratto().toString()) {
                int giorniTrascorsi = 0;
                LocalDate dataOggi = LocalDate.now();
                if (dataFine != null) { giorniTrascorsi = Period.between(dataFine, dataOggi).getDays(); }
            } else {
                sb.append(contratto.getAnomaliaContratto() != null ? contratto.getAnomaliaContratto() : "").append(";");
            }
            sb.append(contratto.getInvioI()).append(";");
            sb.append(contratto.getInterventiInstallazioneInCorso()).append(";");
            sb.append(contratto.getInterventiDisinstallazioneInCorso()).append(";");
            sb.append(contratto.getInterventiManutenzioneSostituzioneInCorso()).append(";");
            sb.append(contratto.getUltimoInterventoEseguito()).append(";");
            sb.append(contratto.getDataUltimoIntervento() != null ? contratto.getDataUltimoIntervento().toString() : "").append(";");
            sb.append(periferica.getIdPeriferica()).append(";");
            sb.append(periferica.getDataPrimoCollaudo() != null ? periferica.getDataPrimoCollaudo().toString() : "").append(";");
            sb.append(periferica.getStato()).append(";");
            sb.append(getDescrizioneStato(periferica.getStato(), periferica.getDataCollaudo(), periferica.getDataDisinstallazione())).append(";");
            sb.append(periferica.getDataCollaudo() != null ? periferica.getDataCollaudo().toString() : "").append(";");
            sb.append(periferica.getDataDisattivazione() != null ? periferica.getDataDisattivazione().toString() : "").append(";");
            sb.append(periferica.getDataDisinstallazione() != null ? periferica.getDataDisinstallazione().toString() : "").append(";");
            sb.append(periferica.getCausale()).append(";");
            sb.append(periferica.getCausaleDisattivazione()).append(";");
            sb.append(periferica.getNote()).append(";");
            sb.append(String.valueOf(periferica.getStatoPeriferica())).append(";");
            sb.append(String.valueOf(periferica.getAnomaliPeriferica())).append(";");
            sb.append(periferica.getUbicazione()).append(";");
            sb.append(posizionePeriferica).append(";");
            sb.append(sim.getIccid() != null ? ("'"+sim.getIccid()) : "").append(";");
            sb.append(sim.getSimState() != null ? (sim.getSimState()) : "").append(";");
            sb.append(sim.getCarrier() != null ? (sim.getCarrier()) : "").append(";");
            if (aggContratto) {
                sb.append("aggiorna tabella contratti/statopolizze;");
            } else {
                sb.append(";");
            }
            if (aggPeriferica) {
                sb.append("Aggiorna periferica;");
            } else {
                sb.append(";");
            }
            sb.append(azionePeriferica).append(";");
            sb.append(sim.getStatoAtteso()).append(";");

            sb.append("'").append(datoIniziale).append(";");
            if (raw != null) {
                sb.append(raw.getIsDataRawActive()).append(";");
                sb.append(raw.getActive()).append(";");
                sb.append(raw.getDiffIsDataRawActiveActive() != null ? raw.getDiffIsDataRawActiveActive() : "").append(";");
                sb.append(raw.getRecordState()).append(";");
                sb.append(raw.getStatoComunicazione()).append(";");
                sb.append(raw.getDiffRecordStateStatoCom() != null ? raw.getDiffRecordStateStatoCom() : "").append(";");
                sb.append(raw.getStatoAllineamento() != null ? raw.getStatoAllineamento() : "").append(";");
            }

            writer.write(sb.toString());
            writer.newLine();
            writer.flush();
            //System.out.println("Dati inseriti correttamente su out_log.");
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura su out_log: " + e.getMessage());
        }
    }

    private String getNomeCompagnia(String codCompagnia) {
        switch (codCompagnia) {
            case "014":
                return "Generali";
            case "022":
                return "ITAS";
            case "038":
                return "VITTORIA";
            case "057":
                return "Cattolica";
            case "057G":
                return "Cattolica GCA";
            case "057J":
                return "Cattolica Jeniot";
            case "247":
                return "Genertel";
            case "429":
                return "TUA";
            case "429J":
                return "TUA JENIOT";
            case "440":
                return "QUIXA";
            case "457":
                return "VERA";
            default:
                return codCompagnia;
        }
    }

    private String getDescrizioneMovimentoJeniot(String tipoMovimento) {
        switch (tipoMovimento) {
            case "0":
                return "Emissione";
            case "1":
                return "Cambio veicolo";
            case "2":
                return "Storno";
            case "3":
                return "Sostituzione";
            case "4":
                return "Storno";
            case "5":
                return "Sospensione";
            case "6":
                return "Riattivazione";
            case "7":
                return "Storno polizza";
            case "8":
                return "Cessione polizza";
            case "9":
                return "Riammissione";
            case "10":
                return "Storno";
            case "11":
                return "Storno";
            case "13":
                return "Aggiornamento dati";
            case "14":
                return "Rinnovo";
            case "17":
                return "Passaggi da INA a GI al rinnovo";
            case "18":
                return "Passaggi da Toro a GI al rinnovo";
            case "19":
                return "Cambio agenzia";
            case "20":
                return "Passaggi da Lloyd a GI al rinnovo";
            case "21":
                return "Cambio veicolo";
            case "31":
                return "Cambio veicolo";
            case "33":
                return "Cambio device";
            case "41":
                return "Cambio veicolo";
            case "43":
                return "Cambio device";
            case "47":
                return "Proroga per installazione";
            case "REPLACE":
                return "Cambio veicolo";
            case "RECT":
                return "Aggiornamento dati";
            case "NEW":
                return "Emissione";
            case "CLOSURE":
                return "Storno";
            case "RENEWAL":
                return "Rinnovo";
            case "SUBVE":
                return "Cambio veicolo";
            default:
                return tipoMovimento;
        }
    }

    private String getDescrizioneStato(String stato,LocalDate dataCollaudo,LocalDate dataDisinstallazione) {
        switch (stato) {
            case "A":
                if (dataCollaudo != null) {
                    return "Attiva";
                } else {
                    return "Abbinata";
                }
            case "D":
                if (dataDisinstallazione != null) {
                    return "Disinstallata";
                } else {
                    return "Disattiva";
                }
            case "R":
                return "Rientrata";
            case "W":
                return "In attesa di sostituzione";
            default:
                return stato;
        }
    }
}
