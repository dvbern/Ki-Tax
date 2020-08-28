ALTER TABLE institution_stammdaten_betreuungsgutscheine
	ADD COLUMN oeffnungs_abweichungen VARCHAR(4000),
	ADD COLUMN offen_bis              TIME,
	ADD COLUMN offen_von              TIME;

ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud
	ADD COLUMN oeffnungs_abweichungen VARCHAR(4000),
	ADD COLUMN offen_bis              TIME,
	ADD COLUMN offen_von              TIME;

CREATE TABLE institution_stammdaten_betreuungsgutscheine_oeffnungstag (
	insitution_stammdaten_betreuungsgutscheine BINARY(16) NOT NULL,
	oeffnungstage                              VARCHAR(255)
);

CREATE TABLE institution_stammdaten_betreuungsgutscheine_oeffnungstag_aud (
	rev                                        INTEGER      NOT NULL,
	insitution_stammdaten_betreuungsgutscheine BINARY(16)   NOT NULL,
	oeffnungstage                              VARCHAR(255) NOT NULL,
	revtype                                    TINYINT,
	PRIMARY KEY (rev, insitution_stammdaten_betreuungsgutscheine, oeffnungstage)
);

CREATE TABLE betreuungsstandort_aud (
	id                                             BINARY(16) NOT NULL,
	rev                                            INTEGER    NOT NULL,
	revtype                                        TINYINT,
	timestamp_erstellt                             DATETIME,
	timestamp_mutiert                              DATETIME,
	user_erstellt                                  VARCHAR(255),
	user_mutiert                                   VARCHAR(255),
	vorgaenger_id                                  VARCHAR(36),
	mail                                           VARCHAR(255),
	telefon                                        VARCHAR(255),
	webseite                                       VARCHAR(255),
	adresse_id                                     BINARY(16),
	institution_stammdaten_betreuungsgutscheine_id BINARY(16),
	PRIMARY KEY (id, rev)
);

ALTER TABLE institution_stammdaten_betreuungsgutscheine_oeffnungstag_aud
	ADD CONSTRAINT FK_stammdaten_oeffnungstag_aud_rev_info
		FOREIGN KEY (rev)
			REFERENCES revinfo(rev);

ALTER TABLE institution_stammdaten_betreuungsgutscheine_oeffnungstag
	ADD CONSTRAINT FK_stammdaten_oeffnungstag_institution_stammdaten_bg
		FOREIGN KEY (insitution_stammdaten_betreuungsgutscheine)
			REFERENCES institution_stammdaten_betreuungsgutscheine(id);

CREATE TABLE betreuungsstandort (
	id                                             BINARY(16)   NOT NULL,
	timestamp_erstellt                             DATETIME     NOT NULL,
	timestamp_mutiert                              DATETIME     NOT NULL,
	user_erstellt                                  VARCHAR(255) NOT NULL,
	user_mutiert                                   VARCHAR(255) NOT NULL,
	version                                        BIGINT       NOT NULL,
	vorgaenger_id                                  VARCHAR(36),
	mail                                           VARCHAR(255),
	telefon                                        VARCHAR(255),
	webseite                                       VARCHAR(255),
	adresse_id                                     BINARY(16)   NOT NULL,
	institution_stammdaten_betreuungsgutscheine_id BINARY(16)   NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE betreuungsstandort
	ADD CONSTRAINT UK_betreuungsstandort_adresse UNIQUE (adresse_id);

ALTER TABLE betreuungsstandort
	ADD CONSTRAINT FK_betreuungsstandort_adresse_id
		FOREIGN KEY (adresse_id)
			REFERENCES adresse(id);

ALTER TABLE betreuungsstandort
	ADD CONSTRAINT FK_betreuungsstandort_betreuungsgutscheine_id
		FOREIGN KEY (institution_stammdaten_betreuungsgutscheine_id)
			REFERENCES institution_stammdaten_betreuungsgutscheine(id);

ALTER TABLE betreuungsstandort
	ADD CONSTRAINT FK_institution_stammdaten_bg_adresse_id
		FOREIGN KEY (adresse_id)
			REFERENCES adresse(id);
