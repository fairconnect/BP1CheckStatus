package org.example.periferica;

import org.example.DBManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PerifericaDAO {

    public List<Periferica> findPerifericheByIdContratto(int idContratto) {
        List<Periferica> perifericheList = new ArrayList<>();

        String query = "SELECT * FROM statoperiferiche WHERE idContratto = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idContratto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Periferica perifericaNew = mapToPeriferica(resultSet);
                    perifericheList.add(perifericaNew);
                }
            }
        } catch (SQLException e) {
            System.out.println("findPerifericheByIdContratto Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return perifericheList;
    }

    public List<Periferica> findPerifericheExtByIdContratto(int idContratto) {
        List<Periferica> perifericheList = new ArrayList<>();

        String query = "SELECT * FROM statoperiferiche s JOIN statoperiferiche_ext se ON s.idStatoPeriferica = se.idStatoPeriferica WHERE idContratto = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idContratto);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Periferica perifericaNew = mapToPerifericaExt(resultSet);
                    perifericheList.add(perifericaNew);
                }
            }
        } catch (SQLException e) {
            System.out.println("findPerifericheExtByIdContratto Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return perifericheList;
    }

    public List<Periferica> findPerifericheById(int idPeriferica) {
        List<Periferica> perifericheList = new ArrayList<>();

        String query = "SELECT * FROM statoperiferiche WHERE idPeriferica = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idPeriferica);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Periferica perifericaNew = mapToPeriferica(resultSet);
                    perifericheList.add(perifericaNew);
                }
            }
        } catch (SQLException e) {
            System.out.println("findPerifericheById Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return perifericheList;
    }

    public List<Periferica> findPerifericheExtById(int idPeriferica) {
        List<Periferica> perifericheList = new ArrayList<>();

        String query = "SELECT * FROM statoperiferiche s JOIN statoperiferiche_ext se ON s.idStatoPeriferica = se.idStatoPeriferica WHERE idPeriferica = ?";

        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idPeriferica);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Periferica perifericaNew = mapToPerifericaExt(resultSet);
                    perifericheList.add(perifericaNew);
                }
            }
        } catch (SQLException e) {
            System.out.println("findPerifericheExtById Errore durante l'esecuzione della query: " + e.getMessage());
            e.printStackTrace();
        }
        return perifericheList;
    }

    private Periferica mapToPeriferica(ResultSet resultSet) throws SQLException {
        int idContratto = resultSet.getInt("idContratto");
        int idPeriferica = resultSet.getInt("idPeriferica");
        int idStatoPeriferica = resultSet.getInt("idStatoPeriferica");

        String stato = resultSet.getString("stato");
        String causale = resultSet.getString("causale");
        String note = resultSet.getString("note");

        LocalDate dataInizio = null;
        Date dbDate = resultSet.getDate("dataInizio");
        if (dbDate != null) { dataInizio = dbDate.toLocalDate(); }

        LocalDate dataCollaudo = null;
        dbDate = resultSet.getDate("dataCollaudo");
        if (dbDate != null) { dataCollaudo = dbDate.toLocalDate(); }

        LocalDate dataDisattivazione = null;
        dbDate = resultSet.getDate("dataDisattivazione");
        if (dbDate != null) { dataDisattivazione = dbDate.toLocalDate(); }

        return new Periferica(idContratto, idPeriferica, idStatoPeriferica, stato, causale, "", "", dataInizio, null, dataCollaudo, dataDisattivazione, null, note);
    }

    private Periferica mapToPerifericaExt(ResultSet resultSet) throws SQLException {
        int idContratto = resultSet.getInt("idContratto");
        int idPeriferica = resultSet.getInt("idPeriferica");
        int idStatoPeriferica = resultSet.getInt("idStatoPeriferica");

        String stato = resultSet.getString("stato");
        String causale = resultSet.getString("causale");
        String note = resultSet.getString("note");
        String causale_disattivazione = resultSet.getString("causale_disattivazione");
        String ubicazione = resultSet.getString("ubicazione");

        LocalDate dataInizio = null;
        Date dbDate = resultSet.getDate("dataInizio");
        if (dbDate != null) { dataInizio = dbDate.toLocalDate(); }

        LocalDate dataCollaudo = null;
        dbDate = resultSet.getDate("dataCollaudo");
        if (dbDate != null) { dataCollaudo = dbDate.toLocalDate(); }

        LocalDate dataDisattivazione = null;
        dbDate = resultSet.getDate("dataDisattivazione");
        if (dbDate != null) { dataDisattivazione = dbDate.toLocalDate(); }

        LocalDate data_primo_collaudo = null;
        dbDate = resultSet.getDate("data_primo_collaudo");
        if (dbDate != null) { data_primo_collaudo = dbDate.toLocalDate(); }

        LocalDate data_disinstallazione = null;
        dbDate = resultSet.getDate("data_disinstallazione");
        if (dbDate != null) { data_disinstallazione = dbDate.toLocalDate(); }

        return new Periferica(idContratto, idPeriferica, idStatoPeriferica, stato, causale, causale_disattivazione, ubicazione, dataInizio, data_primo_collaudo, dataCollaudo, dataDisattivazione, data_disinstallazione,note);
    }

    public void insertPerifericaLog(StatoPeriferica periferica) {
     String query = "INSERT INTO bp1_statoPerifericheLog (idStatoPeriferica, idContratto, idPeriferica, stato, causale, causale_disattivazione, note, dataInizio, dataPrimoCollaudo, dataCollaudo, dataDisattivazione, dataDisinstallazione, statoPeriferica) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection connection = DBManager.getConnectionPaysatDBNewCert();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                preparedStatement.setInt(1, periferica.getIdStatoPeriferica()); // idStatoPeriferica
                preparedStatement.setInt(2, periferica.getIdContratto()); // idContratto
                preparedStatement.setInt(3, periferica.getIdPeriferica()); // idPeriferica
                preparedStatement.setString(4, periferica.getStato()); // stato
                preparedStatement.setString(5, periferica.getCausale()); // causale
                preparedStatement.setString(6, periferica.getCausaleDisattivazione()); // causale_disattivazione
                preparedStatement.setString(7, periferica.getNote()); // note
                preparedStatement.setDate(8, periferica.getDataInizio() != null ? Date.valueOf(periferica.getDataInizio()) : null); // dataInizio
                preparedStatement.setDate(9, periferica.getDataPrimoCollaudo() != null ? Date.valueOf(periferica.getDataPrimoCollaudo()) : null); // dataPrimoCollaudo
                preparedStatement.setDate(10, periferica.getDataCollaudo() != null ? Date.valueOf(periferica.getDataCollaudo()) : null); // dataCollaudo
                preparedStatement.setDate(11, periferica.getDataDisattivazione() != null ? Date.valueOf(periferica.getDataDisattivazione()) : null); // dataDisattivazione
                preparedStatement.setDate(12, periferica.getDataDisinstallazione() != null ? Date.valueOf(periferica.getDataDisinstallazione()) : null); // dataDisinstallazione
                preparedStatement.setString(13, String.valueOf(periferica.getStatoPeriferica())); // statoPeriferica

                int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("insertPerifericaLog Log inserito correttamente per la periferica con ID: " + periferica.getIdPeriferica());
            }
        } catch (SQLException e) {
            System.err.println("insertPerifericaLog Errore durante l'inserimento del log per la periferica: " + e.getMessage());
        }
    }

    public void updatePerifericaById(int idPeriferica, int idStatoPeriferica, String stato) {
        if (stato.equals("A")) {
            attivaPeriferica(idStatoPeriferica);
        } else if (stato.equals("D")){
            disattivaPeriferica(idStatoPeriferica);
        } else {
            System.out.println("Verifiche future su altri stati periferica");
        }
    }

    private void attivaPeriferica(int idStatoPeriferica) {
        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica()) {
            connection.setAutoCommit(false);

            LocalDate dataOggi = LocalDate.now();

            String query1 = "UPDATE statoperiferiche SET stato = 'A', dataDisattivazione = ?, causale = 'Allineamento periferica (Attività)' WHERE idStatoPeriferica = ?";
            try (PreparedStatement preparedStatement1 = connection.prepareStatement(query1)) {
                preparedStatement1.setDate(1, null);
                preparedStatement1.setInt(2, idStatoPeriferica);

                preparedStatement1.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            System.err.println("attivaPeriferica Errore durante l'aggiornamento: " + e.getMessage());
            try (Connection connection = DBManager.getConnectionPaysatDBNewReplica()) {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("attivaPeriferica Errore durante il rollback: " + rollbackEx.getMessage());
            }
        }
    }

    private void disattivaPeriferica(int idStatoPeriferica) {
        try (Connection connection = DBManager.getConnectionPaysatDBNewReplica()) {
            connection.setAutoCommit(false);

            LocalDate dataOggi = LocalDate.now();

            String query1 = "UPDATE statoperiferiche SET stato = 'D', dataDisattivazione = ?, causale = 'Allineamento periferica (Disattivata)' WHERE idStatoPeriferica = ?";
            try (PreparedStatement preparedStatement1 = connection.prepareStatement(query1)) {
                preparedStatement1.setDate(1, Date.valueOf(dataOggi));
                preparedStatement1.setInt(2, idStatoPeriferica);

                preparedStatement1.executeUpdate();
            }

            String query2 = "UPDATE statoperiferiche_ext SET causale_disattivazione = 'Allineamento periferica (Disattivata)' WHERE idStatoPeriferica = ?";
            try (PreparedStatement preparedStatement2 = connection.prepareStatement(query2)) {
                preparedStatement2.setInt(1, idStatoPeriferica);
                preparedStatement2.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            System.err.println("disattivaPeriferica Errore durante l'aggiornamento: " + e.getMessage());
            // In caso di errore, annulla tutte le operazioni
            try (Connection connection = DBManager.getConnectionPaysatDBNewReplica()) {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("disattivaPeriferica Errore durante il rollback: " + rollbackEx.getMessage());
            }
        }
    }
}
