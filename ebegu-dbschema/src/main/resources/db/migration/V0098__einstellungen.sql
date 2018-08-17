CREATE TABLE einstellung (
	id                 VARCHAR(36)  NOT NULL,
	timestamp_erstellt DATETIME     NOT NULL,
	timestamp_mutiert  DATETIME     NOT NULL,
	user_erstellt      VARCHAR(36)  NOT NULL,
	user_mutiert       VARCHAR(36)  NOT NULL,
	version            BIGINT       NOT NULL,
	gueltig_ab         DATE         NOT NULL,
	gueltig_bis        DATE         NOT NULL,
	einstellung_key    VARCHAR(255) NOT NULL,
	value              VARCHAR(255) NOT NULL,
	gemeinde_id        VARCHAR(36),
	gesuchsperiode_id  VARCHAR(36)  NOT NULL,
	mandant_id         VARCHAR(36),
	PRIMARY KEY (id)
);

CREATE TABLE einstellung_aud (
	id                 VARCHAR(36) NOT NULL,
	rev                INTEGER     NOT NULL,
	revtype            TINYINT,
	timestamp_erstellt DATETIME,
	timestamp_mutiert  DATETIME,
	user_erstellt      VARCHAR(36),
	user_mutiert       VARCHAR(36),
	gueltig_ab         DATE,
	gueltig_bis        DATE,
	einstellung_key    VARCHAR(255),
	value              VARCHAR(255),
	gemeinde_id        VARCHAR(36),
	gesuchsperiode_id  VARCHAR(36),
	mandant_id         VARCHAR(36),
	PRIMARY KEY (id, rev)
);

ALTER TABLE einstellung
	ADD CONSTRAINT UK_einstellung UNIQUE (einstellung_key, gesuchsperiode_id, mandant_id, gemeinde_id);

ALTER TABLE einstellung
	ADD CONSTRAINT FK_einstellung_gemeinde_id
FOREIGN KEY (gemeinde_id)
REFERENCES gemeinde (id);

ALTER TABLE einstellung
	ADD CONSTRAINT FK_einstellung_gesuchsperiode_id
FOREIGN KEY (gesuchsperiode_id)
REFERENCES gesuchsperiode (id);

ALTER TABLE einstellung
	ADD CONSTRAINT FK_einstellung_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant (id);

ALTER TABLE einstellung_aud
	ADD CONSTRAINT FK_einstellung_aud_revinfo
FOREIGN KEY (rev)
REFERENCES revinfo (rev);