ALTER TABLE verfuegung_zeitabschnitt DROP FOREIGN KEY FK_verfuegung_zeitabschnitt_verfuegung_id;
ALTER TABLE verfuegung DROP FOREIGN KEY FK_verfuegung_betreuung_id;

ALTER TABLE verfuegung ADD id BINARY(16);
UPDATE verfuegung SET id = betreuung_id;
ALTER TABLE verfuegung MODIFY id BINARY(16) NOT NULL;

ALTER TABLE verfuegung ADD anmeldung_tagesschule_id BINARY(16);

ALTER TABLE verfuegung DROP betreuung_id;
ALTER TABLE verfuegung ADD betreuung_id BINARY(16);
UPDATE verfuegung SET betreuung_id = id;

ALTER TABLE verfuegung ADD PRIMARY KEY (id);


ALTER TABLE verfuegung_aud ADD id BINARY(16);
UPDATE verfuegung_aud SET id = betreuung_id;
ALTER TABLE verfuegung_aud MODIFY id BINARY(16) NOT NULL;

ALTER TABLE verfuegung_aud ADD anmeldung_tagesschule_id BINARY(16);

ALTER TABLE verfuegung_aud MODIFY betreuung_id BINARY(16);

ALTER TABLE verfuegung_aud DROP PRIMARY KEY;
ALTER TABLE verfuegung_aud ADD PRIMARY KEY (id, rev);

ALTER TABLE verfuegung
	ADD CONSTRAINT FK_verfuegung_anmeldungTagesschule_id
FOREIGN KEY (anmeldung_tagesschule_id)
REFERENCES anmeldung_tagesschule (id);

ALTER TABLE verfuegung
	ADD CONSTRAINT FK_verfuegung_betreuung_id
FOREIGN KEY (betreuung_id)
REFERENCES betreuung(id);

ALTER TABLE verfuegung_zeitabschnitt CHANGE verfuegung_betreuung_id verfuegung_id BINARY(16) NOT NULL;