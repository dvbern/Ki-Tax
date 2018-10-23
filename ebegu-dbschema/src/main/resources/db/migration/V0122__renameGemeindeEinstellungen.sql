# Präfix für Gemeinde Einstellungen 'GEMEINDE_'
UPDATE einstellung SET einstellung_key = 'GEMEINDE_KONTINGENTIERUNG_ENABLED' WHERE einstellung_key = 'KONTINGENTIERUNG_ENABLED';
UPDATE einstellung SET einstellung_key = 'GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE' WHERE einstellung_key = 'BG_BIS_UND_MIT_SCHULSTUFE';
# Boolean Spalte ist nicht nötig, kann über das Existieren der Beschwerdeadresse ermittelt werden
ALTER TABLE gemeinde_stammdaten	DROP COLUMN keine_beschwerde_adresse;

