CREATE TABLE IF NOT EXISTS `ref_population_type` (`POPULATION_ID` int(11) NOT NULL,`POPULATION_NAME` varchar(256) CHARACTER SET utf8 DEFAULT NULL,`POPULATION_CODE` varchar(1) DEFAULT NULL, PRIMARY KEY (`POPULATION_ID`));
CREATE TABLE IF NOT EXISTS `ref_unit` (`UNIT_ID` int(11) NOT NULL,`UNIT_NAME` varchar(256) CHARACTER SET utf8 DEFAULT NULL,`UNIT_CODE` varchar(124) DEFAULT NULL, PRIMARY KEY (`UNIT_ID`));
CREATE TABLE IF NOT EXISTS `ref_category` (`CATEGORY_ID` int(11) NOT NULL,`CATEGORY_NAME` varchar(256) CHARACTER SET utf8 DEFAULT NULL,`CATEGORY_CODE` varchar(1) DEFAULT NULL,`CATEGORY_SPECIES` varchar(1) DEFAULT NULL, PRIMARY KEY (`CATEGORY_ID`));
CREATE TABLE IF NOT EXISTS `ref_species_group` (`GROUP_ID` int(11) NOT NULL,`GROUP_NAME` varchar(256) CHARACTER SET utf8 DEFAULT NULL,`GROUP_CODE` varchar(2) DEFAULT NULL,`GROUP_SPECIES` varchar(1) DEFAULT NULL, PRIMARY KEY (`GROUP_ID`));
CREATE TABLE IF NOT EXISTS `ref_data_quality` (`QUALITY_ID` int(11) NOT NULL,`QUALITY_NAME` varchar(256) CHARACTER SET utf8 DEFAULT NULL,`QUALITY_CODE` varchar(2) DEFAULT NULL,`QUALITY_SPECIES` varchar(1) DEFAULT NULL, PRIMARY KEY (`QUALITY_ID`));
CREATE TABLE IF NOT EXISTS `ref_impact_rank` (`RANK_ID` int(11) NOT NULL,`RANK_NAME` varchar(256) CHARACTER SET utf8 DEFAULT NULL,`RANK_CODE` varchar(1) DEFAULT NULL, PRIMARY KEY (`RANK_ID`));
CREATE TABLE IF NOT EXISTS `ref_impact_pollution` (`POLLUTION_ID` int(11) NOT NULL,`POLLUTION_NAME` varchar(256) CHARACTER SET utf8 DEFAULT NULL,`POLLUTION_CODE` varchar(1) DEFAULT NULL, PRIMARY KEY (`POLLUTION_ID`));
