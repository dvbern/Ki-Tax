# Neue Entities fuer Anmeldungen (TS und FI)

create table anmeldung_ferieninsel_aud (
	id                         binary(16) not null,
	rev                        integer    not null,
	revtype                    tinyint,
	timestamp_erstellt         datetime,
	timestamp_mutiert          datetime,
	user_erstellt              varchar(255),
	user_mutiert               varchar(255),
	vorgaenger_id              varchar(36),
	betreuung_nummer           integer,
	gueltig                    bit,
	anmeldestatus              varchar(255),
	anmeldung_mutation_zustand varchar(255),
	kind_id                    binary(16),
	belegung_ferieninsel_id    binary(16),
	institution_stammdaten_id  binary(16),
	primary key (id, rev)
);

create table anmeldung_tagesschule_aud (
	id                         binary(16) not null,
	rev                        integer    not null,
	revtype                    tinyint,
	timestamp_erstellt         datetime,
	timestamp_mutiert          datetime,
	user_erstellt              varchar(255),
	user_mutiert               varchar(255),
	vorgaenger_id              varchar(36),
	betreuung_nummer           integer,
	gueltig                    bit,
	anmeldestatus              varchar(255),
	anmeldung_mutation_zustand varchar(255),
	keine_detailinformationen  bit,
	kind_id                    binary(16),
	belegung_tagesschule_id    binary(16),
	institution_stammdaten_id  binary(16),
	primary key (id, rev)
);

create table anmeldung_ferieninsel (
	id                         binary(16)   not null,
	timestamp_erstellt         datetime     not null,
	timestamp_mutiert          datetime     not null,
	user_erstellt              varchar(255) not null,
	user_mutiert               varchar(255) not null,
	version                    bigint       not null,
	vorgaenger_id              varchar(36),
	betreuung_nummer           integer      not null,
	gueltig                    bit          not null,
	anmeldestatus              varchar(255) not null,
	anmeldung_mutation_zustand varchar(255),
	kind_id                    binary(16)   not null,
	belegung_ferieninsel_id    binary(16),
	institution_stammdaten_id  binary(16)   not null,
	primary key (id)
);

create table anmeldung_tagesschule (
	id                         binary(16)   not null,
	timestamp_erstellt         datetime     not null,
	timestamp_mutiert          datetime     not null,
	user_erstellt              varchar(255) not null,
	user_mutiert               varchar(255) not null,
	version                    bigint       not null,
	vorgaenger_id              varchar(36),
	betreuung_nummer           integer      not null,
	gueltig                    bit          not null,
	anmeldestatus              varchar(255) not null,
	anmeldung_mutation_zustand varchar(255),
	keine_detailinformationen  bit          not null,
	kind_id                    binary(16)   not null,
	belegung_tagesschule_id    binary(16),
	institution_stammdaten_id  binary(16)   not null,
	primary key (id)
);

alter table anmeldung_ferieninsel
	add constraint UK_anmeldung_ferieninsel_kind_betreuung_nummer unique (betreuung_nummer, kind_id);

alter table anmeldung_tagesschule
	add constraint UK_anmeldung_tagesschule_kind_betreuung_nummer unique (betreuung_nummer, kind_id);

alter table anmeldung_ferieninsel_aud
	add constraint FK_anmeldung_ferieninsel_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table anmeldung_tagesschule_aud
	add constraint FK_anmeldung_tagesschule_aud_revinfo
foreign key (rev)
references revinfo (rev);

alter table anmeldung_ferieninsel
	add constraint FK_anmeldung_ferieninsel_kind_id
foreign key (kind_id)
references kind_container (id);

alter table anmeldung_ferieninsel
	add constraint FK_anmeldung_ferieninsel_belegung_ferieninsel_id
foreign key (belegung_ferieninsel_id)
references belegung_ferieninsel (id);

alter table anmeldung_ferieninsel
	add constraint FK_anmeldung_ferieninsel_institution_stammdaten_id
foreign key (institution_stammdaten_id)
references institution_stammdaten (id);

alter table anmeldung_tagesschule
	add constraint FK_anmeldung_tagesschule_kind_id
foreign key (kind_id)
references kind_container (id);

alter table anmeldung_tagesschule
	add constraint FK_anmeldung_tagesschule_belegung_tagesschule_id
foreign key (belegung_tagesschule_id)
references belegung_tagesschule (id);

alter table anmeldung_tagesschule
	add constraint FK_anmeldung_tagesschule_institution_stammdaten_id
foreign key (institution_stammdaten_id)
references institution_stammdaten (id);

# Auslagerung der BG-Stammdaten in InstitutionStammdatenBetreuungsgutscheine

create table institution_stammdaten_betreuungsgutscheine (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	alterskategorie_baby bit not null,
	alterskategorie_kindergarten bit not null,
	alterskategorie_schule bit not null,
	alterskategorie_vorschule bit not null,
	anzahl_plaetze decimal(19,2) not null,
	anzahl_plaetze_firmen decimal(19,2),
	iban varchar(34),
	kontoinhaber varchar(255),
	subventionierte_plaetze bit not null,
	adresse_kontoinhaber_id binary(16),
	primary key (id)
);

create table institution_stammdaten_betreuungsgutscheine_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	alterskategorie_baby bit,
	alterskategorie_kindergarten bit,
	alterskategorie_schule bit,
	alterskategorie_vorschule bit,
	anzahl_plaetze decimal(19,2),
	anzahl_plaetze_firmen decimal(19,2),
	iban varchar(34),
	kontoinhaber varchar(255),
	subventionierte_plaetze bit,
	adresse_kontoinhaber_id binary(16),
	primary key (id, rev)
);

alter table institution_stammdaten add institution_stammdaten_betreuungsgutscheine_id binary(16);
alter table institution_stammdaten_aud add institution_stammdaten_betreuungsgutscheine_id binary(16);

alter table institution_stammdaten_betreuungsgutscheine_aud
	add constraint FK_institution_stammdaten_betreuungsgutscheine_aud_revinfo
foreign key (rev)
references revinfo (rev);

# Migration der bestehenden Daten in die neue Tabelle

update institution_stammdaten set institution_stammdaten_betreuungsgutscheine_id =
											(SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', '')));

# Einige Felder sind zwingend, waren es aber frueher nicht
update institution_stammdaten set anzahl_plaetze = 0.00 where anzahl_plaetze is null;

INSERT INTO institution_stammdaten_betreuungsgutscheine (id,
														 timestamp_erstellt,
														 timestamp_mutiert,
														 user_erstellt,
														 user_mutiert,
														 version,
														 alterskategorie_baby,
														 alterskategorie_kindergarten,
														 alterskategorie_schule,
														 alterskategorie_vorschule,
														 anzahl_plaetze,
														 anzahl_plaetze_firmen,
														 iban,
														 kontoinhaber,
														 subventionierte_plaetze,
														 adresse_kontoinhaber_id)
SELECT *
FROM (SELECT gp.institution_stammdaten_betreuungsgutscheine_id as id,
			 gp.timestamp_erstellt                             as timestamp_erstellt,
			 gp.timestamp_mutiert                              as timestamp_mutiert,
			 gp.user_erstellt                                  as user_erstellt,
			 gp.user_mutiert                                   as user_mutiert,
			 gp.version                                        as version,
			 gp.alterskategorie_baby                           as alterskategorie_baby,
			 gp.alterskategorie_kindergarten                   as alterskategorie_kindergarten,
			 gp.alterskategorie_schule                         as alterskategorie_schule,
			 gp.alterskategorie_vorschule                      as alterskategorie_vorschule,
			 gp.anzahl_plaetze                                 as anzahl_plaetze,
			 gp.anzahl_plaetze_firmen                          as anzahl_plaetze_firmen,
			 gp.iban                                           as iban,
			 gp.kontoinhaber                                   as kontoinhaber,
			 gp.subventionierte_plaetze                        as subventionierte_plaetze,
			 gp.adresse_kontoinhaber_id                        as adresse_kontoinhaber_id
	  from institution_stammdaten as gp) as tmp;

alter table institution_stammdaten_betreuungsgutscheine
	add constraint UK_institution_stammdaten_bg_adressekontoinhaber_id unique (adresse_kontoinhaber_id);

alter table institution_stammdaten_betreuungsgutscheine
	add constraint FK_institution_stammdaten_bg_adressekontoinhaber_id
foreign key (adresse_kontoinhaber_id)
references adresse (id);

alter table institution_stammdaten
	add constraint FK_inst_stammdaten_inst_stammdaten_bg_id
foreign key (institution_stammdaten_betreuungsgutscheine_id)
references institution_stammdaten_betreuungsgutscheine (id);

# Auf der alten Tabelle entfernen

alter table institution_stammdaten
	drop foreign key FK_institution_stammdaten_adressekontoinhaber_id;

alter table institution_stammdaten
	drop key UK_institution_stammdaten_adressekontoinhaber_id;

alter table institution_stammdaten drop alterskategorie_baby;
alter table institution_stammdaten drop alterskategorie_kindergarten;
alter table institution_stammdaten drop alterskategorie_schule;
alter table institution_stammdaten drop alterskategorie_vorschule;
alter table institution_stammdaten drop anzahl_plaetze;
alter table institution_stammdaten drop anzahl_plaetze_firmen;
alter table institution_stammdaten drop iban;
alter table institution_stammdaten drop kontoinhaber;
alter table institution_stammdaten drop subventionierte_plaetze;
alter table institution_stammdaten drop adresse_kontoinhaber_id;

alter table institution_stammdaten_aud drop alterskategorie_baby;
alter table institution_stammdaten_aud drop alterskategorie_kindergarten;
alter table institution_stammdaten_aud drop alterskategorie_schule;
alter table institution_stammdaten_aud drop alterskategorie_vorschule;
alter table institution_stammdaten_aud drop anzahl_plaetze;
alter table institution_stammdaten_aud drop anzahl_plaetze_firmen;
alter table institution_stammdaten_aud drop iban;
alter table institution_stammdaten_aud drop kontoinhaber;
alter table institution_stammdaten_aud drop subventionierte_plaetze;
alter table institution_stammdaten_aud drop adresse_kontoinhaber_id;

# Gemeinde auf InstitutionStammdaten TS und FI hinzufügen

alter table institution_stammdaten_ferieninsel add gemeinde_id binary(16) not null;
alter table institution_stammdaten_ferieninsel_aud add gemeinde_id binary(16);

alter table institution_stammdaten_tagesschule add gemeinde_id binary(16) not null;
alter table institution_stammdaten_tagesschule_aud add gemeinde_id binary(16);

alter table institution_stammdaten_ferieninsel
	add constraint FK_institution_stammdaten_fi_gemeinde_id
foreign key (gemeinde_id)
references gemeinde (id);

alter table institution_stammdaten_tagesschule
	add constraint FK_institution_stammdaten_ts_gemeinde_id
foreign key (gemeinde_id)
references gemeinde (id);