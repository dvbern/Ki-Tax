CREATE TABLE gemeinde (
	id                 VARCHAR(36) NOT NULL,
	timestamp_erstellt DATETIME    NOT NULL,
	timestamp_mutiert  DATETIME    NOT NULL,
	user_erstellt      VARCHAR(36) NOT NULL,
	user_mutiert       VARCHAR(36) NOT NULL,
	version            BIGINT      NOT NULL,
	vorgaenger_id      VARCHAR(36),
	enabled            BIT         NOT NULL,
	name               VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE gemeinde_aud (
	id                 VARCHAR(36) NOT NULL,
	rev                INTEGER     NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(36),
	user_mutiert       VARCHAR(36),
	vorgaenger_id      VARCHAR(36),
	enabled            BIT,
	name               VARCHAR(255),
	PRIMARY KEY (id, rev)
);

INSERT INTO gemeinde (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, vorgaenger_id, enabled, name)
VALUES (UUID(), '2018-01-01 00:00:00', '2018-01-01 00:00:00', 'flyway', 'flyway', 0, NULL, TRUE, 'Bern');

ALTER TABLE gemeinde_aud
	ADD CONSTRAINT FK_gemeinde_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);

ALTER TABLE dossier
	ADD gemeinde_id VARCHAR(36);

ALTER TABLE dossier_aud
	ADD gemeinde_id VARCHAR(36);

alter table dossier
	add constraint UK_dossier_fall_gemeinde unique (fall_id, gemeinde_id);

alter table gemeinde
	add constraint UK_gemeinde_name unique (name);

UPDATE dossier
SET gemeinde_id = (
	SELECT id
	FROM gemeinde
	WHERE name = 'Bern');

ALTER TABLE dossier
	MODIFY gemeinde_id VARCHAR(36) NOT NULL;

ALTER TABLE dossier
	ADD CONSTRAINT FK_dossier_gemeinde_id
FOREIGN KEY (gemeinde_id)
REFERENCES gemeinde (id);