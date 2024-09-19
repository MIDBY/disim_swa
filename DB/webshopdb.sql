-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Creato il: Set 08, 2024 alle 16:59
-- Versione del server: 10.4.32-MariaDB
-- Versione PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `webshopdb`
--

-- --------------------------------------------------------

--
-- Struttura della tabella `caratteristica`
--

CREATE TABLE `caratteristica` (
  `id` int(11) NOT NULL,
  `nome` varchar(32) NOT NULL,
  `idCategoria` int(11) NOT NULL,
  `valoriDefault` varchar(512) NOT NULL,
  `versione` tinyint(3) UNSIGNED NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `caratteristica`
--

INSERT INTO `caratteristica` (`id`, `nome`, `idCategoria`, `valoriDefault`, `versione`) VALUES
(15, 'Stile', 15, 'Classico,Vintage,Futuristico,RGB,Indifferent', 1),
(16, 'Dimensione', 16, '12\",20\",24\",32\",40\",Indifferent', 1),
(17, 'Memoria ROM', 17, '1268Gb,256Gb,512Gb,1Tb,2Tb,Indifferent', 2),
(18, 'Memoria RAM', 17, '8Gb,16Gb,32Gb,64Gb,Indifferent', 2),
(19, 'Larghezza', 18, '15cm,20cm,24cm,Indifferent', 1),
(20, 'Altezza', 18, '4cm,6cm,8cm,10cm,Indifferent', 1),
(21, 'Marca', 17, 'Intel,HP,AMD,MSI,Apple,Indifferent', 1),
(22, 'Schermo', 19, '14\",16\",18\",Indifferent', 3),
(23, 'Processore', 19, 'Ryzen 3,Ryzen 5,Ryzen 7,Intel I3,Intel I5,Intel I7,Intel I9,Indifferent', 3),
(25, 'Memoria ROM', 19, '1268Gb,256Gb,512Gb,1Tb,Indifferent', 1),
(26, 'Memoria RAM', 19, '4Gb,6Gb,8Gb,16Gb,Indifferent', 1),
(27, 'Temperatura minima', 20, '5°C,-10°C,-20°C,Indifferent', 1),
(28, 'Altezza', 21, '50cm,40cm,32cm,Indifferent', 1),
(29, 'Schermo LCD', 22, '7\",10\",12\",Indifferent', 2),
(30, 'Dispenser', 22, 'Liquidi,Ghiaggio,Baguettes,Indifferent', 2),
(31, 'Ripiani congelatore', 23, '2 ripiani,3 ripiani,4 ripiani,Indifferent', 1);

-- --------------------------------------------------------

--
-- Struttura della tabella `categoria`
--

CREATE TABLE `categoria` (
  `id` int(11) NOT NULL,
  `nome` varchar(64) NOT NULL,
  `idCategoriaPadre` int(11) DEFAULT NULL,
  `idImmagine` int(11) NOT NULL,
  `eliminato` tinyint(1) NOT NULL DEFAULT 0,
  `versione` tinyint(3) UNSIGNED NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `categoria`
--

INSERT INTO `categoria` (`id`, `nome`, `idCategoriaPadre`, `idImmagine`, `eliminato`, `versione`) VALUES
(15, 'Computer', NULL, 14, 0, 1),
(16, 'Monitor', 15, 15, 0, 1),
(17, 'Desktop PC', 15, 16, 0, 2),
(18, 'Mini PC', 17, 17, 0, 1),
(19, 'Portatile', 15, 18, 0, 3),
(20, 'Frigorifero', NULL, 19, 0, 1),
(21, 'Mini Frigo', 20, 20, 0, 1),
(22, 'Frigorifero Smart', 23, 21, 0, 2),
(23, 'Frigorifero con congelatore', 20, 22, 0, 1);

-- --------------------------------------------------------

--
-- Struttura della tabella `gruppo`
--

CREATE TABLE `gruppo` (
  `id` int(11) NOT NULL,
  `nome` varchar(16) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `gruppo`
--

INSERT INTO `gruppo` (`id`, `nome`) VALUES
(1, 'AMMINISTRATORE'),
(2, 'ORDINANTE'),
(3, 'TECNICO');

-- --------------------------------------------------------

--
-- Struttura della tabella `gruppo_servizio`
--

CREATE TABLE `gruppo_servizio` (
  `idGruppo` int(11) NOT NULL,
  `idServizio` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `gruppo_servizio`
--

INSERT INTO `gruppo_servizio` (`idGruppo`, `idServizio`) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 8),
(1, 9),
(1, 10),
(1, 11),
(2, 12),
(2, 13),
(2, 14),
(2, 15),
(2, 16),
(3, 1),
(3, 2),
(3, 6),
(3, 7),
(3, 11);

-- --------------------------------------------------------

--
-- Struttura della tabella `immagine`
--

CREATE TABLE `immagine` (
  `id` int(11) NOT NULL,
  `titolo` varchar(256) NOT NULL,
  `tipo` varchar(32) NOT NULL,
  `nomeFile` varchar(256) NOT NULL,
  `grandezza` int(11) NOT NULL,
  `versione` tinyint(3) UNSIGNED NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `immagine`
--

INSERT INTO `immagine` (`id`, `titolo`, `tipo`, `nomeFile`, `grandezza`, `versione`) VALUES
(14, 'Computer image', 'image/jpeg', 'computer.jpg', 9407, 1),
(15, 'Monitor image', 'image/jpeg', 'monitor.jpg', 7433, 1),
(16, 'Desktop PC image', 'image/jpeg', 'desktop.jpg', 7499, 1),
(17, 'Mini PC image', 'image/jpeg', 'minipc.jpg', 3924, 1),
(18, 'Portatile image', 'image/jpeg', 'portatitle.jpg', 5799, 1),
(19, 'Frigorifero image', 'image/jpeg', 'frigorifero.jpg', 2591, 1),
(20, 'Mini Frigo image', 'image/jpeg', 'minifrigo.jpg', 5675, 1),
(21, 'Frigorifero Smart image', 'image/jpeg', 'smartfrigo.jpg', 3298, 1),
(22, 'Frigorifero con congelatore image', 'image/jpeg', 'frigoriferroecongelatore.jpg', 4880, 1);

-- --------------------------------------------------------

--
-- Struttura della tabella `notifica`
--

CREATE TABLE `notifica` (
  `id` int(11) NOT NULL,
  `idDestinatario` int(11) NOT NULL,
  `messaggio` varchar(512) NOT NULL,
  `link` varchar(128) NOT NULL,
  `tipo` enum('INFO','NUOVO','MODIFICATO','CHIUSO','ANNULLATO') NOT NULL,
  `dataCreazione` datetime NOT NULL DEFAULT current_timestamp(),
  `letto` tinyint(1) NOT NULL DEFAULT 0,
  `versione` tinyint(3) UNSIGNED NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `notifica`
--

INSERT INTO `notifica` (`id`, `idDestinatario`, `messaggio`, `link`, `tipo`, `dataCreazione`, `letto`, `versione`) VALUES
(54, 32, 'Welcome in Webshop, new client!', 'index', 'INFO', '2024-09-08 15:50:59', 0, 1),
(55, 33, 'Welcome in Webshop, new client!', 'index', 'INFO', '2024-09-08 16:23:28', 0, 1),
(56, 33, 'Welcome in Webshop, new technician!', 'profile', 'INFO', '2024-09-08 16:23:31', 0, 1),
(57, 32, 'Great news! Your request Request#20240908162124 has been taken in charge by one of our operators!', 'index', 'INFO', '2024-09-08 16:33:34', 0, 1),
(58, 32, 'Request: Request#20240908162124.\n Our technician has sent a new proposal to you, go to check it!', 'requestDetail?reqid=14', 'NUOVO', '2024-09-08 16:35:50', 0, 1),
(59, 32, 'Request: Request#20240908162124.\n Our technician has edited proposal, go to check it!', 'requestDetail?reqid=14', 'MODIFICATO', '2024-09-08 16:36:21', 0, 1),
(60, 33, 'Topolino responded to your proposal!', 'orderDetail?order=14', 'MODIFICATO', '2024-09-08 16:57:18', 0, 1);

-- --------------------------------------------------------

--
-- Struttura della tabella `proposta`
--

CREATE TABLE `proposta` (
  `id` int(11) NOT NULL,
  `idRichiesta` int(11) NOT NULL,
  `idTecnico` int(11) NOT NULL,
  `nomeProdotto` varchar(64) NOT NULL,
  `nomeProduttore` varchar(64) NOT NULL,
  `descrizioneProdotto` varchar(1024) NOT NULL,
  `prezzoProdotto` float NOT NULL,
  `url` varchar(512) DEFAULT NULL,
  `note` varchar(1024) DEFAULT NULL,
  `dataCreazione` datetime NOT NULL DEFAULT current_timestamp(),
  `statoProposta` enum('INATTESA','APPROVATO','RESPINTO') NOT NULL DEFAULT 'INATTESA',
  `motivazione` varchar(1024) NOT NULL,
  `versione` tinyint(3) UNSIGNED NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `proposta`
--

INSERT INTO `proposta` (`id`, `idRichiesta`, `idTecnico`, `nomeProdotto`, `nomeProduttore`, `descrizioneProdotto`, `prezzoProdotto`, `url`, `note`, `dataCreazione`, `statoProposta`, `motivazione`, `versione`) VALUES
(9, 14, 33, 'MSI Titan 18 HX A14VIG-231IT', 'MSI', 'MSI Titan 18 HX A14VIG-231IT Intel i9-14900HX 18\" 120Hz UHD+ Mini LED RTX 4090 + Penna USB 256GB + Mouse + Portachiavi Originale MSI', 6540.2, 'https://www.ollo.it/msi-titan-18-hx-a14vig-231it-intel-i9-14900hx-18-120hz-uhd-mini-led-rtx-4090/p_904580?utm_source=google&utm_medium=cpc&utm_campaign=%F0%9F%92%B8+%5BPM%5D+MSI+-+Notebook+%F0%9F%92%BB+%5Bit_IT%5D&sorgente=shopping&gad_source=1&gclid=Cj0KCQjwlvW2BhDyARIsADnIe-JazYeRfZeILNeTYPhL_iYGY_pKUKyq65co60N1VMgzbETHakLT8PIaAmkXEALw_wcB', 'Lo stencil dei Minions richiesto è possibile applicarlo in sede aprendo la scatola', '2024-09-08 16:35:50', 'APPROVATO', 'APPROVATO', 3);

-- --------------------------------------------------------

--
-- Struttura della tabella `richiesta`
--

CREATE TABLE `richiesta` (
  `id` int(11) NOT NULL,
  `titolo` varchar(64) NOT NULL,
  `descrizione` varchar(512) NOT NULL,
  `idCategoria` int(11) NOT NULL,
  `idOrdinante` int(11) NOT NULL,
  `idTecnico` int(11) DEFAULT NULL,
  `statoRichiesta` enum('NUOVO','PRESOINCARICO','ORDINATO','CHIUSO','ANNULLATO') NOT NULL DEFAULT 'NUOVO',
  `statoOrdine` enum('SPEDITO','ACCETTATO','RESPINTONONCONFORME','RESPINTONONFUNZIONANTE','EMPTY') NOT NULL,
  `dataCreazione` date NOT NULL,
  `note` varchar(1024) DEFAULT NULL,
  `versione` tinyint(3) UNSIGNED NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `richiesta`
--

INSERT INTO `richiesta` (`id`, `titolo`, `descrizione`, `idCategoria`, `idOrdinante`, `idTecnico`, `statoRichiesta`, `statoOrdine`, `dataCreazione`, `note`, `versione`) VALUES
(14, 'Request#20240908162124', 'Cerco un pc portatile con scheda grafica Nvidia 4080Ti che mi può durare 20 anni', 19, 32, 33, 'ORDINATO', 'EMPTY', '2024-09-08', 'Se possibile vorrei che ci metteste un stencil dei Minions', 4),
(15, 'Request#20240908165846', 'Cerco Frigo con alexa integrata', 22, 32, NULL, 'NUOVO', 'EMPTY', '2024-09-08', '', 1);

-- --------------------------------------------------------

--
-- Struttura della tabella `richiesta_caratteristica`
--

CREATE TABLE `richiesta_caratteristica` (
  `id` int(11) NOT NULL,
  `idRichiesta` int(11) NOT NULL,
  `idCaratteristica` int(11) NOT NULL,
  `valore` varchar(256) NOT NULL,
  `versione` tinyint(3) UNSIGNED NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `richiesta_caratteristica`
--

INSERT INTO `richiesta_caratteristica` (`id`, `idRichiesta`, `idCaratteristica`, `valore`, `versione`) VALUES
(14, 14, 22, '18\"', 2),
(15, 14, 23, 'Intel I9', 2),
(16, 14, 25, '1Tb', 2),
(17, 14, 26, 'Indifferent', 2),
(18, 14, 15, 'RGB', 2),
(19, 15, 29, '12\"', 1),
(20, 15, 30, 'Baguettes', 1),
(21, 15, 31, '3 ripiani', 1),
(22, 15, 27, '-10°C', 1);

-- --------------------------------------------------------

--
-- Struttura della tabella `servizio`
--

CREATE TABLE `servizio` (
  `id` int(11) NOT NULL,
  `script` varchar(32) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `servizio`
--

INSERT INTO `servizio` (`id`, `script`) VALUES
(1, 'homepage'),
(2, 'profile'),
(3, 'staff'),
(4, 'clients'),
(5, 'requests'),
(6, 'orders'),
(7, 'orderDetail'),
(8, 'files'),
(9, 'categories'),
(10, 'categoryDetail'),
(11, 'notifications'),
(12, 'index'),
(13, 'newRequest'),
(14, 'requestDetail'),
(15, 'myProfile'),
(16, 'myNotifications');

-- --------------------------------------------------------

--
-- Struttura della tabella `utente`
--

CREATE TABLE `utente` (
  `id` int(11) NOT NULL,
  `username` varchar(128) DEFAULT NULL,
  `email` varchar(128) NOT NULL,
  `password` varchar(256) NOT NULL,
  `indirizzo` varchar(256) NOT NULL,
  `dataIscrizione` date NOT NULL DEFAULT current_timestamp(),
  `accettato` tinyint(1) NOT NULL DEFAULT 0,
  `versione` tinyint(3) UNSIGNED NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `utente`
--

INSERT INTO `utente` (`id`, `username`, `email`, `password`, `indirizzo`, `dataIscrizione`, `accettato`, `versione`) VALUES
(1, 'Marko', 'durbanomk@gmail.com', '75db5cb1c338dfc441a2c470eb457bd10413c824ab6cf4d3282002f63b3e7b40fd200ad2b53cfff5abb978f2c56942b1', 'Via pazzi, 2,  Tokyo,  123, Italy(IT)', '2024-08-06', 0, 16),
(32, 'Topolino', 'topolino@gmail.com', '5f07ba8b9fd73573825f8712fc21d80fa70fd2420f718af003b9cf210222fa9447c70020cc67b139ab745c928a9714ab', 'Via Squit, 15, Bologna, 12542, Italy(IT)', '2024-09-08', 1, 4),
(33, 'Pippo', 'pippo@gmail.com', 'f4f39548c180080ca84b2dfa60940ad230c64334bc7c3064316830bd4de96b4358eeef846139e29f6f1af2b80dfbcb1b', 'Via da Cane, 1, Barisciano, 24582, Spain(ES)', '2024-09-08', 1, 2);

-- --------------------------------------------------------

--
-- Struttura della tabella `utente_gruppo`
--

CREATE TABLE `utente_gruppo` (
  `idUtente` int(11) NOT NULL,
  `idGruppo` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dump dei dati per la tabella `utente_gruppo`
--

INSERT INTO `utente_gruppo` (`idUtente`, `idGruppo`) VALUES
(1, 1),
(32, 2),
(33, 3);

--
-- Indici per le tabelle scaricate
--

--
-- Indici per le tabelle `caratteristica`
--
ALTER TABLE `caratteristica`
  ADD PRIMARY KEY (`id`),
  ADD KEY `categoria` (`idCategoria`) USING BTREE,
  ADD KEY `nome` (`nome`) USING BTREE;

--
-- Indici per le tabelle `categoria`
--
ALTER TABLE `categoria`
  ADD PRIMARY KEY (`id`),
  ADD KEY `categoria_FK2` (`idImmagine`),
  ADD KEY `categoria_FK1` (`idCategoriaPadre`);

--
-- Indici per le tabelle `gruppo`
--
ALTER TABLE `gruppo`
  ADD PRIMARY KEY (`id`);

--
-- Indici per le tabelle `gruppo_servizio`
--
ALTER TABLE `gruppo_servizio`
  ADD UNIQUE KEY `idGruppo` (`idGruppo`,`idServizio`),
  ADD KEY `idServizio` (`idServizio`);

--
-- Indici per le tabelle `immagine`
--
ALTER TABLE `immagine`
  ADD PRIMARY KEY (`id`);

--
-- Indici per le tabelle `notifica`
--
ALTER TABLE `notifica`
  ADD PRIMARY KEY (`id`),
  ADD KEY `notifica_FK1` (`idDestinatario`);

--
-- Indici per le tabelle `proposta`
--
ALTER TABLE `proposta`
  ADD PRIMARY KEY (`id`),
  ADD KEY `proposta_FK2` (`idTecnico`),
  ADD KEY `id_richiesta` (`idRichiesta`,`idTecnico`);

--
-- Indici per le tabelle `richiesta`
--
ALTER TABLE `richiesta`
  ADD PRIMARY KEY (`id`),
  ADD KEY `richiesta_FK1` (`idCategoria`),
  ADD KEY `richiesta_FK2` (`idOrdinante`),
  ADD KEY `richiesta_FK3` (`idTecnico`);

--
-- Indici per le tabelle `richiesta_caratteristica`
--
ALTER TABLE `richiesta_caratteristica`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `id_richiesta` (`idRichiesta`,`idCaratteristica`),
  ADD KEY `richiesta_caratteristica_FK2` (`idCaratteristica`),
  ADD KEY `id_richiesta_2` (`idRichiesta`);

--
-- Indici per le tabelle `servizio`
--
ALTER TABLE `servizio`
  ADD PRIMARY KEY (`id`);

--
-- Indici per le tabelle `utente`
--
ALTER TABLE `utente`
  ADD PRIMARY KEY (`id`);

--
-- Indici per le tabelle `utente_gruppo`
--
ALTER TABLE `utente_gruppo`
  ADD UNIQUE KEY `idUtente` (`idUtente`,`idGruppo`),
  ADD KEY `utente_gruppo_FK2` (`idGruppo`),
  ADD KEY `utente_gruppo_FK1` (`idUtente`);

--
-- AUTO_INCREMENT per le tabelle scaricate
--

--
-- AUTO_INCREMENT per la tabella `caratteristica`
--
ALTER TABLE `caratteristica`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT per la tabella `categoria`
--
ALTER TABLE `categoria`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT per la tabella `gruppo`
--
ALTER TABLE `gruppo`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT per la tabella `immagine`
--
ALTER TABLE `immagine`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT per la tabella `notifica`
--
ALTER TABLE `notifica`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=61;

--
-- AUTO_INCREMENT per la tabella `proposta`
--
ALTER TABLE `proposta`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT per la tabella `richiesta`
--
ALTER TABLE `richiesta`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT per la tabella `richiesta_caratteristica`
--
ALTER TABLE `richiesta_caratteristica`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT per la tabella `servizio`
--
ALTER TABLE `servizio`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT per la tabella `utente`
--
ALTER TABLE `utente`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- Limiti per le tabelle scaricate
--

--
-- Limiti per la tabella `caratteristica`
--
ALTER TABLE `caratteristica`
  ADD CONSTRAINT `caratteristica_FK1` FOREIGN KEY (`idCategoria`) REFERENCES `categoria` (`id`) ON DELETE CASCADE;

--
-- Limiti per la tabella `categoria`
--
ALTER TABLE `categoria`
  ADD CONSTRAINT `categoria_FK1` FOREIGN KEY (`idCategoriaPadre`) REFERENCES `categoria` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `categoria_FK2` FOREIGN KEY (`idImmagine`) REFERENCES `immagine` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limiti per la tabella `gruppo_servizio`
--
ALTER TABLE `gruppo_servizio`
  ADD CONSTRAINT `gruppo_servizio_FK1` FOREIGN KEY (`idGruppo`) REFERENCES `gruppo` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `gruppo_servizio_FK2` FOREIGN KEY (`idServizio`) REFERENCES `servizio` (`id`);

--
-- Limiti per la tabella `notifica`
--
ALTER TABLE `notifica`
  ADD CONSTRAINT `notifica_FK1` FOREIGN KEY (`idDestinatario`) REFERENCES `utente` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limiti per la tabella `proposta`
--
ALTER TABLE `proposta`
  ADD CONSTRAINT `proposta_FK1` FOREIGN KEY (`idRichiesta`) REFERENCES `richiesta` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `proposta_FK2` FOREIGN KEY (`idTecnico`) REFERENCES `utente` (`id`) ON UPDATE CASCADE;

--
-- Limiti per la tabella `richiesta`
--
ALTER TABLE `richiesta`
  ADD CONSTRAINT `richiesta_FK1` FOREIGN KEY (`idCategoria`) REFERENCES `categoria` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `richiesta_FK2` FOREIGN KEY (`idOrdinante`) REFERENCES `utente` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `richiesta_FK3` FOREIGN KEY (`idTecnico`) REFERENCES `utente` (`id`) ON UPDATE CASCADE;

--
-- Limiti per la tabella `richiesta_caratteristica`
--
ALTER TABLE `richiesta_caratteristica`
  ADD CONSTRAINT `richiesta_caratteristica_FK1` FOREIGN KEY (`idRichiesta`) REFERENCES `richiesta` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `richiesta_caratteristica_FK2` FOREIGN KEY (`idCaratteristica`) REFERENCES `caratteristica` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Limiti per la tabella `utente_gruppo`
--
ALTER TABLE `utente_gruppo`
  ADD CONSTRAINT `utente_gruppo_FK1` FOREIGN KEY (`idUtente`) REFERENCES `utente` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `utente_gruppo_FK2` FOREIGN KEY (`idGruppo`) REFERENCES `gruppo` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
