# Tabellen GemeindeStammdaten erstellen

CREATE TABLE gemeinde_stammdaten_aud (
	id                 		VARCHAR(36)	NOT NULL,
	rev                   	INTEGER     NOT NULL,
	revtype               	TINYINT,
	timestamp_erstellt 		DATETIME,
	timestamp_mutiert  		DATETIME,
	user_erstellt      		VARCHAR(36),
	user_mutiert       		VARCHAR(36),
	version            		BIGINT,
	vorgaenger_id      		VARCHAR(36),
	default_benutzerbg_id 	VARCHAR(36),
	default_benutzerts_id	VARCHAR(36),
	gemeinde_id 			VARCHAR(36),
	adresse_id           	VARCHAR(36),
	mail               		VARCHAR(255),
	telefon             	VARCHAR(255),
	webseite             	VARCHAR(255),
	PRIMARY KEY (id, rev)
);

CREATE TABLE gemeinde_stammdaten (
	id                 		VARCHAR(36) NOT NULL,
	timestamp_erstellt 		DATETIME    NOT NULL,
	timestamp_mutiert  		DATETIME    NOT NULL,
	user_erstellt      		VARCHAR(36) NOT NULL,
	user_mutiert       		VARCHAR(36) NOT NULL,
	version            		BIGINT      NOT NULL,
	vorgaenger_id      		VARCHAR(36),
	default_benutzerbg_id 	VARCHAR(36),
	default_benutzerts_id	VARCHAR(36),
	gemeinde_id 			VARCHAR(36) NOT NULL,
	adresse_id           	VARCHAR(36) NOT NULL,
	mail               		VARCHAR(255)NOT NULL,
	telefon             	VARCHAR(255),
	webseite             	VARCHAR(255),
	PRIMARY KEY (id)
);

alter table gemeinde_stammdaten
	add constraint FK_gemeindestammdaten_defaultbenutzerbg_id
	foreign key (default_benutzerbg_id)
	references benutzer (id);

alter table gemeinde_stammdaten
	add constraint FK_gemeindestammdaten_defaultbenutzerts_id
	foreign key (default_benutzerts_id)
	references benutzer (id);

alter table gemeinde_stammdaten
	add constraint FK_gemeindestammdaten_gemeinde_id
	foreign key (gemeinde_id)
	references gemeinde (id);

alter table gemeinde_stammdaten
	add constraint FK_gemeindestammdaten_adresse_id
	foreign key (adresse_id)
	references adresse (id);

ALTER TABLE gemeinde_stammdaten_aud
	ADD CONSTRAINT FK_gemeindestammdaten_aud_revinfo
	FOREIGN KEY (rev)
	REFERENCES revinfo (rev);
