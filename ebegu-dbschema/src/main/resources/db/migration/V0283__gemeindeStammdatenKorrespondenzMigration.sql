# Fuer die Migration muss zuerst die GemeindeStammdatenId zwischengespeichert werden
ALTER TABLE gemeinde_stammdaten_korrespondenz ADD tmpIdStammdaten BINARY(16);

# Daten migrieren (Logo-Daten aus Stammdaten uebernehmen, neue Konfigurationen mit Defaults abfuellen)
INSERT INTO gemeinde_stammdaten_korrespondenz (
		   id,
		   timestamp_erstellt,
		   timestamp_mutiert,
		   user_erstellt,
		   user_mutiert,
		   version,
		   logo_content,
		   logo_name,
		   logo_spacing_left,
		   logo_spacing_top,
		   logo_type,
		   logo_width,
		   receiver_address_spacing_left,
		   receiver_address_spacing_top,
		   sender_address_spacing_left,
		   sender_address_spacing_top,
		   tmpIdStammdaten)
	(
		SELECT UNHEX(REPLACE(UUID(), '-', '')) AS id,
			NOW() AS timestamp_erstellt,
			NOW() AS timestamp_muiert,
			'ebegu' AS user_erstellt,
			'ebegu' AS user_mutiert,
			'0' AS version,
			logo_content AS logo_content,
			logo_name AS logo_name,
			123 AS logo_spacing_left,
			15 AS logo_spacing_top,
			logo_type AS logo_type,
			NULL AS logo_width,
			123 AS receiver_address_spacing_left,
			47 AS receiver_address_spacing_top,
			20 AS sender_address_spacing_left,
			47 AS sender_address_spacing_top,
			id AS tmpIdStammdaten
		FROM gemeinde_stammdaten
	);

#  Die Verknuepfung auf den GemeindeStammdaten setzen
UPDATE gemeinde_stammdaten gs
SET gs.gemeinde_stammdaten_korrespondenz_id = (
	SELECT gsk.id FROM gemeinde_stammdaten_korrespondenz gsk
	WHERE gsk.tmpIdStammdaten = gs.id
);

# Jetzt kann GemeindeStammdatenKorrespondenz auf required gesetzt werden
ALTER TABLE gemeinde_stammdaten
CHANGE gemeinde_stammdaten_korrespondenz_id gemeinde_stammdaten_korrespondenz_id BINARY(16) NOT NULL;

# Jetzt kann das temporaere Id-Feld auf der GemeindeStammdatenKorrespondenz wieder entfernt werden
ALTER TABLE gemeinde_stammdaten_korrespondenz DROP tmpIdStammdaten;

# Die migrierten Felder des Gemeinde-Logos auf GemeindeStammdaten entfernen
ALTER TABLE gemeinde_stammdaten DROP logo_content;
ALTER TABLE gemeinde_stammdaten DROP logo_type;
ALTER TABLE gemeinde_stammdaten DROP logo_name;

ALTER TABLE gemeinde_stammdaten_aud DROP logo_content;
ALTER TABLE gemeinde_stammdaten_aud DROP logo_type;
ALTER TABLE gemeinde_stammdaten_aud DROP logo_name;