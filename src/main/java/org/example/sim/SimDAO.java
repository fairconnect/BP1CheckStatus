package org.example.sim;

import org.example.DBManager;
import org.example.contratto.StatoContratto;
import org.example.periferica.StatoPeriferica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SimDAO {

    public List<Sim> findSimByIdPeriferica(int idPeriferica) {
        List<Sim> simList = new ArrayList<>();

        String query = "SELECT d.idPeriferica, d.s_iccid, si.CARRIER, si.SIM_State, sa.data_packet_activity FROM device_status d LEFT JOIN sim_inventory si ON d.s_iccid = si.ICCID LEFT JOIN sim_activity sa ON sa.ICCID = si.ICCID WHERE d.idPeriferica = ?;";

        try (Connection connection = DBManager.getConnectionDBSim();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idPeriferica);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Sim simNew = mapToSim(resultSet);
                    simList.add(simNew);
                }
            }
        } catch (SQLException e) {
            //LOGGER.severe("Errore durante l'esecuzione della query: " + e.getMessage());
        }
        return simList;
    }

    private Sim mapToSim(ResultSet resultSet) throws SQLException {
        int idPeriferica = resultSet.getInt("idPeriferica");
        String iccid = resultSet.getString("s_iccid");
        String carrier = resultSet.getString("CARRIER");
        String sim_state = resultSet.getString("SIM_State");
        String data_packet_activity = resultSet.getString("data_packet_activity");

        return new Sim(idPeriferica, iccid, carrier, sim_state, data_packet_activity, "");
    }

    public void insertSimLog(StatoContratto statoContratto, StatoPeriferica statoPeriferica, Sim sim, boolean aggiornatoContratto, boolean aggiornatoPeriferica) {
        String query = "INSERT INTO bp1_simLog (idContratto, statoContratto, idPeriferica, statoPeriferica, iccid, carrier, simState, statoAtteso, aggiornatoContratto, aggiornatoPeriferica) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

        try (Connection connection = DBManager.getConnectionPaysatDBNewCert();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, statoContratto.getIdContratto());
            preparedStatement.setString(2, String.valueOf(statoContratto.getStatoContratto()));
            preparedStatement.setInt(3, sim.getIdPeriferica());
            preparedStatement.setString(4, String.valueOf(statoPeriferica.getStatoPeriferica()));
            preparedStatement.setString(5, sim.getIccid());
            preparedStatement.setString(6, sim.getCarrier());
            preparedStatement.setString(7, sim.getSimState());
            //preparedStatement.setString(5, sim.getDataPacketActivity());
            preparedStatement.setString(8, sim.getStatoAtteso());
            preparedStatement.setBoolean(9, aggiornatoContratto);
            preparedStatement.setBoolean(10, aggiornatoPeriferica);

            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                //System.out.println("insertSimLog Log inserito correttamente per la sim con iccid: " + sim.getIccid());
            }
        } catch (SQLException e) {
            System.err.println("insertSimLog Errore durante l'inserimento del log per la sim: " + e.getMessage());
        }
    }
}
