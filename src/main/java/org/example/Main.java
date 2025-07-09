package org.example;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        //True ricerca per idContratto, False ricerca per idPeriferica
        boolean findByIdContratto = false;

        try {
            CheckService checkService = new CheckService();
            List<Integer> lista = new ArrayList<>();

            if (findByIdContratto) {
                lista = leggiIdContrattiDaFile("C:/Users/kevin.pena/IdeaProjects/BP1CheckStatus/src/main/resources/idContratto.txt");
            } else {
                lista= leggiIdContrattiDaFile("C:/Users/kevin.pena/IdeaProjects/BP1CheckStatus/src/main/resources/idPeriferica.txt");
            }

            for (int numero : lista) {
                if (!findByIdContratto) {
                    System.out.println("main : Richiamo del metodo getIdContratto con idPerifica: " + numero);
                    int idCtr = checkService.getIdContratto(numero);
                    System.out.println("main : Richiamo del metodo step1 con idContratto: " + idCtr + " (idPeriferica: " + numero + ")");
                    checkService.step1(idCtr, EnumTypes.EnumComagnia.JENIOT);
                } else {
                    System.out.println("main : Richiamo del metodo step1 con idContratto: " + numero);
                    checkService.step1(numero, EnumTypes.EnumComagnia.JENIOT);
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("main : Errore: l'idContratto");
        }
    }

    public static List<Integer> leggiIdContrattiDaFile(String percorsoFile) {
        List<Integer> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(percorsoFile))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    try {
                        int id = Integer.parseInt(linea);
                        lista.add(id);
                    } catch (NumberFormatException e) {
                        System.err.println("Id non valido: " + linea);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file"  + e.getMessage());
            e.printStackTrace();

        }
        return lista;
    }

    /*public static void readCsvAndInsertData(String csvFilePath, ContrattoDAO contrattoDAO) {
        BufferedReader br = null;

        try {
            // Aprire il file CSV
            br = new BufferedReader(new FileReader(csvFilePath));
            String line;

            // Leggere la prima riga (intestazione) e saltarla, se presente
            line = br.readLine();

            // Leggere ogni riga successiva
            while ((line = br.readLine()) != null) {
                // Separare i valori in base al separatore "," del CSV
                String[] values = line.split(",");

                // Assumiamo che il CSV abbia due colonne: nome e cognome
                if (values.length >= 2) {
                    String nome = values[0].trim();  // Primo valore: nome
                    String cognome = values[1].trim();  // Secondo valore: cognome

                    // Utilizzare il  per inserire i dati nel database
                    //contrattoDAO.insertData(nome, cognome);
                } else {
                    System.err.println("Formato CSV non valido nella riga: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file CSV: " + e.getMessage());
        } finally {
            // Chiude il BufferedReader in tutti i casi
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                System.err.println("Errore durante la chiusura del file: " + e.getMessage());
            }
        }
    }*/
}
