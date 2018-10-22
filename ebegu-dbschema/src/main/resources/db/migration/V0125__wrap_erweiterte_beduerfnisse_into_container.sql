-- drop old column
ALTER TABLE betreuung
	DROP COLUMN erweiterte_beduerfnisse;
ALTER TABLE betreuung_aud
	DROP COLUMN erweiterte_beduerfnisse;

-- add new entity
CREATE TABLE erweiterte_betreuung (
	id                      VARCHAR(36) NOT NULL,
	timestamp_erstellt      DATETIME    NOT NULL,
	timestamp_mutiert       DATETIME    NOT NULL,
	user_erstellt           VARCHAR(36) NOT NULL,
	user_mutiert            VARCHAR(36) NOT NULL,
	version                 BIGINT      NOT NULL,
	vorgaenger_id           VARCHAR(36),
	erweiterte_beduerfnisse BIT         NOT NULL,
	PRIMARY KEY (id)
);

-- add new entity audit
CREATE TABLE erweiterte_betreuung_aud (
	id                      VARCHAR(36) NOT NULL,
	rev                     INTEGER     NOT NULL,
	revtype                 TINYINT,
	timestamp_erstellt      DATETIME,
	timestamp_mutiert       DATETIME,
	user_erstellt           VARCHAR(36),
	user_mutiert            VARCHAR(36),
	vorgaenger_id           VARCHAR(36),
	erweiterte_beduerfnisse BIT,
	PRIMARY KEY (id, rev)
);

-- add new container
CREATE TABLE erweiterte_betreuung_container (
	id                        VARCHAR(36) NOT NULL,
	timestamp_erstellt        DATETIME    NOT NULL,
	timestamp_mutiert         DATETIME    NOT NULL,
	user_erstellt             VARCHAR(36) NOT NULL,
	user_mutiert              VARCHAR(36) NOT NULL,
	version                   BIGINT      NOT NULL,
	vorgaenger_id             VARCHAR(36),
	betreuung_id              VARCHAR(36) NOT NULL,
	erweiterte_betreuunggs_id VARCHAR(36),
	erweiterte_betreuungja_id VARCHAR(36),
	PRIMARY KEY (id)
);

-- add new container audit
CREATE TABLE erweiterte_betreuung_container_aud (
	id                        VARCHAR(36) NOT NULL,
	rev                       INTEGER     NOT NULL,
	revtype                   TINYINT,
	timestamp_erstellt        DATETIME,
	timestamp_mutiert         DATETIME,
	user_erstellt             VARCHAR(36),
	user_mutiert              VARCHAR(36),
	vorgaenger_id             VARCHAR(36),
	betreuung_id              VARCHAR(36),
	erweiterte_betreuunggs_id VARCHAR(36),
	erweiterte_betreuungja_id VARCHAR(36),
	PRIMARY KEY (id, rev)
);

-- add constraints
ALTER TABLE erweiterte_betreuung_aud
	ADD CONSTRAINT FK_erweiterte_betreuung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);

ALTER TABLE erweiterte_betreuung_container_aud
	ADD CONSTRAINT FK_erweiterte_betreuung_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);

ALTER TABLE erweiterte_betreuung_container
	ADD CONSTRAINT UK_erweiterte_betreuung_betreuung UNIQUE (betreuung_id);

ALTER TABLE erweiterte_betreuung_container
	ADD CONSTRAINT FK_erweiterte_betreuung_container_betreuung_id
FOREIGN KEY (betreuung_id)
REFERENCES betreuung (id);

ALTER TABLE erweiterte_betreuung_container
	ADD CONSTRAINT FK_erweiterte_betreuung_container_erweiterte_betreuung_gs
FOREIGN KEY (erweiterte_betreuunggs_id)
REFERENCES erweiterte_betreuung (id);

ALTER TABLE erweiterte_betreuung_container
	ADD CONSTRAINT FK_erweiterte_betreuung_container_erweiterte_betreuung_ja
FOREIGN KEY (erweiterte_betreuungja_id)
REFERENCES erweiterte_betreuung (id);
