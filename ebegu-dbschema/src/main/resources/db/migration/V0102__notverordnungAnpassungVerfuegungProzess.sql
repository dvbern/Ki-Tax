alter table rueckforderung_formular add stufe_2_voraussichtliche_betrag decimal(19,2);
alter table rueckforderung_formular_aud add stufe_2_voraussichtliche_betrag decimal(19,2);

CREATE TABLE generated_notrecht_dokument_aud (
	id                 BINARY(16) NOT NULL,
	rev                INTEGER    NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(255),
	user_mutiert       VARCHAR(255),
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255),
	filepfad           VARCHAR(4000),
	filesize           VARCHAR(255),
	typ                VARCHAR(255),
	write_protected    BIT,
	rueckforderung_formular_id          BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE generated_notrecht_dokument (
	id                 BINARY(16)    NOT NULL,
	timestamp_erstellt DATETIME      NOT NULL,
	timestamp_mutiert  DATETIME      NOT NULL,
	user_erstellt      VARCHAR(255)  NOT NULL,
	user_mutiert       VARCHAR(255)  NOT NULL,
	version            BIGINT        NOT NULL,
	vorgaenger_id      VARCHAR(36),
	filename           VARCHAR(255)  NOT NULL,
	filepfad           VARCHAR(4000) NOT NULL,
	filesize           VARCHAR(255)  NOT NULL,
	typ                VARCHAR(255)  NOT NULL,
	write_protected    BIT           NOT NULL,
	rueckforderung_formular_id          BINARY(16)    NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE generated_notrecht_dokument_aud
	ADD CONSTRAINT FK_generated_notrecht_dokument_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo(rev);

ALTER TABLE generated_notrecht_dokument
	ADD CONSTRAINT FK_generated_dokument_rueckforderung_formular_id
FOREIGN KEY (rueckforderung_formular_id)
REFERENCES rueckforderung_formular(id);
