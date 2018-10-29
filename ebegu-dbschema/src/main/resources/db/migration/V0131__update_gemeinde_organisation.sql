UPDATE adresse a
SET a.organisation = 'Jugendamt', a.strasse = 'Effingerstrasse', a.hausnummer = '21', a.plz = 3008
WHERE a.strasse = 'Predigergasse' AND a.plz = 3001 AND a.hausnummer = 5;

UPDATE adresse a
SET a.organisation = 'Gemeinde'
WHERE a.strasse = 'Schiessplatzweg' AND a.plz = 3072 AND a.hausnummer = 1;
