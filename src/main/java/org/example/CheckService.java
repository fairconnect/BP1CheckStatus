package org.example;

import org.example.contratto.ContrattoDAO;
import org.example.contratto.Mov;
import org.example.contratto.StatoContratto;
import org.example.periferica.Periferica;
import org.example.periferica.PerifericaDAO;
import org.example.periferica.StatoPeriferica;
import org.example.sim.Sim;
import org.example.sim.SimDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.example.EnumTypes.*;
import static org.example.EnumTypes.EnumComagnia.*;

public class CheckService {

    private final ContrattoDAO contrattoDAO = new ContrattoDAO();
    private final PerifericaDAO perifericaDAO = new PerifericaDAO();
    private final SimDAO simDAO = new SimDAO();

    public CheckService() {}

    public void step1(int idContratto, EnumComagnia compagnia) {
        System.err.println("INIZIO step1:" + idContratto);
        //Aggiungere controllo se già è stato elavborato
        /*boolean trovato = contrattoDAO.findContrattoLog(idContratto);
        if !(trovato) { } else { }*/

        StatoContratto statoContratto = new StatoContratto(new Mov(0,0,0,0,"","","","","","","",null,null,null,null, ""), EnumStatoContratto.AMBIGUO, false, -1, -1,0, 0, 0, 0,"",null);

        List<String> compMov = contrattoDAO.findCompagnieByIdContratto(idContratto);
        if (compMov.size() != 1) {
            System.err.println("Errore: crossCompagnia");
            statoContratto.setCrossCompagnia(true);
        }
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

        System.err.println("FINE step1:" + idContratto);

        step2(statoContratto);
    }

    public void step2(StatoContratto statoContratto) {
        //System.err.println("INIZIO step2");

        StatoPeriferica statoPeriferica = checkStatoPeriferica(statoContratto.getIdContratto());

        //System.err.println("FINE step2");
        if (EnumStatoContratto.AMBIGUO != statoContratto.getStatoContratto() && EnumStatoPeriferica.AMBIGUA != statoPeriferica.getStatoPeriferica()) {
            //Step 4 per saltare il 3 che fa update
            step3(statoContratto, statoPeriferica);
        } else {
            step4(statoContratto, statoPeriferica, false ,false);
        }

    }

    public void step3(StatoContratto statoContratto, StatoPeriferica statoPeriferica) {
        //System.err.println("INIZIO step3");
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
        //System.err.println("Fine step3");
        step4(statoContratto, statoPeriferica, aggiornatoContratto ,aggiornatoPeriferica);
    }

    public void step4(StatoContratto statoContratto, StatoPeriferica statoPeriferica, boolean aggiornatoContratto, boolean aggiornatoPeriferica) {
        //System.err.println("INIZIO step4");

        Sim sim = checkSim(statoPeriferica.getIdPeriferica());
        String statoAtteso = "Assenza dbSim";

        if (sim.getCarrier() != null && sim.getCarrier().equals("VODAFONE")) {
            if (sim.getSimState().equalsIgnoreCase("ACTIVE.LIVE") || sim.getSimState().equalsIgnoreCase("ACTIVE.READY") || sim.getSimState().equalsIgnoreCase("ACTIVE.TEST")) {
                if (EnumStatoContratto.SCADUTO.equals(statoContratto.getStatoContratto()) && (EnumStatoPeriferica.DISATTIVA.equals(statoPeriferica.getStatoPeriferica()) || EnumStatoPeriferica.DADISATTIVARE.equals(statoPeriferica.getStatoPeriferica()))) {
                    if (!sim.getSimState().equalsIgnoreCase("ACTIVE.TEST")) {
                        statoAtteso = "Sospendi SIM";
                    } else {
                        statoAtteso = "Indifferente SIM (Test)";
                    }
                } else {
                    statoAtteso = "nessunazione ";
                }
            } else {
                if (EnumStatoContratto.ATTIVO.equals(statoContratto.getStatoContratto()) && EnumStatoPeriferica.ATTIVA.equals(statoPeriferica.getStatoPeriferica())) {
                    statoAtteso = "Attiva SIM";
                } else {
                    statoAtteso = "statoContratto e statoPeriferica non compatibili";
                }
            }
        } else if (sim.getCarrier() != null && sim.getCarrier().equals("TIM")) {
            if (sim.getSimState().equalsIgnoreCase("ACTIVATED") || sim.getSimState().equalsIgnoreCase("ACTIVATION READY") || sim.getSimState().equalsIgnoreCase("TEST READY")) {
                if (EnumStatoContratto.SCADUTO.equals(statoContratto.getStatoContratto()) && (EnumStatoPeriferica.DISATTIVA.equals(statoPeriferica.getStatoPeriferica()) || EnumStatoPeriferica.DADISATTIVARE.equals(statoPeriferica.getStatoPeriferica()))) {
                    if (!sim.getSimState().equalsIgnoreCase("TEST READY")) {
                        statoAtteso = "Sospendi SIM";
                    } else {
                        statoAtteso = "Indifferente SIM (Test)";
                    }
                } else {
                    statoAtteso = "statoContratto e statoPeriferica non compatibili";
                }
            } else {
                if (EnumStatoContratto.ATTIVO.equals(statoContratto.getStatoContratto()) && EnumStatoPeriferica.ATTIVA.equals(statoPeriferica.getStatoPeriferica())) {
                    statoAtteso = "Attiva SIM";
                } else {
                    statoAtteso = "statoContratto e statoPeriferica non compatibili";
                }
            }
        } else {
            System.err.println("step4 provider sim non riconosciuto" + sim.getCarrier());
        }
        sim.setStatoAtteso(statoAtteso);
        simDAO.insertSimLog(statoContratto, statoPeriferica, sim, aggiornatoContratto, aggiornatoPeriferica);
        //System.err.println("FINE step4");
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
                } else if ("09".equals(mov.getTipoMovimento())) {
                /// Case 5 riammisione in vigore
                    statoPolizza = EnumStatoPolizza.RIAMMISSIONE;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                } else if ("14".equals(mov.getTipoMovimento())) {
                    /// Case 5 riammisione in vigore
                    statoPolizza = EnumStatoPolizza.INCORSO;
                    contrattoDAO.insertPolizzaLog(mov, statoPolizza.toString());
                } else if ("02".equals(statoContratto.getTipoMovimento()) || "04".equals(statoContratto.getTipoMovimento()) || "07".equals(statoContratto.getTipoMovimento()) || "10".equals(statoContratto.getTipoMovimento())) {
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
        statoContratto.setUltimoInterventoEseguito(null);
        statoContratto.setDataUltimoIntervento(null);

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
            //Tentativo recupero statoperiferichePreDisaster oppure generazione record statoperiferiche ext
        } else {
            statoPeriferica = new StatoPeriferica(perifericheExt.get(perifericheExt.size() - 1), EnumStatoPeriferica.AMBIGUA);
            int countStateA = 0;

            for (Periferica periferica : perifericheExt) {
                if ("A".equals(periferica.getStato())) {
                    //assegnare A nel valore elaborato
                    countStateA++;
                }
            }
            //
            if (countStateA == 1) {
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
}
