CREATE TABLE IF NOT EXISTS `releasedbupdates` (`RELEASE_ID` int(4) NOT NULL,`RELEASE_NUMBER` varchar(4) NOT NULL,`UPDATE_DESCRIPTION` varchar(256) CHARACTER SET utf8 DEFAULT NULL,`UPDATE_DONE` varchar(1) DEFAULT NULL, PRIMARY KEY (`RELEASE_ID`));

alter table releasedbupdates modify column RELEASE_NUMBER varchar(8) not null;
