UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of adults', UNIT_CODE = 'adults' WHERE UNIT_ID = 2;
UPDATE natura2000.ref_unit SET UNIT_NAME = 'area coverd by population in m2', UNIT_CODE = 'area' WHERE UNIT_ID = 17;
UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of breeding females', UNIT_CODE = 'bfemales' WHERE UNIT_ID = 5;
UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of calling males', UNIT_CODE = 'cmales' WHERE UNIT_ID = 6;
UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of colonies', UNIT_CODE = 'colonies' WHERE UNIT_ID = 12;
UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of map 10x10 km grid cells', UNIT_CODE = 'grids10x10' WHERE UNIT_ID = 20;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of map 1x1 km grid cells', UNIT_CODE = 'grids1x1' WHERE UNIT_ID = 18;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of map 5x5 km grid cells', UNIT_CODE = 'grids5x5' WHERE UNIT_ID = 19;


UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of individuals', UNIT_CODE = 'i' WHERE UNIT_ID = 1;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'length of inhabited feature in km', UNIT_CODE = 'length' WHERE UNIT_ID = 16;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of localities', UNIT_CODE = 'localities' WHERE UNIT_ID = 11;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of inhabited logs', UNIT_CODE = 'logs' WHERE UNIT_ID = 13;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of males', UNIT_CODE = 'males' WHERE UNIT_ID = 7;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of pairs', UNIT_CODE = 'p' WHERE UNIT_ID = 8;
UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of shoots', UNIT_CODE = 'shoots' WHERE UNIT_ID = 9;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of inhabited stones/boulders', UNIT_CODE = 'stones' WHERE UNIT_ID = 15;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of subadults', UNIT_CODE = 'subadults' WHERE UNIT_ID = 3;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of inhabited trees', UNIT_CODE = 'trees' WHERE UNIT_ID = 14;

UPDATE natura2000.ref_unit SET UNIT_NAME = 'number of tufts', UNIT_CODE = 'tufts' WHERE UNIT_ID = 10;

INSERT ignore INTO natura2000.ref_unit VALUES(21,'number of flowering stems','fstems');
