create table modul_tagesschule_group (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	bezeichnung varchar(255) not null,
	reihenfolge integer not null,
	intervall varchar(255) not null,
	modul_tagesschule_name varchar(255) not null,
	verpflegungskosten decimal(19,2),
	wird_paedagogisch_betreut bit not null,
	zeit_bis time not null,
	zeit_von time not null,
	gesuchsperiode_id binary(16) not null,
	institution_stammdaten_tagesschule_id binary(16) not null,
	primary key (id)
);

create table modul_tagesschule_group_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	bezeichnung varchar(255),
	reihenfolge integer,
	intervall varchar(255),
	modul_tagesschule_name varchar(255),
	verpflegungskosten decimal(19,2),
	wird_paedagogisch_betreut bit,
	zeit_bis time,
	zeit_von time,
	gesuchsperiode_id binary(16),
	institution_stammdaten_tagesschule_id binary(16),
	primary key (id, rev)
);

ALTER TABLE modul_tagesschule DROP FOREIGN KEY FK_modul_tagesschule_inst_stammdaten_tagesschule_id;

ALTER TABLE modul_tagesschule DROP vorgaenger_id;
ALTER TABLE modul_tagesschule_aud DROP vorgaenger_id;

ALTER TABLE modul_tagesschule DROP zeit_von;
ALTER TABLE modul_tagesschule_aud DROP zeit_von;

ALTER TABLE modul_tagesschule DROP zeit_bis;
ALTER TABLE modul_tagesschule_aud DROP zeit_bis;

ALTER TABLE modul_tagesschule DROP institution_stammdaten_tagesschule_id;
ALTER TABLE modul_tagesschule_aud DROP institution_stammdaten_tagesschule_id;

ALTER TABLE modul_tagesschule DROP modul_tagesschule_name;
ALTER TABLE modul_tagesschule_aud DROP modul_tagesschule_name;

ALTER TABLE modul_tagesschule ADD COLUMN modul_tagesschule_group_id BINARY(16) NOT NULL;
ALTER TABLE modul_tagesschule_aud ADD COLUMN modul_tagesschule_group_id BINARY(16);

ALTER TABLE institution_stammdaten_tagesschule ADD COLUMN modul_tagesschule_typ VARCHAR(255) NOT NULL;
ALTER TABLE institution_stammdaten_tagesschule_aud ADD COLUMN modul_tagesschule_typ VARCHAR(255);

UPDATE institution_stammdaten_tagesschule SET modul_tagesschule_typ = 'SCOLARIS';

alter table modul_tagesschule_group_aud
	add constraint FK_modul_tagesschule_group_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table modul_tagesschule_group
	add constraint FK_modul_tagesschule_gesuchsperiode_id
foreign key (gesuchsperiode_id)
references gesuchsperiode (id);

alter table modul_tagesschule_group
	add constraint FK_modul_tagesschule_inst_stammdaten_tagesschule_id
foreign key (institution_stammdaten_tagesschule_id)
references institution_stammdaten_tagesschule (id);

alter table modul_tagesschule
	add constraint FK_modul_tagesschule_modul_tagesschule_group_id
foreign key (modul_tagesschule_group_id)
references modul_tagesschule_group (id);
