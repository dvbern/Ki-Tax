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

drop table belegung_tagesschule_modul_tagesschule_aud;
drop table belegung_tagesschule_modul_tagesschule;


create table belegung_tagesschule_modul (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	intervall varchar(255) not null,
	belegung_tagesschule_id binary(16) not null,
	modul_tagesschule_id binary(16) not null,
	primary key (id)
);

create table belegung_tagesschule_modul_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	intervall varchar(255),
	belegung_tagesschule_id binary(16),
	modul_tagesschule_id binary(16),
	primary key (id, rev)
);

alter table belegung_tagesschule_modul_aud
	add constraint FK_belegung_ts_modul_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table belegung_tagesschule_modul
	add constraint FK_belegung_ts_modul_belegung_ts
foreign key (belegung_tagesschule_id)
references belegung_tagesschule (id);

alter table belegung_tagesschule_modul
	add constraint FK_belegung_ts_modul_modul_ts
foreign key (modul_tagesschule_id)
references modul_tagesschule (id);