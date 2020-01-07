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
	betreuungsstunden_pro_woche                          DECIMAL(3,0),
	betreuungsminuten_pro_woche                          DECIMAL(2,0),
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
	betreuungsstunden_pro_woche                          DECIMAL(3,0) 	NOT NULL,
	betreuungsminuten_pro_woche                          DECIMAL(3,0) 	NOT NULL,
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

INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id) VALUES (UNHEX(REPLACE('068e40b2-24a8-11e9-8aa5-cbeb395a1a75', '-','')), '2019-12-11 09:10:38', '2019-12-11 09:10:38', 'flyway', 'flyway', 0, 'MAX_TARIF_MIT_PAEDAGOGISCHER_BETREUUNG', '12.24', null, UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-','')), null);
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id) VALUES (UNHEX(REPLACE('068e40b2-24a8-11e9-8aa5-cbeb395a1a76', '-','')), '2019-12-11 09:10:38', '2019-12-11 09:10:38', 'flyway', 'flyway', 0, 'MAX_TARIF_OHNE_PAEDAGOGISCHER_BETREUUNG', '6.11', null, UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-','')), null);
INSERT INTO einstellung (id, timestamp_erstellt, timestamp_mutiert, user_erstellt, user_mutiert, version, einstellung_key, value, gemeinde_id, gesuchsperiode_id, mandant_id) VALUES (UNHEX(REPLACE('068e40b2-24a8-11e9-8aa5-cbeb395a1a78', '-','')), '2019-12-11 09:10:38', '2019-12-11 09:10:38', 'flyway', 'flyway', 0, 'MIN_TARIF', '0.78', null, UNHEX(REPLACE('0621fb5d-a187-5a91-abaf-8a813c4d263a', '-','')), null);