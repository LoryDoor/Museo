package edu.fauser.labs3.museo;

/*
    FILE: src/main/java/edu/fauser/labs3/museo/ValidazioneEsiti.java
    DESCRIZIONE: Libreria di Enum per i codici di errore delle validazioni
    AUTORE: Lorenzo Porta
    DATA: 12/04/2026
*/

public class ValidazioneEsiti {
    public enum EsitoNominativo {
        OK(0),
        FORMATO_NON_VALIDO(1);

        private final int codice;

        EsitoNominativo(int codice){
            this.codice = codice;
        }

        public int getCodice(){
            return codice;
        }
    }

    public enum EsitoData {
        OK(0),
        FORMATO_NON_VALIDO(1),
        NON_DISPONIBILE(2),
        DATA_PASSATA(3);

        private final int codice;

        EsitoData(int codice) {
            this.codice = codice;
        }

        public int getCodice() {
            return codice;
        }
    }

    public enum EsitoNumeroPartecipanti {
        OK(0),
        FORMATO_NON_VALIDO(1),
        LIMITI_SUPERATI(2);

        private final int codice;

        EsitoNumeroPartecipanti(int codice) {
            this.codice = codice;
        }

        public int getCodice() {
            return codice;
        }
    }

    public enum EsitoArea {
        OK(0),
        NON_SELEZIONATA(1),
        NON_DISPONIBILE(2);

        private final int codice;

        EsitoArea(int codice) {
            this.codice = codice;
        }

        public int getCodice() {
            return codice;
        }
    }

    public enum EsitoPrenotazione{
        OK(0),
        FALLITA(1);

        private final int codice;

        EsitoPrenotazione(int codice) {
            this.codice = codice;
        }

        public int getCodice() {
            return codice;
        }
    }
}