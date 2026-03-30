-- FILE: sql/db_museo.sql
-- DESCRIZIONE: Script di creazione del DB e delle tabelle per l'applicazione Museo
-- AUTORE: Lorenzo Porta
-- DATA: 30/03/2026

CREATE DATABASE IF NOT EXISTS db_museo;

use db_museo;

-- Tabella Aree
DROP TABLE IF EXISTS Aree;
CREATE TABLE IF NOT EXISTS Aree (
    ID_Area integer(3) PRIMARY KEY AUTO_INCREMENT,
    Colore varchar(30) NOT NULL
);

INSERT INTO Aree(Colore) VALUES
    ('Rosso'),
    ('Verde');

-- Tabella visite
CREATE TABLE IF NOT EXISTS Visite (
    ID_Visita integer(5) PRIMARY KEY AUTO_INCREMENT,
    Nominativo varchar(100) NOT NULL,
    Data date NOT NULL,
    NumeroPartecipanti integer(3),
    LEG_ID_Area integer(3),

    FOREIGN KEY(LEG_ID_Area) REFERENCES Aree(ID_Area) ON DELETE CASCADE ON UPDATE CASCADE
);
