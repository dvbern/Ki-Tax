CREATE TABLE pensum_ausserordentlicher_anspruch (
	id                 VARCHAR(36)  NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	pensum             INTEGER      NOT NULL,
	begruendung        VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE pensum_ausserordentlicher_anspruch_aud (
	id                 VARCHAR(36) NOT NULL,
	rev                INTEGER     NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	pensum             INTEGER,
	begruendung        VARCHAR(255),
	PRIMARY KEY (id, rev)
);

ALTER TABLE kind ADD pensum_ausserordentlicher_anspruch_id VARCHAR(36);
ALTER TABLE kind_aud ADD pensum_ausserordentlicher_anspruch_id VARCHAR(36);

ALTER TABLE kind
	ADD CONSTRAINT FK_kind_pensum_ausserordentlicheranspruch_id
FOREIGN KEY (pensum_ausserordentlicher_anspruch_id)
REFERENCES pensum_ausserordentlicher_anspruch (id);

ALTER TABLE pensum_ausserordentlicher_anspruch_aud
	ADD CONSTRAINT FK_pensum_ausserordentlicheranspruch_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);