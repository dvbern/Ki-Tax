CREATE TABLE dossier (
	id                      VARCHAR(36) NOT NULL,
	timestamp_erstellt      DATETIME    NOT NULL,
	timestamp_mutiert       DATETIME    NOT NULL,
	user_erstellt           VARCHAR(255) NOT NULL,
	user_mutiert            VARCHAR(255) NOT NULL,
	version                 BIGINT      NOT NULL,
	vorgaenger_id           VARCHAR(36),
	dossier_nummer          BIGINT      NOT NULL,
	fall_id                 VARCHAR(36) NOT NULL,
	verantwortlicherbg_id   VARCHAR(36),
	verantwortlicherts_id   VARCHAR(36),
	PRIMARY KEY (id)
);

CREATE TABLE dossier_aud (
	id                      VARCHAR(36) NOT NULL,
	rev                     INTEGER     NOT NULL,
	revtype                 TINYINT,
	timestamp_erstellt      DATETIME,
	timestamp_mutiert       DATETIME,
	user_erstellt           VARCHAR(255),
	user_mutiert            VARCHAR(255),
	vorgaenger_id           VARCHAR(36),
	dossier_nummer          BIGINT,
	fall_id                 VARCHAR(36),
	verantwortlicherbg_id   VARCHAR(36),
	verantwortlicherts_id   VARCHAR(36),
	PRIMARY KEY (id, rev)
);

ALTER TABLE fall ADD next_number_dossier INTEGER NOT NULL DEFAULT 1;
ALTER TABLE fall_aud ADD next_number_dossier INTEGER;

ALTER TABLE gesuch ADD dossier_id VARCHAR(36) NOT NULL;
ALTER TABLE gesuch_aud ADD dossier_id VARCHAR(36);

ALTER TABLE mitteilung ADD dossier_id VARCHAR(36) NOT NULL;
ALTER TABLE mitteilung_aud ADD dossier_id VARCHAR(36);

ALTER TABLE dossier
	ADD CONSTRAINT FK_dossier_fall_id
FOREIGN KEY (fall_id)
REFERENCES fall (id);

ALTER TABLE dossier
	ADD CONSTRAINT FK_dossier_verantwortlicher_bg_id
FOREIGN KEY (verantwortlicherbg_id)
REFERENCES benutzer (id);

ALTER TABLE dossier
	ADD CONSTRAINT FK_dossier_verantwortlicher_ts_id
FOREIGN KEY (verantwortlicherts_id)
REFERENCES benutzer (id);

ALTER TABLE dossier_aud
	ADD CONSTRAINT FK_dossier_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);

--  Datenmigration

INSERT INTO dossier (
	SELECT
		UUID(),
		timestamp_erstellt,
		timestamp_mutiert,
		user_erstellt,
		user_mutiert,
		version,
		NULL,
		0, -- dossierNummer
		id, -- fallId
		verantwortlicher_id, -- verantwortlicherBG
		verantwortlichersch_id -- verantwortlicherTS
	FROM fall);


UPDATE gesuch g SET g.dossier_id = (
	SELECT id FROM dossier WHERE fall_id = g.fall_id);
UPDATE mitteilung m SET m.dossier_id = (
	SELECT id FROM dossier WHERE fall_id = m.fall_id);

-- Jetzt sind überall die Daten gesetzt und die FKs können erstellt werden

ALTER TABLE gesuch
	ADD CONSTRAINT FK_gesuch_dossier_id
FOREIGN KEY (dossier_id)
REFERENCES dossier (id);

ALTER TABLE mitteilung
	ADD CONSTRAINT FK_mitteilung_dossier_id
FOREIGN KEY (dossier_id)
REFERENCES dossier (id);

-- Den Unique Key auf dem Gesuch anpassen
set FOREIGN_KEY_CHECKS=0;
DROP INDEX UK_gueltiges_gesuch ON gesuch;
set FOREIGN_KEY_CHECKS=1;
ALTER TABLE gesuch
	ADD CONSTRAINT UK_gueltiges_gesuch UNIQUE (dossier_id, gesuchsperiode_id, gueltig);



-- Nicht mehr benötigte Constraints und Columns löschen

ALTER TABLE gesuch DROP FOREIGN KEY FK_gesuch_fall_id;
ALTER TABLE mitteilung DROP FOREIGN KEY FK_mitteilung_fall_id;

ALTER TABLE mitteilung DROP COLUMN fall_id;
ALTER TABLE gesuch_aud DROP COLUMN fall_id;

ALTER TABLE gesuch DROP COLUMN fall_id;
ALTER TABLE mitteilung_aud DROP COLUMN fall_id;