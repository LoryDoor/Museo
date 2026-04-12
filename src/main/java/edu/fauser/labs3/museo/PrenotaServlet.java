package edu.fauser.labs3.museo;

/*
    FILE: src/java/PrenotaServlet.java
    CONTENUTO: Servlet per la registrazione delle nuove prenotazioni
    AUTORE: Lorenzo Porta
    DATA: 30/03/2026
*/

import edu.fauser.DbUtility;
import edu.fauser.labs3.museo.ValidazioneEsiti.*;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "prenotaServlet", value = "/prenota")
public class PrenotaServlet extends HttpServlet {
    // Costanti per la validazione
    private static final int MIN_AREE = 2;
    private static final int MIN_ID_AREA = 1;
    private static final int MAX_ID_AREA = 2;
    private static final int MIN_NUMERO_PARTECIPANTI = 3;
    private static final int MAX_NUMERO_PARTECIPANTI = 30;

    private static final List<Double> prezzi_aree = new ArrayList<>(MIN_AREE);

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

        // Caricamento dei prezzi delle aree
        try(
            Connection conn = DriverManager.getConnection(dbu.getUrl(), dbu.getUser(), dbu.getPassword());
            Statement stmt = conn.createStatement()
        ){
            String sql = "SELECT Prezzo FROM museo_aree";
            ResultSet resultSet = stmt.executeQuery(sql);

            if(!resultSet.next()){
                System.err.println("[" + new java.util.Date() + "] ERRORE: Non è stato possibile ricavare i prezzi " +
                        "dal database in fase di inizializzazione.");
                throw new RuntimeException("Non è stato possibile ricavare delle informazioni dal database in fase di" +
                        "inizializzazione.");
            }
            else{
                do{
                    double prezzo = resultSet.getDouble(1);
                    prezzi_aree.add(prezzo);
                } while(resultSet.next());
            }
        }
        catch (SQLException e){
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
            session.setAttribute("erroreNominativo", EsitoNominativo.FORMATO_NON_VALIDO.getCodice());
            errori = true;
        }

        // Validazione data
        EsitoData esitoValidaDataVisita = validaDataVisita(strDataVisita);
        if(esitoValidaDataVisita != EsitoData.OK){
            session.setAttribute("erroreData", esitoValidaDataVisita.getCodice());
            errori = true;
        }

        // Validazione numero partecipanti
        EsitoNumeroPartecipanti esitoValidaNumeroPartecipanti = validaNumeroPartecipanti(strNumeroPartecipanti);
        if(esitoValidaNumeroPartecipanti != EsitoNumeroPartecipanti.OK){
            session.setAttribute("erroreNumeroPartecipanti", esitoValidaNumeroPartecipanti.getCodice());
            errori = true;
        }

        // Validazione area
        int area = 0;
        boolean dataValida = (esitoValidaDataVisita == ValidazioneEsiti.EsitoData.OK);

        if(validaArea(strArea)){
            area = Integer.parseInt(strArea);

            // Verifica la disponibilità solo se la data è valida
            if(dataValida && !verificaDisponibilitaArea(area, strDataVisita)){
                session.setAttribute("erroreData", EsitoData.NON_DISPONIBILE);
                session.setAttribute("erroreArea", EsitoArea.NON_DISPONIBILE);
                errori = true;
            }
        }
        else{
            session.setAttribute("erroreArea", EsitoArea.NON_SELEZIONATA);
            errori = true;
        }

        if (errori) { // Se ci sono errori, torna al form
            response.sendRedirect("prenota.jsp");
        }
        else { // Se non ci sono errori, procede con l'inserimento nel DB

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

                // Se la query inserisce 1 riga l'inserimento è andato a buon fine
                inserimentoOk = (stmtInsert.executeUpdate() == 1);
            }
            catch (SQLException e) {
                System.err.println("Errore di inserimento nel database: " + e.getMessage());
                inserimentoOk = false;
            }

            // Calcolo del prezzo totale dei biglietti.
            // La variabile area è un indice 1-based, mentre per l'ArrayList occorre un indice 0-based.
            double importo_totale = Integer.parseInt(strNumeroPartecipanti) * prezzi_aree.get(area-1);

            // Gestione del feedback sull'esito dell'inserimento
            session.setAttribute("esito", (inserimentoOk ? EsitoPrenotazione.OK.getCodice() : EsitoPrenotazione.FALLITA.getCodice()));
            session.setAttribute("totale", inserimentoOk ? importo_totale : "");
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

    private EsitoData validaDataVisita(String strDataVisita){
        Date dataVisitaDate;
        if (strDataVisita == null || strDataVisita.isEmpty()) {
            return EsitoData.FORMATO_NON_VALIDO;
        }
        else{
            try {
                dataVisitaDate = java.sql.Date.valueOf(strDataVisita); // formato YYYY-MM-DD

                // Controllo data successiva a oggi
                java.sql.Date oggi = new java.sql.Date(System.currentTimeMillis());
                if (!dataVisitaDate.after(oggi)) { // non oggi o prima
                    return EsitoData.DATA_PASSATA;
                }
            } catch (IllegalArgumentException e) {
                return EsitoData.FORMATO_NON_VALIDO;
            }
        }

        return EsitoData.OK;
    }

    private EsitoNumeroPartecipanti validaNumeroPartecipanti(String strNumeroPartecipanti){
        int numeroPartecipanti;
        if (strNumeroPartecipanti == null || strNumeroPartecipanti.isEmpty()) {
            return EsitoNumeroPartecipanti.FORMATO_NON_VALIDO;
        }
        else {
            try {
                numeroPartecipanti = Integer.parseInt(strNumeroPartecipanti);
                if (numeroPartecipanti < MIN_NUMERO_PARTECIPANTI || numeroPartecipanti > MAX_NUMERO_PARTECIPANTI) {
                    return EsitoNumeroPartecipanti.LIMITI_SUPERATI;
                }
            }
            catch (NumberFormatException e) {
                return EsitoNumeroPartecipanti.FORMATO_NON_VALIDO;
            }
        }

        return EsitoNumeroPartecipanti.OK;
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
