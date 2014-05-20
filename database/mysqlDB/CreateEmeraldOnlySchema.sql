CREATE DATABASE IF NOT EXISTS emerald DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
commit;
GRANT ALL ON emerald.* TO 'sa'@'%';
GRANT SELECT, INSERT ON emerald.* TO 'sa'@'%';
commit;
