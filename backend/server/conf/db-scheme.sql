-- --------------------------------------------------------
-- Hostitel:                     127.0.0.1
-- Verze serveru:                5.6.20 - MySQL Community Server (GPL)
-- OS serveru:                   Win32
-- HeidiSQL Verze:               9.3.0.4984
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Exportování struktury databáze pro
DROP DATABASE IF EXISTS `pidifrky`;
CREATE DATABASE IF NOT EXISTS `pidifrky` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_czech_ci */;
USE `pidifrky`;


-- Exportování struktury pro tabulka pidifrky.cards
DROP TABLE IF EXISTS `cards`;
CREATE TABLE IF NOT EXISTS `cards` (
  `id` int(11) unsigned NOT NULL,
  `number` int(11) unsigned NOT NULL DEFAULT '0',
  `name` varchar(150) COLLATE utf8_czech_ci NOT NULL DEFAULT '0',
  `latitude` float DEFAULT NULL,
  `longitude` float DEFAULT NULL,
  `neighbours` varchar(200) COLLATE utf8_czech_ci NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `number` (`number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

-- Export dat nebyl vybrán.


-- Exportování struktury pro tabulka pidifrky.cards_x_merchants
DROP TABLE IF EXISTS `cards_x_merchants`;
CREATE TABLE IF NOT EXISTS `cards_x_merchants` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `card_id` int(11) unsigned NOT NULL,
  `merchant_id` int(11) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_cards_x_merchants_cards` (`card_id`),
  KEY `FK_cards_x_merchants_merchants` (`merchant_id`),
  CONSTRAINT `FK_cards_x_merchants_cards` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `FK_cards_x_merchants_merchants` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

-- Export dat nebyl vybrán.


-- Exportování struktury pro tabulka pidifrky.hashes
DROP TABLE IF EXISTS `hashes`;
CREATE TABLE IF NOT EXISTS `hashes` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `type` varchar(20) COLLATE utf8_czech_ci NOT NULL,
  `hash` double NOT NULL,
  `time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

-- Export dat nebyl vybrán.


-- Exportování struktury pro tabulka pidifrky.merchants
DROP TABLE IF EXISTS `merchants`;
CREATE TABLE IF NOT EXISTS `merchants` (
  `id` int(11) unsigned NOT NULL,
  `name` varchar(250) COLLATE utf8_czech_ci NOT NULL,
  `address` varchar(250) COLLATE utf8_czech_ci NOT NULL,
  `latitude` float DEFAULT NULL,
  `longitude` float DEFAULT NULL,
  `gps` tinyint(1) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci;

-- Export dat nebyl vybrán.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
