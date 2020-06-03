CREATE TABLE kitax_uebergangsloesung_institution_oeffnungszeiten (
	id                 BINARY(16)     NOT NULL,
	timestamp_erstellt DATETIME       NOT NULL,
	timestamp_mutiert  DATETIME       NOT NULL,
	user_erstellt      VARCHAR(255)   NOT NULL,
	user_mutiert       VARCHAR(255)   NOT NULL,
	version            BIGINT         NOT NULL,
	name_kibon         VARCHAR(255)   NOT NULL,
	name_kitax         VARCHAR(255)   NOT NULL,
	oeffnungsstunden   DECIMAL(19, 2) NOT NULL,
	oeffnungstage      DECIMAL(19, 2) NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE kitax_uebergangsloesung_institution_oeffnungszeiten
	ADD CONSTRAINT UK_kitax_uebergangsloesung_institution_oeffnungszeiten UNIQUE (name_kibon);