CREATE TABLE betreuungspensum_abweichung (
	id                          BINARY(16)     NOT NULL,
	timestamp_erstellt          DATETIME       NOT NULL,
	timestamp_mutiert           DATETIME       NOT NULL,
	user_erstellt               VARCHAR(255)   NOT NULL,
	user_mutiert                VARCHAR(255)   NOT NULL,
	version                     BIGINT         NOT NULL,
	vorgaenger_id               VARCHAR(36),
	gueltig_ab                  DATE           NOT NULL,
	gueltig_bis                 DATE           NOT NULL,
	monatliche_betreuungskosten DECIMAL(19, 2) NOT NULL,
	pensum                      DECIMAL(19, 10) NOT NULL,
	unit_for_display            VARCHAR(255)   NOT NULL,
	status				        VARCHAR(55)    NOT NULL,
	betreuung_id          		BINARY(16)     NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE betreuungspensum_abweichung_aud (
	id                          BINARY(16) NOT NULL,
	rev                         INTEGER    NOT NULL,
	revtype                     TINYINT,
	timestamp_erstellt          DATETIME,
	timestamp_mutiert           DATETIME,
	user_erstellt               VARCHAR(255),
	user_mutiert                VARCHAR(255),
	vorgaenger_id               VARCHAR(36),
	gueltig_ab                  DATE,
	gueltig_bis                 DATE,
	monatliche_betreuungskosten DECIMAL(19, 2),
	pensum                      DECIMAL(19, 10),
	unit_for_display            VARCHAR(255),
	status				        VARCHAR(55),
	betreuung_id          		BINARY(16),
	PRIMARY KEY (id, rev)
);

ALTER TABLE betreuungspensum_abweichung_aud
	ADD CONSTRAINT FK_betreuungspensum_abweichung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE betreuungspensum_abweichung
	ADD CONSTRAINT FK_betreuungspensum_abweichung_betreuung_id
FOREIGN KEY (betreuung_id)
REFERENCES betreuung(id);