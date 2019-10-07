create table einstellungen_tagesschule_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	modul_tagesschule_typ varchar(255),
	gesuchsperiode_id binary(16),
	institution_stammdaten_tagesschule_id binary(16),
	primary key (id, rev)
);

create table einstellungen_tagesschule (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	modul_tagesschule_typ varchar(255) not null,
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
	identifier varchar(255),
	bezeichnung varchar(255),
	intervall varchar(255),
	modul_tagesschule_name varchar(255),
	reihenfolge integer,
	verpflegungskosten decimal(19,2),
	wird_paedagogisch_betreut bit,
	zeit_bis time,
	zeit_von time,
	einstellungen_tagesschule_id binary(16),
	primary key (id, rev)
);

create table modul_tagesschule_group (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	identifier varchar(255),
	bezeichnung varchar(255) not null,
	intervall varchar(255) not null,
	modul_tagesschule_name varchar(255) not null,
	reihenfolge integer not null,
	verpflegungskosten decimal(19,2),
	wird_paedagogisch_betreut bit not null,
	zeit_bis time not null,
	zeit_von time not null,
	einstellungen_tagesschule_id binary(16) not null,
	primary key (id)
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

alter table einstellungen_tagesschule_aud
	add constraint FK_einstellungen_tagesschule_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table einstellungen_tagesschule
	add constraint FK_einstellungen_ts_gesuchsperiode_id
foreign key (gesuchsperiode_id)
references gesuchsperiode (id);

alter table einstellungen_tagesschule
	add constraint FK_einstellungen_ts_inst_stammdaten_tagesschule_id
foreign key (institution_stammdaten_tagesschule_id)
references institution_stammdaten_tagesschule (id);

alter table modul_tagesschule_group_aud
	add constraint FK_modul_tagesschule_group_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table modul_tagesschule
	add constraint FK_modul_tagesschule_modul_tagesschule_group_id
foreign key (modul_tagesschule_group_id)
references modul_tagesschule_group (id);

alter table modul_tagesschule_group
	add constraint FK_modul_tagesschule_einstellungen_tagesschule_id
foreign key (einstellungen_tagesschule_id)
references einstellungen_tagesschule (id);

# Die spezifischen InstitutionsStammdaten muessen nicht zeitabhaengig sein, die "normalen" sind es schon
alter table institution_stammdaten_ferieninsel drop vorgaenger_id;
alter table institution_stammdaten_ferieninsel drop gueltig_ab;
alter table institution_stammdaten_ferieninsel drop gueltig_bis;
alter table institution_stammdaten_ferieninsel_aud drop vorgaenger_id;
alter table institution_stammdaten_ferieninsel_aud drop gueltig_ab;
alter table institution_stammdaten_ferieninsel_aud drop gueltig_bis;

alter table institution_stammdaten_tagesschule drop vorgaenger_id;
alter table institution_stammdaten_tagesschule drop gueltig_ab;
alter table institution_stammdaten_tagesschule drop gueltig_bis;
alter table institution_stammdaten_tagesschule_aud drop vorgaenger_id;
alter table institution_stammdaten_tagesschule_aud drop gueltig_ab;
alter table institution_stammdaten_tagesschule_aud drop gueltig_bis;