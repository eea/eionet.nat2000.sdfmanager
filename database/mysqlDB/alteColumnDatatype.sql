alter table `natura2000`.`site` change SITE_SPA_LEGAL_REF SITE_SPA_LEGAL_REF LONGTEXT;
alter table `natura2000`.`site` change SITE_SAC_LEGAL_REF SITE_SAC_LEGAL_REF LONGTEXT;
alter table `natura2000`.`site` change SITE_EXPLANATIONS SITE_EXPLANATIONS LONGTEXT;
update natura2000.country set country_code ='UK' where country_code = 'GB';
commit;