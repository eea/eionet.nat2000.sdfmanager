DELETE FROM ref_species WHERE ref_species_code = '2284';
UPDATE ref_species SET ref_species_code_new = 0 WHERE ref_species_code_new IS NULL;