alter table belegung_tagesschule add abholung_tagesschule varchar(255);
alter table belegung_tagesschule add abweichung_zweites_semester bit not null;
alter table belegung_tagesschule add bemerkung varchar(4000);
alter table belegung_tagesschule add plan_klasse varchar(255);

alter table belegung_tagesschule_aud add abholung_tagesschule varchar(255);
alter table belegung_tagesschule_aud add abweichung_zweites_semester bit;
alter table belegung_tagesschule_aud add bemerkung varchar(4000);
alter table belegung_tagesschule_aud add plan_klasse varchar(255);

alter table modul_tagesschule_group drop bezeichnung;
alter table modul_tagesschule_group add bezeichnung_id binary(16) not null;

alter table modul_tagesschule_group_aud drop bezeichnung;
alter table modul_tagesschule_group_aud add bezeichnung_id binary(16) not null;

alter table modul_tagesschule_group
	add constraint UK_bezeichnung_id unique (bezeichnung_id);

alter table modul_tagesschule_group
	add constraint FK_bezeichnung_id
foreign key (bezeichnung_id)
references text_ressource (id);