CREATE TABLE unbezahlter_urlaub_aud (
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
	PRIMARY KEY (id, rev)
);

CREATE TABLE unbezahlter_urlaub (
	id                 VARCHAR(36)  NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(255) NOT NULL,
	user_mutiert       VARCHAR(255) NOT NULL,
	version            BIGINT       NOT NULL,
	vorgaenger_id      VARCHAR(36),
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE erwerbspensum ADD unbezahlter_urlaub_id VARCHAR(36);
ALTER TABLE erwerbspensum_aud ADD unbezahlter_urlaub_id VARCHAR(36);

ALTER TABLE erwerbspensum
	ADD CONSTRAINT FK_erwerbspensum_urlaub_id
FOREIGN KEY (unbezahlter_urlaub_id)
REFERENCES unbezahlter_urlaub (id);

ALTER TABLE unbezahlter_urlaub_aud
	ADD CONSTRAINT FK10yopvhxm14yekil7pcuckq9k
FOREIGN KEY (rev)
REFERENCES revinfo (rev);