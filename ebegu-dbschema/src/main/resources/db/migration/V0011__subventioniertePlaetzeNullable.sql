ALTER TABLE institution_stammdaten MODIFY COLUMN anzahl_plaetze decimal(19,2);
ALTER TABLE institution_stammdaten_aud MODIFY COLUMN anzahl_plaetze decimal(19,2);
UPDATE institution_stammdaten SET anzahl_plaetze = NULL WHERE betreuungsangebot_typ = 'TAGESFAMILIEN';
UPDATE institution_stammdaten_aud SET anzahl_plaetze = NULL WHERE betreuungsangebot_typ = 'TAGESFAMILIEN';