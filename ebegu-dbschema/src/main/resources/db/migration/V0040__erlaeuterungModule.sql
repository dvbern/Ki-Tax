alter table einstellungen_tagesschule add if not exists erlaeuterung varchar(4000);
alter table einstellungen_tagesschule_aud add if not exists erlaeuterung varchar(4000);

alter table modul_tagesschule_group_aud modify column bezeichnung_id binary(16);