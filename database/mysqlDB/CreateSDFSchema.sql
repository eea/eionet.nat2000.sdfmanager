CREATE DATABASE IF NOT EXISTS natura2000 DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
commit;
CREATE USER 'sa'@'%' IDENTIFIED BY 'sa';
commit;
GRANT ALL ON natura2000.* TO 'sa'@'%';
GRANT ALL ON natura2000.* TO 'root'@'%';
GRANT SELECT, INSERT ON natura2000.* TO 'sa'@'%';
GRANT SELECT, INSERT ON natura2000.* TO 'root'@'%';
commit;
