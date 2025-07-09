package org.example;

import org.example.contratto.ContrattoDAO;
import org.example.contratto.Mov;
import org.example.contratto.StatoContratto;
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
import java.util.Objects;

import static org.example.EnumTypes.*;
import static org.example.EnumTypes.EnumComagnia.*;

public class CheckService {

    private final ContrattoDAO contrattoDAO = new ContrattoDAO();
    private final PerifericaDAO perifericaDAO = new PerifericaDAO();
    private final SimDAO simDAO = new SimDAO();

    public CheckService() {}

    public int getIdContratto(int idPeriferica) {
        int idContratto = -1;

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

    public void step1(int idContratto, EnumComagnia compagnia) {
        System.out.println("INIZIO step1:" + idContratto);

        StatoContratto statoContratto = new StatoContratto(new Mov(0,0,0,0,"","","","","","","",null,null,null,null, ""), EnumStatoContratto.AMBIGUO, false, -1, -1,0, 0, 0, 0,"",null);

        /*List<String> compMov = contrattoDAO.findCompagnieByIdContratto(idContratto);
        if (compMov.size() != 1) {
            statoContratto.setCrossCompagnia(true);
        }*/
        switch (compagnia) {
            case MES:
                System.out.println("Gestione della compagnia MES");
                statoContratto = checkStatoContratto(idContratto);
                break;
            case JENIOT:
                System.out.println("Gestione della compagnia JENIOT");
                statoContratto = contrattiJeniot(idContratto);
                break;
            default:
                System.out.println("Compagnia non riconosciuta");
                break;
        }

        System.out.println("FINE step1:" + idContratto);

        step2(statoContratto);
    }

    public void step2(StatoContratto statoContratto) {
        System.out.println("INIZIO step2");

        StatoPeriferica statoPeriferica = checkStatoPeriferica(statoContratto.getIdContratto());

        System.out.println("FINE step2");

        if (EnumStatoContratto.AMBIGUO != statoContratto.getStatoContratto() && EnumStatoPeriferica.AMBIGUA != statoPeriferica.getStatoPeriferica()) {
            step3(statoContratto, statoPeriferica);
        } else {
            step4(statoContratto, statoPeriferica, false ,false);
        }

    }

    public void step3(StatoContratto statoContratto, StatoPeriferica statoPeriferica) {
        System.out.println("INIZIO step3");
        //Attenzione update
        /*if (EnumStatoContratto.ATTIVO.equals(statoContratto.getStatoContratto())) {
            contrattoDAO.updateStatoContrattoById(statoContratto.getIdContratto(), 2);
            contrattoDAO.updateStatoPolizzeByIdContratto(statoContratto.getIdContratto(), 1);
            if (statoPeriferica.getStatoPeriferica().equals(EnumStatoPeriferica.DISATTIVA)) {
                perifericaDAO.updatePerifericaById(statoPeriferica.getIdPeriferica(), statoPeriferica.getIdStatoPeriferica(), "A");
            }
        } else */
        boolean aggiornatoContratto = false;
        boolean aggiornatoPeriferica = false;
        if (EnumStatoContratto.SCADUTO.equals(statoContratto.getStatoContratto())) {
            if (2 == statoContratto.getContrattiStato() || 1 == statoContratto.getStatoPolizzeStato()) {
                //contrattoDAO.updateStatoContrattoById(statoContratto.getIdContratto(), 0);
                //contrattoDAO.updateStatoPolizzeByIdContratto(statoContratto.getIdContratto(), 3);
                aggiornatoContratto = true;
            }
            if (statoPeriferica.getStatoPeriferica().equals(EnumStatoPeriferica.ATTIVA)) {
                //perifericaDAO.updatePerifericaById(statoPeriferica.getIdPeriferica(), statoPeriferica.getIdStatoPeriferica(), "D");
                aggiornatoPeriferica = true;
                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.DADISATTIVARE);
            }
        }
        System.out.println("Fine step3");
        step4(statoContratto, statoPeriferica, aggiornatoContratto ,aggiornatoPeriferica);
    }

    public void step4(StatoContratto statoContratto, StatoPeriferica statoPeriferica, boolean aggiornatoContratto, boolean aggiornatoPeriferica) {
        System.out.println("INIZIO step4");

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
        simDAO.insertSimLog(statoContratto, statoPeriferica, sim, aggiornatoContratto, aggiornatoPeriferica);
        System.out.println("FINE step4");
        insertLogCSV(statoContratto,statoPeriferica,sim);
        DBManager.closeConnection();
    }

    private void step5(StatoContratto statoContratto, StatoPeriferica statoPeriferica) {
        //gestione api sim
    }

    public StatoContratto checkStatoContratto(int idContratto) {
        ///Recupero la lista dei mov legato a un contratto
        List<Mov> contratti = contrattoDAO.findMovByIdContratto(idContratto);

        StatoContratto statoContratto;

        if (contratti.isEmpty()) {
            statoContratto = new StatoContratto(new Mov(0,0,0,0,"","","","","","","",null,null,null,null, ""), EnumStatoContratto.NESSUNRECORD, false, -1, -1,0,0,0,0,"",null);
        } else {
            ///Prendo l'ultimo mov per il contratto
            statoContratto = new StatoContratto(contratti.get(0), EnumStatoContratto.AMBIGUO, false, -1, -1,0,0,0,0,"",null);

            for (Mov contratto : contratti) {
                if (contratto.getIdTracciato() > statoContratto.getIdTracciato()) {
                    statoContratto = new StatoContratto(contratto, EnumStatoContratto.IDULTIMOMOVERRATO, false, -1, -1,0,0,0,0,"",null);
                }
            }
            ///Controlliamo la compagnia dell'ultimo mov del contratto
            String valoreRiferimento = statoContratto.getCodCompagnia();

            for (Mov contratto : contratti) {
                if (!contratto.getCodCompagnia().equals(valoreRiferimento)) {
                    statoContratto.setStatoContratto(EnumStatoContratto.CROSSCOMPAGNIA);
                    statoContratto.setCrossCompagnia(true);//Attenzione a rinominare parametro crossCompagnia
                }
            }
            ///Determina lo stato in base alla compagnia e la data
            LocalDate oggi = LocalDate.now();
            if (statoContratto.getCodCompagnia().equals("MES") || statoContratto.getCodCompagnia().equals("CDS")) {
                if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio())) {
                    statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                } else {
                    statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                }
            } else {
                if (!oggi.isAfter(statoContratto.getDataFineServizio())) {
                    statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                } else {
                    statoContratto.setStatoContratto(EnumStatoContratto.ULTIMADATASCADUTA);
                }
            }
        }
        ///Recupero i valori dalla tabella contratti e statopolizze
        statoContratto.setContrattiStato(contrattoDAO.findStatoContrattoById(idContratto));
        statoContratto.setStatoPolizzeStato(contrattoDAO.findStatoPolizzeByIdContratto(idContratto));

        ///Log statoContratto
        contrattoDAO.insertContrattoLog(statoContratto);

        return statoContratto;
    }

    public StatoContratto contrattiJeniot(int idContratto) {
        StatoContratto statoContratto;
        LocalDate oggi = LocalDate.now();

        List<Mov> movList = contrattoDAO.findMovGeneraliIdContratto(idContratto);

        if (movList.isEmpty()) {
            statoContratto = new StatoContratto(new Mov(0,0,0,0,"","","","","","","",null,null,null,null, ""), EnumStatoContratto.NESSUNRECORD, false, -1, -1,0,0,0,0, "", null);
        } else {
            ///Prendo l'ultimo mov per il contratto
            statoContratto = new StatoContratto(movList.get(0), EnumStatoContratto.AMBIGUO, false, -1, -1,0,0,0,0,"",null);

            for (Mov contratto : movList) {
                if (contratto.getIdUnicoMov() > statoContratto.getIdUnicoMov()) {
                    statoContratto = new StatoContratto(contratto, EnumStatoContratto.IDULTIMOMOVERRATO, false, -1, -1,0,0,0,0,"",null);
                }
            }
            boolean scarti = contrattoDAO.findScartiGenerali(statoContratto.getTarga());

            if (scarti) {
                statoContratto = new StatoContratto(movList.get(0), EnumStatoContratto.SCARTIDARECUPERARE, false, -1, -1,0,0,0,0,"",null);
            } else {

                if ("0".equals(statoContratto.getTipoMovimento()) || "18".equals(statoContratto.getTipoMovimento()) || "14".equals(statoContratto.getTipoMovimento()) || "6".equals(statoContratto.getTipoMovimento()) || "9".equals(statoContratto.getTipoMovimento()) || "13".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio())) {
                        statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                    } else {
                        statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                    }
                } else if ("11".equals(statoContratto.getTipoMovimento()) || "2".equals(statoContratto.getTipoMovimento()) || "4".equals(statoContratto.getTipoMovimento()) || "7".equals(statoContratto.getTipoMovimento()) || "10".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataDecorrenza())) {
                        statoContratto.setStatoContratto(EnumStatoContratto.STORNATO);
                    } else {
                        statoContratto.setStatoContratto(EnumStatoContratto.DASTORNARE);
                    }
                } else if ("8".equals(statoContratto.getTipoMovimento()) || "17".equals(statoContratto.getTipoMovimento()) || "19".equals(statoContratto.getTipoMovimento()) || "20".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataDecorrenza())) {
                        statoContratto.setStatoContratto(EnumStatoContratto.TERMINATO);
                    } else {
                        statoContratto.setStatoContratto(EnumStatoContratto.DASTORNARE);
                    }
                } else if ("5".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio())) {
                        statoContratto.setStatoContratto(EnumStatoContratto.SOSPESO);
                    }
                } else if ("1".equals(statoContratto.getTipoMovimento()) || "3".equals(statoContratto.getTipoMovimento()) || "21".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio())) {
                        statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                    } else {
                        statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                    }
                } else if ("31".equals(statoContratto.getTipoMovimento()) || "33".equals(statoContratto.getTipoMovimento()) || "41".equals(statoContratto.getTipoMovimento()) || "43".equals(statoContratto.getTipoMovimento())) {
                    if (!oggi.isBefore(statoContratto.getDataInizioServizio()) && !oggi.isAfter(statoContratto.getDataFineServizio())) {
                        statoContratto.setStatoContratto(EnumStatoContratto.ATTIVO);
                    } else {
                        statoContratto.setStatoContratto(EnumStatoContratto.SCADUTO);
                    }
                }
            }

            for (Mov mov : movList) {
                /// Case 2 FIX TIPOMOV
                /*if ("01".equals(mov.getTipoMovimento()) || "03".equals(mov.getTipoMovimento()) || "21".equals(mov.getTipoMovimento()) || "33".equals(mov.getTipoMovimento())) {
                    statoPolizza = EnumStatoPolizza.MODIFICA;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                }*/
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
            }

        }
        ///verificare installazione ed invio I
        statoContratto.setInvioI(contrattoDAO.findInvioIByTarga(statoContratto.getTarga()));

        /// Recupero interventi in corso legati al contratto
        statoContratto.setInterventiInstallazioneInCorso(contrattoDAO.findInterventiGenerali(idContratto, "installazioni"));
        statoContratto.setInterventiInstallazioneInCorso(contrattoDAO.findInterventiGenerali(idContratto, "disinstallazioni"));
        statoContratto.setInterventiInstallazioneInCorso(contrattoDAO.findInterventiGenerali(idContratto, "altro"));
        //Verificare ultimo intervento
        Intervento intervento = contrattoDAO.findUltimoIntervento(idContratto);
        if (intervento != null) {
            statoContratto.setUltimoInterventoEseguito(intervento.getTipoIntervento());
            statoContratto.setDataUltimoIntervento(intervento.getDataIntervento());
        }

        ///Recupero i valori dalla tabella contratti e statopolizze
        statoContratto.setContrattiStato(contrattoDAO.findStatoContrattoById(idContratto));
        statoContratto.setStatoPolizzeStato(contrattoDAO.findStatoPolizzeByIdContratto(idContratto));

        ///Log statoContratto
        contrattoDAO.insertContrattoLog(statoContratto);

        return statoContratto;
    }

    public StatoPeriferica checkStatoPeriferica(int idContratto) {
        List<Periferica> periferiche = perifericaDAO.findPerifericheByIdContratto(idContratto);
        List<Periferica> perifericheExt = perifericaDAO.findPerifericheExtByIdContratto(idContratto);

        StatoPeriferica statoPeriferica;

        if (periferiche.isEmpty()) {
            statoPeriferica = new StatoPeriferica(new Periferica(0,0,0,"","","","",null,null,null,null,null,""),EnumStatoPeriferica.NESSUNAPERIFERICA);
        } else if (perifericheExt.size() != periferiche.size()) {
            statoPeriferica = new StatoPeriferica(new Periferica(0,0,0,"","","","",null,null,null,null,null,""),EnumStatoPeriferica.SANAREEXT);
        } else {
            statoPeriferica = new StatoPeriferica(perifericheExt.get(perifericheExt.size() - 1), EnumStatoPeriferica.AMBIGUA);
            int countStateA = 0;

            for (Periferica periferica : perifericheExt) {
                if ("A".equals(periferica.getStato())) {
                    countStateA++;
                }
            }
            //
            if (countStateA == 1) {
                if (statoPeriferica.getStato().equals("F") || statoPeriferica.getStato().equals("W")) {
                    for (Periferica periferica : perifericheExt) {
                        if ("A".equals(periferica.getStato())) {
                            statoPeriferica = new StatoPeriferica(periferica, EnumStatoPeriferica.AMBIGUA);
                        }
                    }
                }
                if (statoPeriferica.getStato().equals("A")) {
                    if (statoPeriferica.getDataCollaudo() != null && statoPeriferica.getDataDisattivazione() == null && statoPeriferica.getDataDisinstallazione() == null) {
                        statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.ATTIVA);
                    } else if (statoPeriferica.getDataCollaudo() == null && statoPeriferica.getDataDisattivazione() == null && statoPeriferica.getDataDisinstallazione() == null) {
                        statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.ABBINATA);
                    } else if (statoPeriferica.getDataDisattivazione() != null || statoPeriferica.getDataDisinstallazione() != null) {//Da verificare se dividere e gestire disattivata e disinstallata
                        statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.NODATADIS);
                    } else {
                        statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.NESSUNADATA);
                    }
                } else {
                    statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.STATOANOULTIMORECORD);
                }

            } else if (countStateA > 1) {
                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.DOPPIAA);
            } else {
                List<Periferica> perifericheCompleta = perifericaDAO.findPerifericheById(statoPeriferica.getIdPeriferica());
                List<Periferica> perifericheExtCompleta = perifericaDAO.findPerifericheExtById(statoPeriferica.getIdPeriferica());

                if (perifericheCompleta.isEmpty()) {
                    statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.NESSUNAPERIFERICA);
                } else if (perifericheExtCompleta.size() != perifericheCompleta.size()) {
                    statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.SANAREEXT);
                    //Tentativo recupero statoperiferichePreDisaster oppure generazione record statoperiferiche ext
                } else {
                    int countStateA2 = 0;

                    for (Periferica periferica : perifericheExtCompleta) {
                        if ("A".equals(periferica.getStato())) {
                            countStateA2++;
                        }
                    }
                    if (countStateA2 == 1) {
                        statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.PERIFERICASUDIVERSOCONTRATTO);
                    } else if (countStateA2 > 1) {
                        statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.DOPPIAA);
                    } else {
                        int contrattoPrincipale = statoPeriferica.getIdContratto();
                        statoPeriferica = new StatoPeriferica(perifericheExtCompleta.get(perifericheExtCompleta.size() - 1),EnumStatoPeriferica.AMBIGUA);

                        if (statoPeriferica.getIdContratto() == contrattoPrincipale) {
                            if (statoPeriferica.getStato().equals("R") || statoPeriferica.getStato().equals("W")) {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.STATONONVALIDO);
                            } else if (statoPeriferica.getDataDisattivazione() == null && statoPeriferica.getDataDisinstallazione() == null) {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.NODATE);
                            } else if (statoPeriferica.getDataDisattivazione() != null && statoPeriferica.getDataDisinstallazione() == null) {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.DISATTIVA);
                            } else if (statoPeriferica.getDataDisinstallazione() != null) {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.DISINSTALLATA);
                            } else {
                                statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.AMBIGUA);
                            }
                        } else {
                            statoPeriferica.setStatoPeriferica(EnumStatoPeriferica.PERIFERICASUDIVERSOCONTRATTO);
                        }
                    }
                }
            }
        }
        //Log statoContratto
        perifericaDAO.insertPerifericaLog(statoPeriferica);

        return statoPeriferica;
    }

    public Sim checkSim (int idPeriferica) {
        if (idPeriferica == 0) {
            return new Sim(0,"Error","","","","");
        }
        List<Sim> statoSim = simDAO.findSimByIdPeriferica(idPeriferica);
        Sim sim = new Sim(idPeriferica,"Error"+ idPeriferica,"","","","");

        if (statoSim.size() == 1) {
            sim = new Sim(statoSim.get(0));
        } else {
            sim.setCarrier("Nessuno");
            System.err.println("checkSim : Errore lista sim");
        }
        return sim;
    }


    public void insertLogCSV(StatoContratto contratto, StatoPeriferica periferica, Sim sim) {
        String csvFile = "out_log.csv";
        File file = new File(csvFile);
        boolean fileExists = file.exists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
            // Se il file non esiste, scrive le intestazioni
            if (!fileExists) {
                writer.write("numVoucher;idContratto;Targa;Vat;Compagnia;TipoMovimento;DescrizioneMov;NumPolizza;DataDecorrenza;DataInizio;DataFine;AnniServizio;StatoContratto;InvioI;InstallazioniInCorso;DisinstallazioniInCorso;ManutenzioneInCorso;UltimoInterventoEseguito;DataUltimoInterventoEseguito;DataPrimoCollaudo;idPeriferica;Stato;DescrizioneStato;DataCollaudo;DataDisattivazione;DataDisinstallazione;Causale;CausaleDisattivazione;Note;StatoPeriferica;Iccid;StatoSim;SimState;Carrier;StatoAtteso;");
                writer.newLine();
            }
            // Costruisce la riga CSV
            StringBuilder sb = new StringBuilder();
            sb.append(contratto.getNumVoucher()).append(";");
            sb.append(contratto.getIdContratto()).append(";");
            sb.append(escapeCsv(contratto.getTarga())).append(";");
            sb.append(escapeCsv(contratto.getVat())).append(";");
            sb.append(escapeCsv(getNomeCompagnia(contratto.getCodCompagnia()))).append(";");
            sb.append(escapeCsv(contratto.getTipoMovimento())).append(";");
            sb.append(escapeCsv(getDescrizioneMovimento(contratto.getTipoMovimento()))).append(";");
            sb.append(escapeCsv(contratto.getNumPolizza())).append(";");
            sb.append(contratto.getDataDecorrenza() != null ? contratto.getDataDecorrenza().toString() : "").append(";");
            sb.append(contratto.getDataInizioServizio() != null ? contratto.getDataInizioServizio().toString() : "").append(";");
            sb.append(contratto.getDataFineServizio() != null ? contratto.getDataFineServizio().toString() : "").append(";");
            LocalDate dataInizio = contratto.getDataInizioServizio();
            LocalDate dataFine = contratto.getDataFineServizio();
            int anniTrascorsi = 0;
            if (dataInizio != null && dataFine != null) { anniTrascorsi = Period.between(dataInizio, dataFine).getYears(); }
            sb.append(anniTrascorsi).append(";");
            sb.append(escapeCsv(String.valueOf(contratto.getStatoContratto()))).append(";");
            sb.append(contratto.getInvioI()).append(";");
            sb.append(contratto.getInterventiInstallazioneInCorso()).append(";");
            sb.append(contratto.getInterventiDisinstallazioneInCorso()).append(";");
            sb.append(contratto.getInterventiManutenzioneSostituzioneInCorso()).append(";");
            sb.append(escapeCsv(contratto.getUltimoInterventoEseguito())).append(";");
            sb.append(contratto.getDataUltimoIntervento() != null ? contratto.getDataUltimoIntervento().toString() : "").append(";");
            sb.append(periferica.getDataPrimoCollaudo() != null ? periferica.getDataPrimoCollaudo().toString() : "").append(";");
            sb.append(periferica.getIdPeriferica()).append(";");
            sb.append(escapeCsv(periferica.getStato())).append(";");
            sb.append(escapeCsv(getDescrizioneStato(periferica.getStato(), periferica.getDataCollaudo(), periferica.getDataDisinstallazione()))).append(";");
            sb.append(periferica.getDataCollaudo() != null ? periferica.getDataCollaudo().toString() : "").append(";");
            sb.append(periferica.getDataDisattivazione() != null ? periferica.getDataDisattivazione().toString() : "").append(";");
            sb.append(periferica.getDataDisinstallazione() != null ? periferica.getDataDisinstallazione().toString() : "").append(";");
            sb.append(escapeCsv(periferica.getCausale())).append(";");
            sb.append(escapeCsv(periferica.getCausaleDisattivazione())).append(";");
            sb.append(escapeCsv(periferica.getNote())).append(";");
            sb.append(escapeCsv(String.valueOf(periferica.getStatoPeriferica()))).append(";");
            sb.append(sim.getIccid() != null ? escapeCsv(sim.getIccid()) : "").append(";");
            sb.append(sim.getSimState() != null ? escapeCsv(sim.getSimState()) : "").append(";");
            sb.append(sim.getCarrier() != null ? escapeCsv(sim.getCarrier()) : "").append(";");
            sb.append(sim.getStatoAtteso() != null ? escapeCsv(sim.getStatoAtteso()) : "").append(";");
            writer.write(sb.toString());
            writer.newLine();
            writer.flush();
            System.out.println("Dati inseriti correttamente su out_log.");
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura su out_log: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    private String getNomeCompagnia(String codCompagnia) {
        switch (codCompagnia) {
            case "014":
                return "Generali";
            case "057G":
                return "GCA";
            case "429J":
                return "Tua Jeniot";
            case "057J":
                return "Cattolica";
            default:
                return codCompagnia; // oppure "" se vuoi lasciare vuoto il campo
        }
    }

    private String getDescrizioneMovimento(String tipoMovimento) {
        switch (tipoMovimento) {
            case "00":
                return "Emissione";
            case "01":
                return "Cambio veicolo";
            case "02":
                return "Storno";
            case "03":
                return "Sostituzione";
            case "04":
                return "Storno";
            case "05":
                return "Sospensione";
            case "06":
                return "Riattivazione";
            case "07":
                return "Storno polizza";
            case "08":
                return "Cessione polizza";
            case "09":
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
            default:
                return "N/D";
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
                return "N/D";
        }
    }
}
