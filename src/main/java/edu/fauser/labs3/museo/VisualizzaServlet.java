package edu.fauser.labs3.museo;

/*
    FILE: src/main/java/edu/fauser/labs3/museo/VisualizzaServlet.java
    DESCRIZIONE: Servlet per la visualizzazione delle prenotazioni inserite a sistema
    AUTORE: Lorenzo Porta
    DATA: 06/04/2026
*/

import edu.fauser.DbUtility;

import java.io.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "visualizzaServlet", value = "/visualizza")
public class VisualizzaServlet extends HttpServlet {
    // Costanti per le porzioni statiche della pagina HTML
    private static final String HEADER =
        "<header>" +
        "<h1>Museo</h1>" +
        "<h3>Servizio di prenotazione</h3>" +
        "<a href='index.jsp'>Torna alla pagina principale</a>" +
        "</header>";

    private static final String FOOTER =
        "<footer>" +
        "Sito realizzato a scopo didattico." +
        "<br>Tutte le funzioni sono a solo scopo dimostrativo." +
        "<br><br>" +
        "Autore: Lorenzo Porta, Classe 5FIN, AS: 2025/2026" +
        "<br>ITT &quotG. Fauser&quot Via G. B. Ricci, 14, 28100, Novara, Italia." +
        "</footer>";

    private static final String HTML_START =
        "<!DOCTYPE html>" +
        "<html lang='it'>" +
        "<head>" +
        "<title>Visualizza prenotazioni</title>" +
        "<meta charset='utf-8'>" +
        "<link rel='stylesheet' type='text/css' href='css/style.css'>" +
        "</head>" +
        "<body>\n" +
        HEADER +
        "\n<main>" +
        "<div class='container'>";

    private static final String HTML_END =
        "</div>" +
        "</main>\n" +
        FOOTER +
        "\n</body>" +
        "</html>";

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
            String sql = DbUtility.isProduction() ?
                    "SELECT DISTINCT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'db12778' AND TABLE_NAME = 'museo_visite'" :
                    "SELECT DISTINCT 1 FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'db_museo' AND TABLE_NAME = 'museo_visite'";
            ResultSet resultSet = stmt.executeQuery(sql);

            if(!resultSet.next()){
                System.err.println("[" + new java.util.Date() + "] ERRORE: Database non inizializzato. " +
                        "Non è stata trovata la tabella \"museo_visite\"");
                throw new RuntimeException("Il database non è inizializzato correttamente: Non è stata trovata la " +
                        "tabella \"museo_visite\"");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.err.println("[" + new java.util.Date() + "] ERRORE SQL: " + e.getMessage());
            throw new RuntimeException("Errore di accesso al database: " + e.getMessage(), e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String areaFiltro = request.getParameter("area") == null ? "" : request.getParameter("area");

        out.println(HTML_START);

        // Stampa dinamica del form per il filtro di ricerca
        out.println("<div class='form-filtro'>");
        out.println("<form method='get' action='" + request.getContextPath() + "/visualizza'>");
        out.println("<label for='selArea'>Area:</label>");
        out.println("<select id='selArea' name='area'>");
        out.println("<option value=''> --- Seleziona area --- </option>");
        out.println("<option value='1'" + ("1".equals(areaFiltro) ? " selected" : "") + ">Area Rossa</option>");
        out.println("<option value='2'" + ("2".equals(areaFiltro) ? " selected" : "") + ">Area Verde</option>");
        out.println("</select>");
        out.println("<input type='submit' value='Applica'>");
        out.println("</form>");
        out.println("</div>");

        String sql;
        if(areaFiltro == null || areaFiltro.isEmpty()) {
            sql = "SELECT Nominativo, Data, NumeroPartecipanti, Colore FROM museo_visite, museo_aree WHERE LEG_ID_Area = ID_Area";
        }
        else{
            sql = "SELECT Nominativo, Data, NumeroPartecipanti, Colore FROM museo_visite, museo_aree WHERE LEG_ID_Area = ID_Area AND ID_Area = ?";
        }

        DbUtility dbu = DbUtility.getInstance(getServletContext());
        try(
            Connection conn = DriverManager.getConnection(dbu.getUrl(), dbu.getUser(), dbu.getPassword());
            PreparedStatement stmt = conn.prepareStatement(sql)
        ){
            // Se è stato applicato il filtro viene aggiunto il parametro alla query
            if(areaFiltro != null && !areaFiltro.isEmpty()) {
                stmt.setInt(1, Integer.parseInt(areaFiltro));
            }

            ResultSet resultSet = stmt.executeQuery();

            String rowPattern =
                "<tr class='data-table'>" +
                "<td class='data-table'>%s</td>" +
                "<td class='data-table'>%s</td>" +
                "<td class='data-table'>%d</td>" +
                "<td class='data-table'>%s</td>" +
                "</tr>";

            if(resultSet.next()){
                out.println("<div class='card'>");
                out.println("<table class='data-table'>");

                out.println("<thead class='data-table'>");
                out.println("<tr class='data-table'>");
                out.println("<th class='data-table'>Nominativo</th>");
                out.println("<th class='data-table'>Data prenotata</th>");
                out.println("<th class='data-table'>Numero di partecipanti</th>");
                out.println("<th class='data-table'>Area di interesse</th>");
                out.println("</tr>");
                out.println("</thead>");

                out.println("<tbody class='data-table'>");
                do{
                    // Fetch dei dati dal ResultSet
                    String nominativo = resultSet.getString(1);
                    String data = dtf.format(resultSet.getDate(2).toLocalDate());
                    int numeroPartecipanti = resultSet.getInt(3);
                    String area = resultSet.getString(4);

                    // Stampa una riga della tabella già formattata
                    out.println(String.format(rowPattern, nominativo, data, numeroPartecipanti, area));
                } while(resultSet.next());
                out.println("</tbody>");
                out.println("</table>");
            }
            else {
                out.println("<div class='empty'>Al momento nel sistema non risultano prenotazioni registrate.</div>");
            }
        }
        catch (SQLException e) {
            System.err.println("Errore di connessione al database: " + e.getMessage());
            out.println("<div class='error'>Errore di connessione al database</div>");
        }

        out.println(HTML_END);
    }

    public void destroy() {
        super.destroy();
    }
}
