CREATE TABLE socialhilfe_zeitraum (
	id                    BINARY(16)   NOT NULL,
	timestamp_erstellt    DATETIME     NOT NULL,
	timestamp_mutiert     DATETIME     NOT NULL,
	user_erstellt         VARCHAR(255) NOT NULL,
	user_mutiert          VARCHAR(255) NOT NULL,
	version               BIGINT       NOT NULL,
	vorgaenger_id         VARCHAR(36),
	gueltig_ab            DATE         NOT NULL,
	gueltig_bis           DATE         NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE socialhilfe_zeitraum_aud (
	id                    BINARY(16) NOT NULL,
	rev                   INTEGER    NOT NULL,
	revtype               TINYINT,
	timestamp_erstellt    DATETIME,
	timestamp_mutiert     DATETIME,
	user_erstellt         VARCHAR(255),
	user_mutiert          VARCHAR(255),
	vorgaenger_id         VARCHAR(36),
	gueltig_ab            DATE,
	gueltig_bis           DATE,
	PRIMARY KEY (id, rev)
);

CREATE TABLE socialhilfe_zeitraum_container_aud (
	id                         BINARY(16) NOT NULL,
	rev                        INTEGER    NOT NULL,
	revtype                    TINYINT,
	timestamp_erstellt         DATETIME,
	timestamp_mutiert          DATETIME,
	user_erstellt              VARCHAR(255),
	user_mutiert               VARCHAR(255),
	vorgaenger_id              VARCHAR(36),
	socialhilfe_zeitraumgs_id         BINARY(16),
	socialhilfe_zeitraumja_id         BINARY(16),
	familiensituation_container_id BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE socialhilfe_zeitraum_container (
	id                         BINARY(16)   NOT NULL,
	timestamp_erstellt         DATETIME     NOT NULL,
	timestamp_mutiert          DATETIME     NOT NULL,
	user_erstellt              VARCHAR(255) NOT NULL,
	user_mutiert               VARCHAR(255) NOT NULL,
	version                    BIGINT       NOT NULL,
	vorgaenger_id              VARCHAR(36),
	socialhilfe_zeitraumgs_id         BINARY(16),
	socialhilfe_zeitraumja_id         BINARY(16),
	familiensituation_container_id BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE socialhilfe_zeitraum_aud
	ADD CONSTRAINT FK_socialhilfe_zeitraum_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE socialhilfe_zeitraum_container_aud
	ADD CONSTRAINT FK_socialhilfe_zeitraum_container_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE socialhilfe_zeitraum_container
	ADD CONSTRAINT FK_socialhilfe_zeitraum_container_socialhilfezeitraumgs_id
FOREIGN KEY (socialhilfe_zeitraumgs_id)
REFERENCES socialhilfe_zeitraum(id);

ALTER TABLE socialhilfe_zeitraum_container
	ADD CONSTRAINT FK_socialhilfe_zeitraum_container_socialhilfezeitraumja_id
FOREIGN KEY (socialhilfe_zeitraumja_id)
REFERENCES socialhilfe_zeitraum(id);

ALTER TABLE socialhilfe_zeitraum_container
	ADD CONSTRAINT FK_socialhilfe_zeitraum_container_familiensituation_id
FOREIGN KEY (familiensituation_container_id)
REFERENCES familiensituation_container(id);