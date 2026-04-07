package edu.fauser.labs3.museo;

/*
    FILE: src/java/PrenotaServlet.java
    CONTENUTO: Servlet per la registrazione delle nuove prenotazioni
    AUTORE: Lorenzo Porta
    DATA: 30/03/2026
*/

import edu.fauser.DbUtility;

import java.io.*;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "prenotaServlet", value = "/prenota")
public class PrenotaServlet extends HttpServlet {
    // Costanti generiche
    private static final int MIN_AREE = 2;
    private static final int MIN_ID_AREA = 1;
    private static final int MAX_ID_AREA = 2;
    private static final String NUOVA_PRENOTAZIONE_OK = "ok";
    private static final String NUOVA_PRENOTAZIONE_FALLITA = "failed";

    public void init() throws ServletException {
        super.init();

        // Inizializzazione di DBUtility
        DbUtility dbu = DbUtility.getInstance(getServletContext());
        dbu.setProdCredentials("jdbc:mariadb://localhost:3306/db12778?maxPoolSize=2&pool", "db12778", "********");
        dbu.setDevCredentials("jdbc:mariadb://localhost:3306/db_museo?PoolSize=2&pool", "root", "********");

        // Verifica della corretta inizializzazione del DB per i compiti di questa servlet
        try(
            Connection conn = DriverManager.getConnection(dbu.getUrl(), dbu.getUser(), dbu.getPassword());
            Statement stmt = conn.createStatement()
        ) {
            String sql = "SELECT COUNT(*) FROM museo_aree";
            ResultSet resultSet = stmt.executeQuery(sql);

            resultSet.next(); // COUNT restituirà sempre una riga
            int count;
            if((count = resultSet.getInt(1)) < MIN_AREE) {
                System.err.println("[" + new java.util.Date() + "] ERRORE: Database non inizializzato. " +
                        "Trovate " + count + " aree, minimo richiesto: " + MIN_AREE);
                throw new RuntimeException("Il database non è inizializzato correttamente: trovate " + count +
                        " aree, quando il numero minimo è " + MIN_AREE);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[" + new java.util.Date() + "] ERRORE SQL: " + e.getMessage());
            throw new RuntimeException("Errore di accesso al database: " + e.getMessage(), e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Parametri del form
        String nominativo = request.getParameter("nominativo");
        String strDataVisita = request.getParameter("data");
        String strNumeroPartecipanti = request.getParameter("numero-partecipanti");
        String strArea = request.getParameter("area");

        boolean errori = false;
        HttpSession session = request.getSession();

        // Rimozione di eventuali errori precedenti
        session.removeAttribute("erroreNominativo");
        session.removeAttribute("erroreData");
        session.removeAttribute("erroreNumeroPartecipanti");
        session.removeAttribute("erroreArea");

        // Validazione nominativo
        if (!validaNominativo(nominativo)){
            session.setAttribute("erroreNominativo", 1);
            errori = true;
        }

        // Validazione data
        int esitoValidaDataVisita = validaDataVisita(strDataVisita);
        if(esitoValidaDataVisita != 0){
            session.setAttribute("erroreDataVisita", esitoValidaDataVisita);
            errori = true;
        }

        // Validazione numero partecipanti
        int esitoValidaNumeroPartecipanti = validaNumeroPartecipanti(strNumeroPartecipanti);
        if(esitoValidaNumeroPartecipanti != 0){
            session.setAttribute("erroreNumeroPartecipanti", esitoValidaNumeroPartecipanti);
            errori = true;
        }

        // Validazione area
        int area = 0;
        if(validaArea(strArea)){
            area = Integer.parseInt(strArea);
            if(!verificaDisponibilitaArea(area, strDataVisita)){
                session.setAttribute("erroreData", 2);
                session.setAttribute("erroreArea", 2);
                errori = true;
            }
        }
        else{
            session.setAttribute("erroreArea", 1);
            errori = true;
        }

        // Se ci sono errori, torna al form
        if (errori) {
            response.sendRedirect("prenota.jsp");
        }
        else {
            // Se non ci sono errori, procede con l'inserimento nel DB
            DbUtility dbu = DbUtility.getInstance(getServletContext());
            boolean inserimentoOk;
            try (
                Connection conn = DriverManager.getConnection(dbu.getUrl(), dbu.getUser(), dbu.getPassword());
                PreparedStatement stmtInsert = conn.prepareStatement("INSERT INTO museo_visite (Nominativo, Data, NumeroPartecipanti, LEG_ID_Area) VALUES (?, ?, ?, ?)")
            ) {
                stmtInsert.setString(1, nominativo);
                stmtInsert.setDate(2, java.sql.Date.valueOf(strDataVisita));
                stmtInsert.setInt(3, Integer.parseInt(strNumeroPartecipanti));
                stmtInsert.setInt(4, area);

                inserimentoOk = (stmtInsert.executeUpdate() == 1);
            }
            catch (SQLException e) {
                System.err.println("Errore di inserimento nel database: " + e.getMessage());
                inserimentoOk = false;
            }

            // Gestione esito inserimento
            session.setAttribute("esito", (inserimentoOk ? NUOVA_PRENOTAZIONE_OK : NUOVA_PRENOTAZIONE_FALLITA));
            response.sendRedirect("esito-prenotazione.jsp");
        }
    }

    private boolean validaNominativo(String nominativo){
        return (
            nominativo != null && // Campo valorizzato
            !nominativo.trim().isEmpty() && // Campo non vuoto
            nominativo.matches("[a-zA-Z\\s]+") // Campo composto da soli caratteri alfabetici e spazi
        );
    }

    private int validaDataVisita(String strDataVisita){
        Date dataVisitaDate;
        if (strDataVisita == null || strDataVisita.isEmpty()) {
            return 1; // Formato non valido
        }
        else{
            try {
                dataVisitaDate = java.sql.Date.valueOf(strDataVisita); // formato YYYY-MM-DD

                // Controllo data non precedente a oggi
                java.sql.Date oggi = new java.sql.Date(System.currentTimeMillis());
                if (dataVisitaDate.before(oggi)) {
                    return 3; // Data nel passato
                }
            } catch (IllegalArgumentException e) {
                return 1; // Formato non valido
            }
        }

        return 0;
    }

    private int validaNumeroPartecipanti(String strNumeroPartecipanti){
        int numeroPartecipanti;
        if (strNumeroPartecipanti == null || strNumeroPartecipanti.isEmpty()) {
            return 1; // Campo non compilato correttamente
        }
        else {
            try {
                numeroPartecipanti = Integer.parseInt(strNumeroPartecipanti);
                if (numeroPartecipanti < 3 || numeroPartecipanti > 30) {
                    return 2; //Campo compilato con valori oltre i limiti
                }
            }
            catch (NumberFormatException e) {
                return 1; // Campo non compilato correttamente
            }
        }

        return 0;
    }

    private boolean validaArea(String strArea){
        int area;
        if (strArea == null || strArea.isEmpty()) {
            return false;
        }
        else {
            try {
                area = Integer.parseInt(strArea);
                if (area < MIN_ID_AREA || area > MAX_ID_AREA) {
                    return false;
                }
            }
            catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    private boolean verificaDisponibilitaArea(int area, String strDataVisita){
        DbUtility dbu = DbUtility.getInstance(getServletContext());

        try (
            Connection conn = DriverManager.getConnection(dbu.getUrl(), dbu.getUser(), dbu.getPassword());
            PreparedStatement stmtVerifica = conn.prepareStatement("SELECT COUNT(*) FROM museo_visite WHERE LEG_ID_Area = ? AND Data = ?")
        ){
            stmtVerifica.setInt(1, area);
            stmtVerifica.setDate(2, java.sql.Date.valueOf(strDataVisita));
            ResultSet rs = stmtVerifica.executeQuery();

            rs.next();
            int count = rs.getInt(1);
            if (count > 0) {
                return false;
            }
        }
        catch (SQLException e) {
            System.err.println("Errore durante la verifica: " + e.getMessage());
            return false; // Se non è possibile eseguire la verifica si considera l'area come non disponibile
        }

        return true;
    }

    public void destroy() {
        super.destroy();
    }
}
