<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.fauser.labs3.museo.ValidazioneEsiti.*" %>

<%--
    FILE: src/main/webapp/prenota.jsp
    DESCRIZIONE: Form per l'inserimento di una nuova prenotazione
    AUTORE: Lorenzo Porta
    DATA: 05/04/2026
--%>

<!DOCTYPE html>
<html lang="it">
    <head>
        <title>Museo</title>
        <meta charset='utf-8'>
        <link rel='stylesheet' type='text/css' href='css/style.css'>
    </head>

    <body>
        <header>
            <h1>Museo</h1>
            <h3>Servizio di prenotazione</h3>
            <a href="index.jsp">Torna alla pagina principale</a>
        </header>

        <main>
            <div class="container">
                <h1>Nuova prenotazione</h1>
                <form method="post" action="prenota">
                    <table class="form-table">
                        <tr class="form-table">
                            <th class="form-table"><label for="txtNominativo">Nome completo</label></th>
                            <td class="form-table">
                                <input type="text" id="txtNominativo" name="nominativo" required>
                                <% if(session.getAttribute("erroreNominativo") != null) { %>
                                    <div class="error">Il nominativo fornito non è valido.</div>
                                <% } %>
                            </td>
                        </tr>
                        <tr class="form-table">
                            <th class="form-table"><label for="txtData">Data di visita</label></th>
                            <td>
                                <input type="date" id="txtData" name="data" required>
                                <%
                                    if(session.getAttribute("erroreData") != null){
                                        int erroreData = (int) session.getAttribute("erroreData");
                                        if(erroreData == EsitoData.FORMATO_NON_VALIDO.getCodice()) {
                                %>
                                                <div class="error">La data indicata è in un formato non valido.</div>
                                <%
                                        } else if(erroreData == EsitoData.NON_DISPONIBILE.getCodice()) {
                                %>
                                                <div class="error">La data selezionata è già occupata per l'area selezionata.</div>
                                <%
                                        } else if(erroreData == EsitoData.DATA_PASSATA.getCodice()) {
                                %>
                                                <div class="error">La data selezionata deve essere successiva alla data odierna.</div>
                                <%
                                        }
                                    }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <th><label for="txtNumeroPartecipanti">Numero di partecipanti</label></th>
                            <td>
                                <input type="number" id="txtNumeroPartecipanti" name="numero-partecipanti" min="3" max="30" step="1" required>
                                <%
                                    if(session.getAttribute("erroreNumeroPartecipanti") != null) {
                                        int erroreNumeroPartecipanti = (int)session.getAttribute("erroreNumeroPartecipanti");
                                        if(erroreNumeroPartecipanti == EsitoNumeroPartecipanti.FORMATO_NON_VALIDO.getCodice()){
                                %>
                                            <div class="error">Indicare un numero di partecipanti valido.</div>
                                <%
                                        } else if(erroreNumeroPartecipanti == EsitoNumeroPartecipanti.LIMITI_SUPERATI.getCodice()){
                                %>
                                            <div class="error">Indicare un numero di partecipati compreso tra 3 e 30.</div>
                                <%
                                        }
                                    }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <th><label for="selArea">Area di interesse</label></th>
                            <td>
                                <select id="selArea" name="area" required>
                                    <option value="" selected> --- Seleziona area --- </option>
                                    <option value="1">Area Rossa</option>
                                    <option value="2">Area Verde</option>
                                </select>
                                <%
                                    if(session.getAttribute("erroreArea") != null) {
                                        int erroreArea = (int)session.getAttribute("erroreArea");
                                        if(erroreArea == EsitoArea.NON_SELEZIONATA.getCodice()){
                                %>
                                            <div class="error">Selezionare un'area valida.</div>
                                <%
                                        } else if(erroreArea == EsitoArea.NON_DISPONIBILE.getCodice()){
                                %>
                                            <div class='error'>L&#39;area selezionata non è disponibile per la data scelta.</div>
                                <%
                                        }
                                    }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2"><input type="submit" name="invia" value="Invia"></td>
                        </tr>
                    </table>
                </form>
            </div>
        </main>

        <footer>
            Sito realizzato a scopo didattico.
            <br>Tutte le funzioni sono a solo scopo dimostrativo.
            <br><br>
            Autore: Lorenzo Porta, Classe 5FIN, AS: 2025/2026
            <br>ITT "G. Fauser" Via G. B. Ricci, 14, 28100, Novara, Italia.
        </footer>

        <script>
            document.addEventListener('DOMContentLoaded', () => {
                const input = document.getElementById('txtData');
                if(input) {
                    const domani = new Date(); // Parte dalla data odierna
                    domani.setDate(domani.getDate() + 1); // Aggiunge 1 giorno
                    input.min = domani.toISOString().split('T')[0]; // Formatta come YYYY-MM-DD e imposta come minimo selezionabile
                }
            });

            /*
                1) Se input non è null o undefined imposta il suo attributo min
                2) .toISOString() => restituisce es. "2026-04-12T18:30:00.000Z"
                3) .split('T')[0] => estrae solo "2026-04-12"
             */
        </script>
    </body>
</html>
