ALTER TABLE verfuegung_zeitabschnitt ADD regelwerk VARCHAR(255) NOT NULL;
ALTER TABLE verfuegung_zeitabschnitt_aud ADD regelwerk VARCHAR(255);

UPDATE verfuegung_zeitabschnitt SET regelwerk = 'ASIV';
