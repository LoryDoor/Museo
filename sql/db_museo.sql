-- FILE: sql/db_museo.sql
-- DESCRIZIONE: Script di creazione del DB e delle tabelle per l'applicazione Museo
-- AUTORE: Lorenzo Porta
-- DATA: 30/03/2026

CREATE DATABASE IF NOT EXISTS db_museo;

use db_museo;

-- Tabella Aree
DROP TABLE IF EXISTS museo_aree;
CREATE TABLE IF NOT EXISTS museo_aree (
    ID_Area integer(3) PRIMARY KEY AUTO_INCREMENT,
    Colore varchar(30) NOT NULL,
    Prezzo decimal(4,2) NOT NULL
);

INSERT INTO museo_aree(Colore, Prezzo) VALUES
    ('Rossa', 12.00),
    ('Verde', 5.00);

-- Tabella visite
CREATE TABLE IF NOT EXISTS museo_visite (
    ID_Visita integer(5) PRIMARY KEY AUTO_INCREMENT,
    Nominativo varchar(100) NOT NULL,
    Data date NOT NULL,
    NumeroPartecipanti integer(3),
    LEG_ID_Area integer(3) NOT NULL,

    FOREIGN KEY(LEG_ID_Area) REFERENCES museo_aree(ID_Area)
        ON DELETE CASCADE ON UPDATE CASCADE,

    -- Impedisce due visite per la stessa area nello stesso giorno
    CONSTRAINT unique_area_data UNIQUE (LEG_ID_Area, Data)
);
