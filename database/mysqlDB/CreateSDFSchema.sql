CREATE DATABASE IF NOT EXISTS natura2000 DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
commit;
GRANT ALL ON natura2000.* to 'sa'@'%' IDENTIFIED BY 'sa';
commit;
