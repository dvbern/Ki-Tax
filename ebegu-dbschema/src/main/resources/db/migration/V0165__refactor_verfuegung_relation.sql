DROP TABLE verfuegung;

DROP TABLE verfuegung_aud;


DROP TABLE verfuegung_zeitabschnitt_aud;
DROP TABLE verfuegung_zeitabschnitt;


CREATE TABLE verfuegung (
	timestamp_erstellt        DATETIME     NOT NULL,
	timestamp_mutiert         DATETIME     NOT NULL,
	user_erstellt             VARCHAR(255) NOT NULL,
	user_mutiert              VARCHAR(255) NOT NULL,
	version                   BIGINT       NOT NULL,
	vorgaenger_id             VARCHAR(36),
	generated_bemerkungen     VARCHAR(4000),
	kategorie_kein_pensum     BIT          NOT NULL,
	kategorie_max_einkommen   BIT          NOT NULL,
	kategorie_nicht_eintreten BIT          NOT NULL,
	kategorie_normal          BIT          NOT NULL,
	manuelle_bemerkungen      VARCHAR(4000),
	betreuung_id              VARCHAR(36)  NOT NULL,
	PRIMARY KEY (betreuung_id)
);

CREATE TABLE verfuegung_aud (
	betreuung_id              VARCHAR(36) NOT NULL,
	rev                       INTEGER     NOT NULL,
	revtype                   TINYINT,
	timestamp_erstellt        DATETIME,
	timestamp_mutiert         DATETIME,
	user_erstellt             VARCHAR(255),
	user_mutiert              VARCHAR(255),
	vorgaenger_id             VARCHAR(36),
	generated_bemerkungen     VARCHAR(4000),
	kategorie_kein_pensum     BIT,
	kategorie_max_einkommen   BIT,
	kategorie_nicht_eintreten BIT,
	kategorie_normal          BIT,
	manuelle_bemerkungen      VARCHAR(4000),
	PRIMARY KEY (betreuung_id, rev)
);


CREATE TABLE verfuegung_zeitabschnitt_aud (
	id                                                   VARCHAR(36) NOT NULL,
	rev                                                  INTEGER     NOT NULL,
	revtype                                              TINYINT,
	timestamp_erstellt                                   DATETIME,
	timestamp_mutiert                                    DATETIME,
	user_erstellt                                        VARCHAR(255),
	user_mutiert                                         VARCHAR(255),
	vorgaenger_id                                        VARCHAR(36),
	gueltig_ab                                           DATE,
	gueltig_bis                                          DATE,
	abzug_fam_groesse                                    DECIMAL(19, 2),
	anspruchberechtigtes_pensum                          INTEGER,
	bemerkungen                                          VARCHAR(4000),
	betreuungspensum                                     DECIMAL(19, 2),
	betreuungsstunden                                    DECIMAL(19, 2),
	einkommensjahr                                       INTEGER,
	elternbeitrag                                        DECIMAL(19, 2),
	fam_groesse                                          DECIMAL(19, 2),
	massgebendes_einkommen_vor_abzug_famgr               DECIMAL(19, 2),
	minimaler_elternbeitrag                              DECIMAL(19, 2),
	minimales_ewp_unterschritten                         BIT,
	verguenstigung                                       DECIMAL(19, 2),
	verguenstigung_ohne_beruecksichtigung_minimalbeitrag DECIMAL(19, 2),
	verguenstigung_ohne_beruecksichtigung_vollkosten     DECIMAL(19, 2),
	vollkosten                                           DECIMAL(19, 2),
	zahlungsstatus                                       VARCHAR(255),
	zu_spaet_eingereicht                                 BIT,
	verfuegung_betreuung_id                              VARCHAR(36),
	PRIMARY KEY (id, rev)
);

CREATE TABLE verfuegung_zeitabschnitt (
	id                                                   VARCHAR(36)    NOT NULL,
	timestamp_erstellt                                   DATETIME       NOT NULL,
	timestamp_mutiert                                    DATETIME       NOT NULL,
	user_erstellt                                        VARCHAR(255)   NOT NULL,
	user_mutiert                                         VARCHAR(255)   NOT NULL,
	version                                              BIGINT         NOT NULL,
	vorgaenger_id                                        VARCHAR(36),
	gueltig_ab                                           DATE           NOT NULL,
	gueltig_bis                                          DATE           NOT NULL,
	abzug_fam_groesse                                    DECIMAL(19, 2),
	anspruchberechtigtes_pensum                          INTEGER        NOT NULL,
	bemerkungen                                          VARCHAR(4000),
	betreuungspensum                                     DECIMAL(19, 2) NOT NULL,
	betreuungsstunden                                    DECIMAL(19, 2),
	einkommensjahr                                       INTEGER        NOT NULL,
	elternbeitrag                                        DECIMAL(19, 2),
	fam_groesse                                          DECIMAL(19, 2),
	massgebendes_einkommen_vor_abzug_famgr               DECIMAL(19, 2),
	minimaler_elternbeitrag                              DECIMAL(19, 2),
	minimales_ewp_unterschritten                         BIT            NOT NULL,
	verguenstigung                                       DECIMAL(19, 2),
	verguenstigung_ohne_beruecksichtigung_minimalbeitrag DECIMAL(19, 2),
	verguenstigung_ohne_beruecksichtigung_vollkosten     DECIMAL(19, 2),
	vollkosten                                           DECIMAL(19, 2),
	zahlungsstatus                                       VARCHAR(255)   NOT NULL,
	zu_spaet_eingereicht                                 BIT            NOT NULL,
	verfuegung_betreuung_id                              VARCHAR(36)    NOT NULL,
	PRIMARY KEY (id)
);


ALTER TABLE verfuegung
	ADD CONSTRAINT FK_verfuegung_betreuung_id
		FOREIGN KEY (betreuung_id)
			REFERENCES betreuung(id);

ALTER TABLE verfuegung_aud
	ADD CONSTRAINT FK_verfuegung_aud_rev
		FOREIGN KEY (rev)
			REFERENCES revinfo(rev);




ALTER TABLE betreuung
	DROP COLUMN verfuegung_id;



ALTER TABLE verfuegung_zeitabschnitt
	ADD CONSTRAINT FK_verfuegung_zeitabschnitt_verfuegung_id
		FOREIGN KEY (verfuegung_betreuung_id)
			REFERENCES verfuegung(betreuung_id);


ALTER TABLE verfuegung_zeitabschnitt_aud
	ADD CONSTRAINT FK_verfuegung_zeitabschnitt_aud_rev
		FOREIGN KEY (rev)
			REFERENCES revinfo(rev);