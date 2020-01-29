#Foreing Keys abhaengen
ALTER TABLE verfuegung_zeitabschnitt DROP FOREIGN KEY FK_verfuegung_zeitabschnitt_verfuegung_id;
ALTER TABLE verfuegung DROP FOREIGN KEY FK_verfuegung_betreuung_id;

# umbennen der id column
# ALTER TABLE verfuegung ADD id BINARY(16);
# UPDATE verfuegung SET id = betreuung_id;
# ALTER TABLE verfuegung MODIFY id BINARY(16) NOT NULL;

ALTER TABLE verfuegung CHANGE betreuung_id id BINARY(16) NOT NULL;


#einfuegen der neuen  columns fuer die FKs (nullable)
ALTER TABLE verfuegung ADD betreuung_id BINARY(16);
UPDATE verfuegung SET  betreuung_id = id;
ALTER TABLE verfuegung ADD anmeldung_tagesschule_id BINARY(16);

# todo homa: verstehe ich nicht
# ALTER TABLE verfuegung DROP id;
# ALTER TABLE verfuegung ADD betreuung_id BINARY(16);
# UPDATE verfuegung SET id = id;

# todo homa sollte nicht noetig sein
#ALTER TABLE verfuegung ADD PRIMARY KEY (id);


# FKs neu definieren und einfuegen
ALTER TABLE verfuegung
	ADD CONSTRAINT FK_verfuegung_anmeldungTagesschule_id
FOREIGN KEY (anmeldung_tagesschule_id)
REFERENCES anmeldung_tagesschule (id);

ALTER TABLE verfuegung
	ADD CONSTRAINT FK_verfuegung_betreuung_id
FOREIGN KEY (betreuung_id)
REFERENCES betreuung(id);


ALTER TABLE verfuegung_zeitabschnitt CHANGE verfuegung_betreuung_id verfuegung_id BINARY(16) NOT NULL;

ALTER TABLE verfuegung_zeitabschnitt
	ADD CONSTRAINT FK_verfuegung_zeitabschnitt_verfuegung_id
FOREIGN KEY (verfuegung_id)
REFERENCES verfuegung (id);


# AUD Tabelle fixen (rename betreuung_id to id to keep pk on same column)
ALTER TABLE verfuegung_aud CHANGE betreuung_id id BINARY(16) NOT NULL;
ALTER TABLE verfuegung_aud ADD betreuung_id BINARY(16);
UPDATE verfuegung_aud SET betreuung_id = id;


ALTER TABLE verfuegung_aud ADD anmeldung_tagesschule_id BINARY(16);

# todo homa verstehe ich nicht
# ALTER TABLE verfuegung_aud DROP betreuung_id;
# ALTER TABLE verfuegung_aud ADD betreuung_id BINARY(16);
# UPDATE verfuegung_aud SET betreuung_id = id;

# ALTER TABLE verfuegung_aud DROP PRIMARY KEY;
# ALTER TABLE verfuegung_aud ADD PRIMARY KEY (id, rev);




ALTER TABLE verfuegung_zeitabschnitt_aud CHANGE verfuegung_betreuung_id verfuegung_id BINARY(16);
