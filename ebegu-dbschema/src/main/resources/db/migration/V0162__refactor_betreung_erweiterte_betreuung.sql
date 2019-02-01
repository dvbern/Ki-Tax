# drop FK_betreuung_verfuegung_id"
# drop uc ohne namen ...
# drop feld betreuung.verfuegung_id und add verfung.betreuung_id

ALTER TABLE erweiterte_betreuung_container
	DROP FOREIGN KEY FK_erweiterte_betreuung_container_betreuung_id;

alter table erweiterte_betreuung_container drop key UK_erweiterte_betreuung_betreuung;


ALTER TABLE erweiterte_betreuung_container_aud
	DROP FOREIGN KEY FK_erweiterte_betreuung_container_aud_revinfo;

DROP TABLE erweiterte_betreuung_container_aud;

CREATE TABLE erweiterte_betreuung_container_aud (
	betreuung_id              VARCHAR(36) NOT NULL,
	rev                       INTEGER     NOT NULL,
	revtype                   TINYINT,
	timestamp_erstellt        DATETIME,
	timestamp_mutiert         DATETIME,
	user_erstellt             VARCHAR(255),
	user_mutiert              VARCHAR(255),
	vorgaenger_id             VARCHAR(36),
	erweiterte_betreuunggs_id VARCHAR(36),
	erweiterte_betreuungja_id VARCHAR(36),
	PRIMARY KEY (betreuung_id, rev)
);




ALTER TABLE erweiterte_betreuung_container_aud
	ADD CONSTRAINT FK_erweiterte_betreuung_container_aud_revinfo
		FOREIGN KEY (rev)
			REFERENCES revinfo(rev);



alter table erweiterte_betreuung_container drop primary key;

alter table erweiterte_betreuung_container drop column id;


alter table erweiterte_betreuung_container modify betreuung_id varchar(36) not null;

ALTER TABLE erweiterte_betreuung_container
ADD CONSTRAINT FK_erweiterte_betreuung_container_betreuung_id
FOREIGN KEY (betreuung_id)
REFERENCES betreuung(id);







