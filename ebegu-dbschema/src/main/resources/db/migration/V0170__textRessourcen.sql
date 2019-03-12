-- add new entity
CREATE TABLE text_ressource (
	id                      VARCHAR(36)   NOT NULL,
	timestamp_erstellt      DATETIME      NOT NULL,
	timestamp_mutiert       DATETIME      NOT NULL,
	user_erstellt           VARCHAR(36)   NOT NULL,
	user_mutiert            VARCHAR(36)   NOT NULL,
	version                 BIGINT        NOT NULL,
	vorgaenger_id           VARCHAR(36),
	sprache					VARCHAR(16)   NOT NULL,
	text					VARCHAR(4000) NOT NULL,
	PRIMARY KEY (id)
);

-- add new entity audit
CREATE TABLE text_ressource_aud (
	id                      VARCHAR(36) NOT NULL,
	rev                     INTEGER     NOT NULL,
	revtype                 TINYINT,
	timestamp_erstellt      DATETIME,
	timestamp_mutiert       DATETIME,
	user_erstellt           VARCHAR(36),
	user_mutiert            VARCHAR(36),
	vorgaenger_id           VARCHAR(36),
	sprache					VARCHAR(16),
	text					VARCHAR(4000),
	PRIMARY KEY (id, rev)
);

-- add new container
CREATE TABLE text_ressource_container (
	id                        VARCHAR(36) NOT NULL,
	timestamp_erstellt        DATETIME    NOT NULL,
	timestamp_mutiert         DATETIME    NOT NULL,
	user_erstellt             VARCHAR(36) NOT NULL,
	user_mutiert              VARCHAR(36) NOT NULL,
	version                   BIGINT      NOT NULL,
	vorgaenger_id             VARCHAR(36),
	text_ressource_de_id      VARCHAR(36),
	text_ressource_fr_id      VARCHAR(36),
	PRIMARY KEY (id)
);

-- add new container audit
CREATE TABLE text_ressource_container_aud (
	id                        VARCHAR(36) NOT NULL,
	rev                       INTEGER     NOT NULL,
	revtype                   TINYINT,
	timestamp_erstellt        DATETIME,
	timestamp_mutiert         DATETIME,
	user_erstellt             VARCHAR(36),
	user_mutiert              VARCHAR(36),
	vorgaenger_id             VARCHAR(36),
	text_ressource_de_id      VARCHAR(36),
	text_ressource_fr_id      VARCHAR(36),
	PRIMARY KEY (id, rev)
);

-- add reference to GemeindeStammdaten
ALTER TABLE gemeinde_stammdaten
	ADD COLUMN rechtsmittelbelehrung_id VARCHAR(36);

ALTER TABLE gemeinde_stammdaten_aud
	ADD COLUMN rechtsmittelbelehrung_id VARCHAR(36);

-- add constraints
ALTER TABLE text_ressource_aud
	ADD CONSTRAINT FK_text_ressource_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);

ALTER TABLE text_ressource_container_aud
	ADD CONSTRAINT FK_text_ressource_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT UK_rechtsmittelbelehrung_id UNIQUE (rechtsmittelbelehrung_id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_rechtsmittelbelehrung_id
FOREIGN KEY (rechtsmittelbelehrung_id)
REFERENCES text_ressource_container (id);

ALTER TABLE text_ressource_container
	ADD CONSTRAINT FK_text_ressource_container_text_ressource_deutsch
FOREIGN KEY (text_ressource_de_id)
REFERENCES text_ressource (id);

ALTER TABLE text_ressource_container
	ADD CONSTRAINT FK_text_ressource_container_text_ressource_franzoesisch
FOREIGN KEY (text_ressource_fr_id)
REFERENCES text_ressource (id);
