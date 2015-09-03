alter table `site` change SITE_SPA_LEGAL_REF SITE_SPA_LEGAL_REF LONGTEXT;
alter table `site` change SITE_SAC_LEGAL_REF SITE_SAC_LEGAL_REF LONGTEXT;
alter table `site` change SITE_EXPLANATIONS SITE_EXPLANATIONS LONGTEXT;
update country set country_code ='UK' where country_code = 'GB';
commit;