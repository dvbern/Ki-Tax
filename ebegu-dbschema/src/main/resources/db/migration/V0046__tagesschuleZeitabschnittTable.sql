CREATE TABLE anmeldung_tagesschule_zeitabschnitt_aud (
	id                                                   BINARY(16) NOT NULL,
	rev                                                  INTEGER     NOT NULL,
	revtype                                              TINYINT,
	timestamp_erstellt                                   DATETIME,
	timestamp_mutiert                                    DATETIME,
	user_erstellt                                        VARCHAR(255),
	user_mutiert                                         VARCHAR(255),
	vorgaenger_id                                        VARCHAR(36),
	gueltig_ab                                           DATE,
	gueltig_bis                                          DATE,
	massgebendes_einkommen_inkl_abzug_famgr              DECIMAL(19, 2),
	verpflegungskosten                                   DECIMAL(19, 2),
	betreuungsstunden_pro_woche                          DECIMAL(19, 2),
	gebuehr_pro_stunde                                   DECIMAL(5, 2),
	total_kosten_pro_woche               				 DECIMAL(19, 2),
	pedagogisch_betreut	                                 BIT,
	anmeldung_tagesschule_id                	 		 BINARY(16),
	PRIMARY KEY (id, rev)
);

CREATE TABLE anmeldung_tagesschule_zeitabschnitt (
	id                                                   BINARY(16)     NOT NULL,
	timestamp_erstellt                                   DATETIME       NOT NULL,
	timestamp_mutiert                                    DATETIME       NOT NULL,
	user_erstellt                                        VARCHAR(255)   NOT NULL,
	user_mutiert                                         VARCHAR(255)   NOT NULL,
	version                                              BIGINT         NOT NULL,
	vorgaenger_id                                        VARCHAR(36),
	gueltig_ab                                           DATE           NOT NULL,
	gueltig_bis                                          DATE           NOT NULL,
	massgebendes_einkommen_inkl_abzug_famgr              DECIMAL(19, 2) NOT NULL,
	verpflegungskosten                                   DECIMAL(19, 2) NOT NULL,
	betreuungsstunden_pro_woche                          DECIMAL(19, 2) NOT NULL,
	gebuehr_pro_stunde                                   DECIMAL(5, 2)  NOT NULL,
    total_kosten_pro_woche               				 DECIMAL(19, 2) NOT NULL,
	pedagogisch_betreut                                  BIT            NOT NULL,
	anmeldung_tagesschule_id                  		     BINARY(16)     NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE anmeldung_tagesschule_zeitabschnitt
	ADD CONSTRAINT FK_anmeldung_tagesschule_id
FOREIGN KEY (anmeldung_tagesschule_id)
REFERENCES anmeldung_tagesschule(id);