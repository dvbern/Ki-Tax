ALTER TABLE institution_stammdaten MODIFY COLUMN anzahl_plaetze decimal(19,2);
UPDATE institution_stammdaten
SET anzahl_plaetze = NULL, anzahl_plaetze_firmen = NULL, subventionierte_plaetze = false
WHERE betreuungsangebot_typ = 'TAGESFAMILIEN';
