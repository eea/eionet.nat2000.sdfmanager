-- temporarily turn on auto increment to insert new rows
ALTER TABLE `ref_designations_emerald` CHANGE `REF_DESIGNATIONS_ID` `REF_DESIGNATIONS_ID` int(11) AUTO_INCREMENT NOT NULL;
-- new values
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO01', 'Nasjonalpark', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO02', 'Landskapsvernområde', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO03', 'Naturreservat', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO04', 'Biotopvern (naturmangfoldloven)', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO05', 'Marint verneområde (naturmangfoldloven)', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO11', 'Naturminne', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO13', 'Plantelivsfredning', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO15', 'Dyrelivsfredning', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO16', 'Botanisk/Zoologisk artsfredning', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO17', 'Dyrefredningsområde', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO19', 'Plantefredningsområde', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO21', 'Dyrefrednings-/Plantefredningsområde', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO22', 'Administrativt fredete områder eller trær', 'B');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO23', 'Biotopvern etter viltloven', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO31', 'Nasjonalpark (Svalbard)', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO33', 'Naturreservater (Svalbard)', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO34', 'Geotopvernområde (Svalbard)', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO39', 'Biotopvernområde (Svalbard)', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO43', 'Naturreservat (Antarktis)', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO53', 'Naturreservat (Jan Mayen)', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO90', 'Midlertidig vern', 'A');
INSERT INTO ref_designations_emerald (`REF_DESIGNATIONS_CODE`, `REF_DESIGNATIONS_DESCR`, `REF_DESIGNATIONS_CATEGORY`) VALUES ('NO99', 'Annet vern', 'A');
-- disable auto increment again
ALTER TABLE `ref_designations_emerald` CHANGE `REF_DESIGNATIONS_ID` `REF_DESIGNATIONS_ID` int(11) NOT NULL;