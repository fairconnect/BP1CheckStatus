package org.example;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        ///Settare parametri per effettuare la ricerca
        boolean findByIdContratto = false;
        boolean findByIdPeriferica = false;
        boolean findByIccid = true;

        try {
            CheckService checkService = new CheckService();
            List<Integer> lista = new ArrayList<>();
            List<String> listaString = new ArrayList<>();

            if (findByIdContratto) {
                lista = leggiIntDaFile("C:/Users/kevin.pena/IdeaProjects/BP1CheckStatus/src/main/resources/idContratto.txt");
            } else if (findByIdPeriferica) {
                lista = leggiIntDaFile("C:/Users/kevin.pena/IdeaProjects/BP1CheckStatus/src/main/resources/idPeriferica.txt");
            } else if (findByIccid) {
                listaString= leggiStringheDaFile("C:/Users/kevin.pena/IdeaProjects/BP1CheckStatus/src/main/resources/ICCID.txt");
            }

            if (findByIccid) {
                for (String iccid : listaString) {
                    System.out.println("main : Richiamo del metodo getIdContratto con iccid: " + iccid);
                    int idperi = checkService.getIdPerifericaBySim(iccid);
                    System.out.println("main : Richiamo del metodo getIdContratto con idPerifica: " + idperi);
                    int idCtr = checkService.getIdContratto(idperi);
                    System.out.println("main : Richiamo del metodo step1 con idContratto: " + idCtr + " (idPerifica: " + idperi + ")");
                    checkService.step1(idCtr, iccid);
                }
            }

            if (findByIdContratto || findByIdPeriferica) {
                for (int id : lista) {
                    if (findByIdPeriferica) {
                        System.out.println("main : Richiamo del metodo getIdContratto con idPerifica: " + id);
                        int idCtr = checkService.getIdContratto(id);
                        System.out.println("main : Richiamo del metodo step1 con idContratto: " + idCtr + " (idPerifica: " + id + ")");
                        checkService.step1(idCtr, String.valueOf(id));
                    } else if (findByIdContratto) {
                        System.out.println("main : Richiamo del metodo step1 con idContratto: " + id);
                        checkService.step1(id, String.valueOf(id));
                    }
                }
            }

        } catch (NumberFormatException e) {
            System.err.println("main : Errore: l'idContratto");
        }
    }

    public static List<Integer> leggiIntDaFile(String percorsoFile) {
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

    public static List<String> leggiStringheDaFile(String percorsoFile) {
        List<String> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(percorsoFile))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    lista.add(linea);
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nella lettura del file: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }
}
