
# Neue Entitaet Auszahlungsdaten

create table auszahlungsdaten (
	id binary(16) not null,
	timestamp_erstellt datetime not null,
	timestamp_mutiert datetime not null,
	user_erstellt varchar(255) not null,
	user_mutiert varchar(255) not null,
	version bigint not null,
	iban varchar(34) not null,
	kontoinhaber varchar(255) not null,
	adresse_kontoinhaber_id binary(16),
	tmp_id binary(16),
	primary key (id)
);

create table auszahlungsdaten_aud (
	id binary(16) not null,
	rev integer not null,
	revtype tinyint,
	timestamp_erstellt datetime,
	timestamp_mutiert datetime,
	user_erstellt varchar(255),
	user_mutiert varchar(255),
	iban varchar(34),
	kontoinhaber varchar(255),
	adresse_kontoinhaber_id binary(16),
	primary key (id, rev)
);

alter table auszahlungsdaten
	add constraint FK_auszahlungsdaten_adressekontoinhaber_id
		foreign key (adresse_kontoinhaber_id)
			references adresse (id);

alter table auszahlungsdaten_aud
	add constraint FK_auszahlungsdaten_aud_revinfo
		foreign key (rev)
			references revinfo (rev);

# (1) Diese verwenden in InstitutionStammdatenBetreuungsgutscheine

alter table institution_stammdaten_betreuungsgutscheine add auszahlungsdaten_id binary(16);
alter table institution_stammdaten_betreuungsgutscheine_aud add auszahlungsdaten_id binary(16);

alter table institution_stammdaten_betreuungsgutscheine
	add constraint UK_institution_stammdaten_bg_auszahlungsdaten_id unique (auszahlungsdaten_id);

alter table institution_stammdaten_betreuungsgutscheine
	add constraint FK_institution_stammdaten_bg_auszahlungsdaten_id
		foreign key (auszahlungsdaten_id)
			references auszahlungsdaten (id);

# Migration der Daten von InstitutionStammdatenBetreuungsgutscheine zu Auszahlungsdaten (via tempId Feld)
INSERT INTO auszahlungsdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							  iban, kontoinhaber, adresse_kontoinhaber_id, tmp_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-06-17 00:00:00'              as timestamp_erstellt,
			 '2020-06-17 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 gp.iban as iban,
			 gp.kontoinhaber							as kontoinhaber,
			 gp.adresse_kontoinhaber_id                               as adresse_kontoinhaber_id,
			 gp.id                              as tmp_id
	  from institution_stammdaten_betreuungsgutscheine as gp where gp.iban is not null) as tmp;

update institution_stammdaten_betreuungsgutscheine isb set isb.auszahlungsdaten_id = (
	select id from auszahlungsdaten where tmp_id = isb.id
) ;

# Jetzt die alten Spalten loeschen

alter table institution_stammdaten_betreuungsgutscheine
	drop CONSTRAINT  FK_institution_stammdaten_bg_adressekontoinhaber_id;

alter table institution_stammdaten_betreuungsgutscheine
	drop CONSTRAINT UK_institution_stammdaten_bg_adressekontoinhaber_id;

alter table institution_stammdaten_betreuungsgutscheine drop iban;
alter table institution_stammdaten_betreuungsgutscheine drop kontoinhaber;
alter table institution_stammdaten_betreuungsgutscheine drop adresse_kontoinhaber_id;

alter table institution_stammdaten_betreuungsgutscheine_aud drop iban;
alter table institution_stammdaten_betreuungsgutscheine_aud drop kontoinhaber;
alter table institution_stammdaten_betreuungsgutscheine_aud drop adresse_kontoinhaber_id;



# (2) Diese verwenden in Familiensituation

alter table familiensituation add auszahlungsdaten_id binary(16);
alter table familiensituation_aud add auszahlungsdaten_id binary(16);

alter table familiensituation
	add constraint UK_familiensituation_auszahlungsdaten_id unique (auszahlungsdaten_id);

alter table familiensituation
	add constraint FK_familiensituation_auszahlungsdaten_id
		foreign key (auszahlungsdaten_id)
			references auszahlungsdaten (id);

# Migration der Daten von Familiensituation zu Auszahlungsdaten (via tempId Feld)
INSERT INTO auszahlungsdaten (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version,
							  iban, kontoinhaber, adresse_kontoinhaber_id, tmp_id)
SELECT *
FROM (SELECT UNHEX(REPLACE(UUID() COLLATE utf8_unicode_ci, '-', ''))    as id,
			 '2020-06-17 00:00:00'              as timestamp_erstellt,
			 '2020-06-17 00:00:00'              as timestamp_mutiert,
			 'flyway'                           as user_erstellt,
			 'flyway'                           as user_mutiert,
			 0                                  as version,
			 gp.iban as iban,
			 gp.kontoinhaber							as kontoinhaber,
			 gp.zahlungsadresse_id                               as adresse_kontoinhaber_id,
			 gp.id                              as tmp_id
	  from ebegu.familiensituation as gp where gp.iban is not null) as tmp;

update familiensituation f set f.auszahlungsdaten_id = (
	select id from auszahlungsdaten where tmp_id = f.id
);

# Jetzt die alten Spalten loeschen

alter table familiensituation
	drop CONSTRAINT FK_familiensituation_zahlungs_adresse;

alter table familiensituation drop iban;
alter table familiensituation drop kontoinhaber;
alter table familiensituation drop zahlungsadresse_id;

alter table familiensituation_aud drop iban;
alter table familiensituation_aud drop kontoinhaber;
alter table familiensituation_aud drop zahlungsadresse_id;

# (3) Auszahlungsdaten verwenden in den Zahlungen

alter table zahlung add auszahlungsdaten_id binary(16);
alter table zahlung_aud add auszahlungsdaten_id binary(16);

alter table zahlung add betreuungsangebot_typ varchar(255);
alter table zahlung add institution_id binary(16);
alter table zahlung add institution_name varchar(255);
alter table zahlung add traegerschaft_name varchar(255);

alter table zahlung_aud add betreuungsangebot_typ varchar(255);
alter table zahlung_aud add institution_id binary(16);
alter table zahlung_aud add institution_name varchar(255);
alter table zahlung_aud add traegerschaft_name varchar(255);

# Migrieren betreuungsangebotTyp, InstitutionId, institutionName, traegerschaftName

update zahlung z set z.betreuungsangebot_typ = (
	select betreuungsangebot_typ from institution_stammdaten where id = z.institution_stammdaten_id);

update zahlung z set z.institution_id = (
	select institution_id from institution_stammdaten where id = z.institution_stammdaten_id);

update zahlung z set z.institution_name = (
	select i.name from institution_stammdaten st
					   LEFT JOIN institution i ON st.institution_id = i.id where st.id = z.institution_stammdaten_id);

update zahlung z set z.traegerschaft_name = (
	select t.name from institution_stammdaten st
					   LEFT JOIN institution i ON st.institution_id = i.id
					   LEFT JOIN traegerschaft t ON i.traegerschaft_id = t.id where st.id = z.institution_stammdaten_id);

# Erst jetzt koennen die Felder not null gesetzt werden
ALTER TABLE zahlung	CHANGE COLUMN betreuungsangebot_typ betreuungsangebot_typ varchar(255) not null;
ALTER TABLE zahlung	CHANGE COLUMN institution_id institution_id binary(16) not null;
ALTER TABLE zahlung	CHANGE COLUMN institution_name institution_name varchar(255) not null;


# Migration der Daten von Zahlung zu Auszahlungsdaten

update zahlung z set z.auszahlungsdaten_id = (
	select id from auszahlungsdaten where tmp_id = (
		select institution_stammdaten_betreuungsgutscheine_id from institution_stammdaten where id = z.institution_stammdaten_id
	)
);

# Nicht mehr benoetigte Daten aufraeumen
ALTER  TABLE  zahlung drop CONSTRAINT FK_Zahlung_institutionstammdaten_id;
alter table zahlung drop institution_stammdaten_id;
alter table zahlung_aud drop institution_stammdaten_id;

# Die Migrationshilfe (tmp_id) loeschen

alter table auszahlungsdaten drop tmp_id;

# Zusaetzliches Enum auf dem Auftrag

alter table zahlungsauftrag add zahlungslauf_typ varchar(255);
alter table zahlungsauftrag_aud add zahlungslauf_typ varchar(255);

update zahlungsauftrag set zahlungslauf_typ = 'GEMEINDE_INSTITUTION';
ALTER TABLE zahlungsauftrag	CHANGE COLUMN zahlungslauf_typ zahlungslauf_typ varchar(255) not null;