<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%--
    FILE: webapp/esito-prenotazione.jsp
    DESCRIZIONE: Pagina per mostrare gli esiti della prenotazione effetuata dall'utente
    AUTORE: Lorenzo Porta
    DATA: 05/04/2026
--%>

<%
    final String NUOVA_PRENOTAZIONE_OK = "ok";
    final String NUOVA_PRENOTAZIONE_FALLITA = "failed";
%>

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
                <div class="card">
                    <h2>Esito prenotazione</h2>
                    <%if (session.getAttribute("esito").equals(NUOVA_PRENOTAZIONE_OK)) {%>
                        <p class="success">
                            La tua prenotazione &egrave; andata a buon fine.
                            <br>Grazie per aver utilizzato il nostro servizio.
                        </p>
                    <% } else if(session.getAttribute("esito").equals(NUOVA_PRENOTAZIONE_FALLITA)){ %>
                        <p class="failed">
                            Ci dispiace, abbiamo riscontrato dei problemi nella registrazione della tua prenotazione.<br>
                            Ti preghiamo di riprovare più tardi.
                        </p>
                    <% } %>

                    <a href="index.jsp">Torna alla pagina principale</a>
                </div>
            </div>
        </main>

        <footer>
            Sito realizzato a scopo didattico.
            <br>Tutte le funzioni sono a solo scopo dimostrativo.
            <br><br>
            Autore: Lorenzo Porta, Classe 5FIN, AS: 2025/2026
            <br>ITT "G. Fauser" Via G. B. Ricci, 14, 28100, Novara, Italia.
        </footer>
    </body>
</html>
