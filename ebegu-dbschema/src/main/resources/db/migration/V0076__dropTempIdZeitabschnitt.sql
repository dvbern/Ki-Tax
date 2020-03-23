alter table bgcalculation_result drop temp_id_zeitabschnitt;
alter table bgcalculation_result_aud drop temp_id_zeitabschnitt;

alter table bgcalculation_result modify column abzug_fam_groesse decimal(19,2) not null;
alter table bgcalculation_result modify column fam_groesse decimal(19,2) not null;
alter table bgcalculation_result_aud modify column abzug_fam_groesse decimal(19,2) not null;
alter table bgcalculation_result_aud modify column fam_groesse decimal(19,2) not null;