CREATE DATABASE IF NOT EXISTS emerald DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
commit;
CREATE USER 'sa'@'%' IDENTIFIED BY 'sa';
commit;
GRANT ALL ON emerald.* TO 'sa'@'%';
GRANT ALL ON emerald.* TO 'root'@'%';
GRANT SELECT, INSERT ON emerald.* TO 'sa'@'%';
GRANT SELECT, INSERT ON emerald.* TO 'root'@'%';
commit;
