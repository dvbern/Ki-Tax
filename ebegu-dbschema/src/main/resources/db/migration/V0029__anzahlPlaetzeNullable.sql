ALTER TABLE institution_stammdaten_betreuungsgutscheine MODIFY COLUMN anzahl_plaetze decimal(19,2);

UPDATE institution_stammdaten_betreuungsgutscheine SET anzahl_plaetze = NULL WHERE anzahl_plaetze = 0;