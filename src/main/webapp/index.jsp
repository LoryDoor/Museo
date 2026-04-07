<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%--
    FILE: webapp/index.jsp
    DESCRIZIONE: Pagina principale della webapp
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
        </header>

        <main>
            <div class="container">
                <div class="card">
                    <h2>Nuova prenotazione</h2>
                    <p>
                        Utilizza questo strumento per registrare una nuova prenotazione per una delle sale.
                    </p>
                    <a href="prenota.jsp">Prenota</a>
                </div>

                <div class="card">
                    <h2>Lista prenotazioni</h2>
                    <p>
                        Utilizza questo strumento per visualizzare la lista delle prenotazioni attualmente presenti.
                    </p>
                    <a href="visualizza">Visualizza</a>
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
