package org.example.contratto;
import org.example.DBManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContrattoDAO {

    public List<String> findCompagnieByIdContratto(int idContratto) {
        List<String> compagnieList = new ArrayList<>();

        String query = "select codCompagnia from tracciati where numVoucher = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idContratto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String codCompagnia = resultSet.getString("codCompagnia");
                    if (!compagnieList.contains(codCompagnia)) {
                        compagnieList.add(codCompagnia);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("findCompagnieByIdContratto Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return compagnieList;
    }

    public List<Mov> findMovByIdContratto(int idContratto) {
        List<Mov> movList = new ArrayList<>();

        //String query = "SELECT idTracciato, numVoucher, codStato, codCompagnia, numPolizza, targa, vat, dataDecorrenza, dataInizioServ, dataFineServ FROM tracciati WHERE numVoucher = ?";

        String query = "SELECT t.idTracciato, t.numVoucher, t.codStato, t.codCompagnia, t.numPolizza, t.targa, t.vat, " +
                "t.dataDecorrenza, t.dataInizioServ, t.dataFineServ, cd.dataInizio, cd.dataFine FROM contratti_dealer cd JOIN tracciati t ON t.numVoucher = cd.idContratto WHERE numVoucher = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idContratto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Mov movNew = mapToMov(resultSet);
                    movList.add(movNew);
                }
            }
        } catch (SQLException e) {
            System.out.println("findMovByIdContratto Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return movList;
    }

    public List<Mov> findMovGeneraliIdContratto(int idContratto) {
        List<Mov> movList = new ArrayList<>();
        //int numVoucher = findNumVoucherByIdContratto(idContratto);

        String query = "select t.numVoucher as t_idContratto, tg.idUnicoMovimento, t.codStato, t.idTracciato, t.numPolizza, t.numPolSostituita, tg.numVoucher as tg_numVoucher, t.codCompagnia, tg.tipoMovimento, t.dataDecorrenza, t.dataArrivo, t.dataInizioServ, t.dataFineServ, t.vat, t.targa, tg.nomeFileMovim from  tracciati t left join tracciatigenerali tg on t.idTracciato = tg.idTracciato where t.numVoucher = ? order by t.dataArrivo desc";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idContratto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Mov movNew = mapToMovGen(resultSet);
                    movList.add(movNew);
                }
            }
        } catch (SQLException e) {
            System.out.println("findMovGeneraliIdContratto Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return movList;
    }

    public Boolean findScartiGenerali(String targa) {
        List<Mov> movList = new ArrayList<>();
        boolean scarti = false;

        String query = "select * from scartitracciatigenerali s left join tracciatigenerali tg on tg.idUnicoMovimento = s.idUnicoMovimento where s.targa = ? and tg.idUnicoMovimento is null";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, targa);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next()) {
                    return scarti;
                }
                while (resultSet.next()) {
                    Map<String, Boolean> idUnicoMovimentoMap = new HashMap<>();

                    while (resultSet.next()) {
                        String idUnicoMovimento = resultSet.getString("idUnicoMovimento");
                        String esito = resultSet.getString("codiceRisultato");

                        if ("02".equals(esito)) {
                            // Se esito è 02, saltiamo l'elaborazione di questo idUnicoMovimento
                            idUnicoMovimentoMap.put(idUnicoMovimento, true); // Ignorare id con esito 02
                        } else {
                            // Se idUnicoMovimento non è segnato come ignorato, marcato come scarto da recuperare
                            idUnicoMovimentoMap.putIfAbsent(idUnicoMovimento, false);
                        }
                    }
                    // Ritorno true se esistono scarti da recuperare (valore "false")
                    for (Boolean isIgnorare : idUnicoMovimentoMap.values()) {
                        if (!isIgnorare) {
                            if (!scarti) {
                                scarti = true;
                            }
                        }
                    }
                    return scarti;
                }
            }
        } catch (SQLException e) {
            System.out.println("findScartiGenerali Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    public int findInterventiGenerali(int idContratto, String tipoIntervento) {
            int interventi = 0;
            String query = "";
            if (tipoIntervento.equals("installazioni")) {
                query = "SELECT count(*) as numeroInterventi from operations o join stato_operations so on so.idoperation=o.idOperation and so.lastRecord=1 join tipo_operations top on top.tipoOperazione=o.tipoOperazione join tipo_stato_operations tso on tso.idtipoStatoOperation=so.stato where o.idContratto = ? and tso.category_state != 'COMPLETED' and top.tipoOperazione in (1, 3, 9,14,19, 20)";
            } else if (tipoIntervento.equals("disinstallazioni")) {
                query = "SELECT count(*) as numeroInterventi from operations o join stato_operations so on so.idoperation=o.idOperation and so.lastRecord=1 join tipo_operations top on top.tipoOperazione=o.tipoOperazione join tipo_stato_operations tso on tso.idtipoStatoOperation=so.stato where o.idContratto = ? and tso.category_state != 'COMPLETED' and top.tipoOperazione in (2,4,12,15,21)";
            } else {
                query = "SELECT count(*) as numeroInterventi from operations o join stato_operations so on so.idoperation=o.idOperation and so.lastRecord=1 join tipo_operations top on top.tipoOperazione=o.tipoOperazione join tipo_stato_operations tso on tso.idtipoStatoOperation=so.stato where o.idContratto = ? and tso.category_state != 'COMPLETED' and top.tipoOperazione in (5,6,7,8,10,11,13,16,17,18)";
            }

            try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, idContratto);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        interventi = resultSet.getInt("numeroInterventi");
                    }
                }
            } catch (Exception e) {
                System.out.println("findNumVoucherByIdContratto Errore durante l'esecuzione della query: " + e.getMessage());
                e.printStackTrace();
            }
        return interventi;
    }

    private Mov mapToMov(ResultSet resultSet) throws SQLException {
        int idTracciato = resultSet.getInt("idTracciato");
        int idContratto = resultSet.getInt("numVoucher");
        long idUnicoMov = resultSet.getInt("idUnicoMovimento");
        String codStato = resultSet.getString("codStato");
        String codCompagnia = resultSet.getString("codCompagnia");
        String numPolizza = resultSet.getString("numPolizza");
        String targa = resultSet.getString("targa");
        String vat = resultSet.getString("vat");

        LocalDate dataDecorrenza = null;
        Date dbDate = resultSet.getDate("dataDecorrenza");
        if (dbDate != null) { dataDecorrenza = dbDate.toLocalDate(); }

        LocalDate dataInizioServizio = null;
        dbDate = resultSet.getDate("dataInizioServ");
        if (dbDate != null) { dataInizioServizio = dbDate.toLocalDate(); }

        LocalDate dataFineServizio = null;
        dbDate = resultSet.getDate("dataFineServ");
        if (dbDate != null) { dataFineServizio = dbDate.toLocalDate(); }

        LocalDate dataInizioContratto = null;
        dbDate = resultSet.getDate("dataInizio");
        if (dbDate != null) { dataInizioContratto = dbDate.toLocalDate(); }

        LocalDate dataFineContratto = null;
        dbDate = resultSet.getDate("dataFine");
        if (dbDate != null) { dataFineContratto = dbDate.toLocalDate(); }

        return new Mov(0,idTracciato, idContratto,idUnicoMov, codStato, "",codCompagnia, numPolizza,"", targa, vat,null,
                dataDecorrenza, dataInizioContratto, dataFineContratto, "");
    }

    private Mov mapToMovGen(ResultSet resultSet) throws SQLException {
        int idTracciato = resultSet.getInt("idTracciato");
        int idContratto = resultSet.getInt("t_idContratto");
        long idUnicoMov = resultSet.getLong("idUnicoMovimento");
        String codStato = resultSet.getString("codStato");
        String numPolizza = resultSet.getString("numPolizza");
        String numPolSostituita = resultSet.getString("numPolSostituita");
        int numVoucher = resultSet.getInt("tg_numVoucher");
        String codCompagnia = resultSet.getString("codCompagnia");
        String tipoMovimento = resultSet.getString("tipoMovimento");

        LocalDate dataDecorrenza = null;
        Date dbDate = resultSet.getDate("dataDecorrenza");
        if (dbDate != null) dataDecorrenza = dbDate.toLocalDate();

        LocalDate dataArrivo = null;
        dbDate = resultSet.getDate("dataArrivo");
        if (dbDate != null) dataArrivo = dbDate.toLocalDate();

        LocalDate dataInizioServ = null;
        dbDate = resultSet.getDate("dataInizioServ");
        if (dbDate != null) dataInizioServ = dbDate.toLocalDate();

        LocalDate dataFineServ = null;
        dbDate = resultSet.getDate("dataFineServ");
        if (dbDate != null) dataFineServ = dbDate.toLocalDate();

        String vat = resultSet.getString("vat");
        String targa = resultSet.getString("targa");
        String nomeFileMovim = resultSet.getString("nomeFileMovim");

        // Restituisce un oggetto Mov con i valori mappati
        return new Mov(numVoucher, idTracciato, idContratto,idUnicoMov, codStato,tipoMovimento, codCompagnia, numPolizza,numPolSostituita, targa, vat, dataDecorrenza, dataInizioServ, dataFineServ, dataArrivo,nomeFileMovim);
    }


    public int findNumVoucherByIdContratto(int idContratto) {
        int numVoucher = 0;

        String query = "SELECT voucherGenerali FROM contratti_generali WHERE idContratto = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idContratto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    numVoucher = resultSet.getInt("voucherGenerali");
                }
            }
        } catch (Exception e) {
            System.out.println("findNumVoucherByIdContratto Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return numVoucher;
    }

    public List<Mov> findMovByIdContrattoContrattiDealer(int idContratto) {
        List<Mov> movList = new ArrayList<>();

        String query = "SELECT dataInizioServ, dataFineServ FROM contratti_dealer WHERE idContratto = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idContratto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Mov movNew = mapToMov(resultSet);
                    movList.add(movNew);
                }
            }
        } catch (SQLException e) {
            System.out.println("findMovByIdContratto Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return movList;
    }

    public void insertContrattoLog(StatoContratto contratto) {
        String query = "INSERT INTO bp1_statoContrattiLog (idTracciato,numVoucher, idContratto, tipoMovimento, codCompagnia, numPolizza, targa, vat, dataDecorrenza, dataInizioServizio, dataFineServizio, statoContratto, crossCompagnia, contrattiStato, statoPolizzeStato, invioI, interventiInstallazioneInCorso, interventiDisinstallazioneInCorso, interventiManutenzioneSostituzioneInCorso)" +
                " VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?)";

        try (Connection connection = DBManager.getConnectionPaysatDBNewCert();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, contratto.getIdTracciato());
            preparedStatement.setInt(2, contratto.getNumVoucher());
            preparedStatement.setInt(3, contratto.getIdContratto());
            preparedStatement.setString(4, contratto.getTipoMovimento());
            preparedStatement.setString(5, contratto.getCodCompagnia());
            preparedStatement.setString(6, contratto.getNumPolizza());
            preparedStatement.setString(7, contratto.getTarga());
            preparedStatement.setString(8, contratto.getVat());
            preparedStatement.setDate(9, contratto.getDataDecorrenza() != null ? Date.valueOf(contratto.getDataDecorrenza()) : null);
            preparedStatement.setDate(10, contratto.getDataInizioServizio() != null ? Date.valueOf(contratto.getDataInizioServizio()) : null);
            preparedStatement.setDate(11, contratto.getDataFineServizio() != null ? Date.valueOf(contratto.getDataFineServizio()) : null);
            preparedStatement.setString(12, String.valueOf(contratto.getStatoContratto()));
            preparedStatement.setBoolean(13, contratto.getCrossCompagnia());
            preparedStatement.setInt(14, contratto.getContrattiStato());
            preparedStatement.setInt(15, contratto.getStatoPolizzeStato());
            preparedStatement.setInt(16, contratto.getInvioI());
            preparedStatement.setInt(17, contratto.getInterventiInstallazioneInCorso());
            preparedStatement.setInt(18, contratto.getInterventiDisinstallazioneInCorso());
            preparedStatement.setInt(19, contratto.getInterventiManutenzioneSostituzioneInCorso());

            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("insertContrattoLog Log inserito correttamente per il contratto con ID: " + contratto.getIdContratto());
            }
        } catch (SQLException e) {
            System.err.println("insertContrattoLog Errore durante l'inserimento del log per il contratto: " + e.getMessage());
        }
    }

    public void insertPolizzaLog(Mov mov, String statoPolizza) {
        String query = "INSERT INTO bp1_PolizzaLog (numVoucher, idContratto, numPolizza,numPolizzaSostituita, dataDecorrenza, dataArrivo, tipoOperazione, statoPolizza)" +
        " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBManager.getConnectionPaysatDBNewCert();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, mov.getNumVoucher());
            preparedStatement.setInt(2, mov.getIdContratto());
            preparedStatement.setString(3, mov.getNumPolizza());
            preparedStatement.setString(4, mov.getNumPolSostituita());
            preparedStatement.setDate(5, mov.getDataDecorrenza() != null ? Date.valueOf(mov.getDataDecorrenza()) : null);
            preparedStatement.setDate(6, mov.getDataArrivo() != null ? Date.valueOf(mov.getDataArrivo()) : null);
            preparedStatement.setString(7, mov.getTipoMovimento());
            preparedStatement.setString(8, statoPolizza);

            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("insertPolizzaLog Log inserito correttamente per il contratto con ID: " + mov.getIdContratto());
            }
        } catch (SQLException e) {
            System.err.println("insertPolizzaLog Errore durante l'inserimento del log: " + e.getMessage());
        }
    }

    public StatoContratto findContrattoLog(int idContratto) {
        StatoContratto trovato = null;
        String query = "SELECT idContratto FROM bp1_statoContrattiLog WHERE idContratto = ?";

        try (Connection connection = DBManager.getConnectionDBSim();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idContratto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {

                }
            }
        } catch (Exception e) {
            System.out.println("findContrattoLog Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return trovato;
    }

    public int findInvioIByTarga(String targa) {
        int invioI = -1;

        String query = "select count(*) as inviato from infogenimp i where statoImpianto = 'I' and targa = ?;";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, targa);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    invioI = resultSet.getInt("inviato");
                }
            }
        } catch (Exception e) {
            System.out.println("findInvioIByTarga Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return invioI;
    }

    ///Tabella CONTRATTI
    public int findStatoContrattoById(int idContratto) {
        int statoContratto = -1;

        String query = "SELECT stato FROM contratti WHERE idContratto = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idContratto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    statoContratto = resultSet.getInt("stato");
                }
            }
        } catch (Exception e) {
            System.out.println("findStatoContrattoById Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return statoContratto;
    }

    public void updateStatoContrattoById(int idContratto, int nuovoStato) {
        String query = "UPDATE contratti SET stato = ? WHERE idContratto = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement updateStatement = connection.prepareStatement(query)) {

            updateStatement.setInt(1, nuovoStato);
            updateStatement.setInt(2, idContratto);

            int rowsUpdated = updateStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Contratto aggiornato");
            }
        } catch (Exception e) {
            System.err.println("updateStatoContrattoById Errore durante l'aggiornamento dello stato del contratto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    ///Tabella STATOPOLIZZE
    public int findStatoPolizzeByIdContratto(int idContratto) {
        int statoPolizza = -1;

        String query = "SELECT stato_polizza FROM stato_polizze WHERE idContratto = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idContratto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    statoPolizza = resultSet.getInt("stato_polizza");
                }
            }
        } catch (Exception e) {
            System.out.println("findStatoPolizzeByIdContratto Errore durante l'esecuzione della query:: " + e.getMessage());
            e.printStackTrace();
        }
        return statoPolizza;
    }

    public void updateStatoPolizzeByIdContratto(int idContratto, int nuovoStato) {
        String query = "UPDATE stato_polizze SET stato_polizza = ? WHERE idContratto = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement updateStatement = connection.prepareStatement(query)) {

            updateStatement.setInt(1, nuovoStato);
            updateStatement.setInt(2, idContratto);

            int rowsUpdated = updateStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("StatoPolizze aggiornata");
            }
        } catch (Exception e) {
            System.err.println("updateStatoPolizzeByIdContratto Errore durante l'aggiornamento statopolizze: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
