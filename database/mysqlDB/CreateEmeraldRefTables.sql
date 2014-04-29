CREATE TABLE IF NOT EXISTS `natura2000`.`ref_nuts_emerald` (`REF_NUTS_ID` int(11) NOT NULL,`REF_NUTS_CODE` varchar(12) DEFAULT NULL,`REF_NUTS_DESCRIPTION` varchar(256) DEFAULT NULL,PRIMARY KEY (`REF_NUTS_ID`));
CREATE TABLE IF NOT EXISTS `natura2000`.`ref_designations_emerald` (`REF_DESIGNATIONS_ID` int(11) NOT NULL,`REF_DESIGNATIONS_CODE` varchar(16) DEFAULT NULL,`REF_DESIGNATIONS_DESCR` varchar(512) DEFAULT NULL,`REF_DESIGNATIONS_CATEGORY` char(1) DEFAULT NULL,PRIMARY KEY (`REF_DESIGNATIONS_ID`));
CREATE TABLE IF NOT EXISTS `natura2000`.`ref_hab_classes_emerald` (REF_HAB_CLASSES_ID INTEGER PRIMARY KEY,REF_HAB_CLASSES_CODE VARCHAR(3),REF_HAB_CLASSES_DESCR_EN VARCHAR(128),REF_HAB_CLASSES_DESCR_FR VARCHAR(128));
CREATE TABLE IF NOT EXISTS `natura2000`.`ref_habitats_emerald` (`REF_HABITATS_ID` int(11) NOT NULL,`REF_HABITATS_CODE` varchar(8) DEFAULT NULL,`REF_HABITATS_PRIORITY` smallint(6) DEFAULT NULL,`REF_HABITATS_DESC_EN` varchar(1024) DEFAULT NULL,`REF_HABITATS_DESC_FR` varchar(1024) DEFAULT NULL,`REF_HABITATS_DESC_DE` varchar(1024) DEFAULT NULL,`REF_HABITATS_DESC_ES` varchar(1024) DEFAULT NULL,`REF_HABITATS_DESC_IT` varchar(1024) DEFAULT NULL,`REF_HABITATS_DESC_NL` varchar(1024) DEFAULT NULL,`REF_HABITATS_DESC_PT` varchar(1024) DEFAULT NULL,`REF_HABITATS_DESC_DK` varchar(1024) DEFAULT NULL,PRIMARY KEY (`REF_HABITATS_ID`));