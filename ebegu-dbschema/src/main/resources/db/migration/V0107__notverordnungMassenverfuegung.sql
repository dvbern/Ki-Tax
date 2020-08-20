CREATE TABLE generated_general_dokument (
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
	identifier         VARCHAR(255)  NOT NULL,
	PRIMARY KEY (id)
);