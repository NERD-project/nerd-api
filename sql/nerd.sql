SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `nerd` DEFAULT CHARACTER SET utf8 ;
USE `nerd` ;

-- -----------------------------------------------------
-- Table `nerd`.`document`
-- type: plaintext,webtext,timedtext
-- -----------------------------------------------------
DROP TABLE IF EXISTS `nerd`.`document` ;

CREATE  TABLE IF NOT EXISTS `nerd`.`document` (
  `idDocument` INT(11) NOT NULL AUTO_INCREMENT ,
  `textHash` CHAR(40) NOT NULL ,
  `text` MEDIUMTEXT NULL DEFAULT NULL ,
  `timedtext` MEDIUMTEXT NULL DEFAULT NULL ,
  `URI` MEDIUMTEXT NULL DEFAULT NULL ,
  `type` MEDIUMTEXT NULL DEFAULT NULL ,
  `language` MEDIUMTEXT NULL DEFAULT NULL ,
  `source` MEDIUMTEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`idDocument`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `nerd`.`tool`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `nerd`.`tool` ;

CREATE  TABLE IF NOT EXISTS `nerd`.`tool` (
  `idTool` INT(11) NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NULL DEFAULT NULL ,
  `uri` MEDIUMTEXT NULL DEFAULT NULL ,
  PRIMARY KEY (`idTool`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


INSERT INTO `tool`(name,uri) VALUES 
('nerd','http://nerd.eurecom.fr'),
('alchemyapi','http://www.alchemyapi.com'),
('dbspotlight','http://dbpedia.org/spotlight'),
('evri','http://www.evri.com/developer/rest'),
('extractiv','http://extractiv.com'),
('opencalais','http://www.opencalais.com'),
('lupedia','http://lupedia.ontotext.com'),
('saplo','http://www.saplo.com'),
('semitags', 'http://nlp2.vse.cz/nerDe/rest/v1/recognize'),
('wikimeta','http://www.wikimeta.org'),
('yahoo','http://developer.yahoo.com/search/content/V2/contentAnalysis.html'),
('zemanta','http://www.zemanta.com'),
('thd','https://ner.vse.cz/thd/'),
('textrazor','http://api.textrazor.com'),
('combined','http://nerd.eurecom.fr');


-- -----------------------------------------------------
-- Table `nerd`.`user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `nerd`.`user` ;

CREATE  TABLE IF NOT EXISTS `nerd`.`user` (
  `idUser` INT(11) NOT NULL AUTO_INCREMENT,
  `firstName` VARCHAR(255),
  `lastName` VARCHAR(255),
  `nickName` VARCHAR(255),
  `openidEmail` VARCHAR(255),
  `email` VARCHAR(255),
  `website` VARCHAR(255),  
  `projectName` VARCHAR(255),
  `projectUri` VARCHAR(255),
  `language` VARCHAR(255),
  `organization` VARCHAR(255),
  `country` VARCHAR(255),
  `registrationDate` DATETIME NULL DEFAULT NULL,
  `registrationKeyDate` DATETIME NULL DEFAULT NULL,
  `validity` TINYINT NOT NULL DEFAULT 1,
  `openid` VARCHAR(255) NOT NULL,
  `tokenHash` CHAR(40) NOT NULL,
  `tokenTimeStamp` DATETIME NOT NULL,
  `dailyUsage` INT(10) NOT NULL DEFAULT 0,
  `dailyQuota` INT(10) NOT NULL DEFAULT 500,
  `totalUsage` INT(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`idUser`),
  UNIQUE KEY (`nickName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `nerd`.`usage`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `nerd`.`service` ;

CREATE  TABLE IF NOT EXISTS `nerd`.`service` (
  `idService` INT(11) NOT NULL AUTO_INCREMENT,
  `apikey` MEDIUMTEXT NULL DEFAULT NULL ,
  `userIdUser` INT(11) NOT NULL ,
  `toolIdTool` INT(11) NOT NULL ,
  PRIMARY KEY (`idService`),
  INDEX `fk_User` (`userIdUser` ASC) ,
  INDEX `fk_Tool` (`toolIdTool` ASC) ,
  CONSTRAINT `fk_Tool`
    FOREIGN KEY (`toolIdTool` )
    REFERENCES `nerd`.`tool` (`idTool` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_User`
    FOREIGN KEY (`userIdUser` )
    REFERENCES `nerd`.`user` (`idUser` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `nerd`.`annotation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `nerd`.`annotation` ;

CREATE  TABLE IF NOT EXISTS `nerd`.`annotation` (
  `idAnnotation` INT(11) NOT NULL AUTO_INCREMENT ,
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  `ontology` VARCHAR(15) NULL DEFAULT NULL,
  `documentIdDocument` INT(11) NULL DEFAULT NULL ,
  `toolIdTool` INT(11) NULL DEFAULT NULL ,
  `userIdUser` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`idAnnotation`) ,
  INDEX `fk_Annotation_Document` (`documentIdDocument` ASC) ,
  INDEX `fk_Annotation_User` (`userIdUser` ASC) ,
  INDEX `fk_Annotation_Tool` (`toolIdTool` ASC) ,
  CONSTRAINT `fk_Annotation_Document`
    FOREIGN KEY (`documentIdDocument` )
    REFERENCES `nerd`.`document` (`idDocument`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Annotation_Tool`
    FOREIGN KEY (`toolIdTool` )
    REFERENCES `nerd`.`tool` (`idTool` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Annotation_User`
    FOREIGN KEY (`userIdUser` )
    REFERENCES `nerd`.`user` (`idUser` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `nerd`.`entity`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `nerd`.`entity` ;

CREATE  TABLE IF NOT EXISTS `nerd`.`entity` (
  `idEntity` INT(11) NOT NULL AUTO_INCREMENT ,
  `label` MEDIUMTEXT NULL DEFAULT NULL ,
  `extractorType` MEDIUMTEXT NULL DEFAULT NULL ,
  `uri` MEDIUMTEXT NULL DEFAULT NULL ,
  `nerdType` VARCHAR(100) NULL DEFAULT NULL ,
  `startChar` INT(11) NULL DEFAULT NULL ,
  `endChar` INT(11) NULL DEFAULT NULL ,
  `startNPT` FLOAT NULL DEFAULT NULL ,
  `endNPT` FLOAT NULL DEFAULT NULL ,
  `confidence` FLOAT NULL DEFAULT NULL ,
  `relevance` FLOAT NULL DEFAULT NULL ,
  `extractor` VARCHAR(45) NULL DEFAULT NULL ,
  `annotationIdAnnotation` INT(11) NOT NULL ,
  PRIMARY KEY (`idEntity`, `annotationIdAnnotation`) ,
  INDEX `fk_entity_annotation` (`annotationIdAnnotation` ASC) ,
  CONSTRAINT `fk_entity_annotation`
    FOREIGN KEY (`annotationIdAnnotation` )
    REFERENCES `nerd`.`annotation` (`idAnnotation` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;
CREATE INDEX nerdType USING BTREE ON entity(nerdType);
CREATE INDEX confidence USING BTREE ON entity(confidence); 

-- -----------------------------------------------------
-- Table `nerd`.`evaluation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `nerd`.`evaluation` ;

CREATE  TABLE IF NOT EXISTS `nerd`.`evaluation` (
  `idEvaluation` INT NOT NULL AUTO_INCREMENT ,
  `validityNE` TINYINT NULL ,
  `validityType` TINYINT NULL ,
  `validityURI` TINYINT NULL ,
  `relevant` TINYINT NULL ,
  `validityNERDType` TINYINT NULL ,
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ,
  `entityidEntity` INT(11) NOT NULL ,
  PRIMARY KEY (`idEvaluation`, `entityidEntity`) ,
  INDEX `fk_evaluation_entity` (`entityidEntity` ASC) ,
  CONSTRAINT `fk_evaluation_entity`
    FOREIGN KEY (`entityidEntity` )
    REFERENCES `nerd`.`entity` (`idEntity` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;


-- ----------------------------------------------------
-- Enable the scheduling daemon
-- ----------------------------------------------------
-- @root
-- grant all privileges on nerd.* to nerduser 
-- http://dev.mysql.com/doc/refman/5.5/en/events-configuration.html
-- SET @@global.event_scheduler = ON;
-- show processlist (cronjobs scheduled)
-- show events

-- -----------------------------------------------------
-- Event cleaning
-- -----------------------------------------------------
DROP EVENT IF EXISTS reset_counter_user;

-- -----------------------------------------------------
-- Event declaration
-- -----------------------------------------------------
DELIMITER $$  
CREATE  
    EVENT `reset_counter_user`  
    ON SCHEDULE EVERY 1 DAY  
    DO BEGIN  
        update nerd.user set dailyUsage=0;
	END $$  
DELIMITER ;   
