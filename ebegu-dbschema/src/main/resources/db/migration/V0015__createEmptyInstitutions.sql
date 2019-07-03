# This inserts where in an external script, which has already been executed in some servers. In those servers we cannot insert these items again because
# we get a duplicate error. We insert them then only if they don't exist what we check with "where not exists..."

# institution 1
INSERT INTO institution(id,
						timestamp_erstellt,
						timestamp_mutiert,
						user_erstellt,
						user_mutiert,
						version,
						vorgaenger_id,
						name,
						mandant_id,
						traegerschaft_id,
						status)
	SELECT * FROM (SELECT
	      UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', '')) as id,
		  '2016-01-01 00:00:00' as timestamp_erstellt,
		  '2016-01-01 00:00:00' as timestamp_mutiert,
		  'flyway' as user_erstellt,
		  'flyway' as user_mutiert,
		  0 as version,
		  null as vorgaenger_id,
		  '' as name,
		  UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')) as mandant_id,
		  null as traegerschaft_id,
		  'AKTIV' as status
	) AS tmp
	WHERE NOT EXISTS(
			SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))
		)
	LIMIT 1;

# Institution 2
INSERT INTO institution(id,
						timestamp_erstellt,
						timestamp_mutiert,
						user_erstellt,
						user_mutiert,
						version,
						vorgaenger_id,
						name,
						mandant_id,
						traegerschaft_id,
						status)
	SELECT * FROM (SELECT
					   UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')) as id,
					   '2016-01-01 00:00:00' as timestamp_erstellt,
					   '2016-01-01 00:00:00' as timestamp_mutiert,
					   'flyway' as user_erstellt,
					   'flyway' as user_mutiert,
					   0 as version,
					   null as vorgaenger_id,
					   '' as name,
					   UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-', '')) as mandant_id,
					   null as traegerschaft_id,
					   'AKTIV' as status
	) AS tmp
	WHERE NOT EXISTS(
			SELECT id FROM institution WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', ''))
		)
	LIMIT 1;



# Address1
INSERT INTO adresse (id,
                     timestamp_erstellt,
                     timestamp_mutiert,
                     user_erstellt,
                     user_mutiert,
                     version,
                     vorgaenger_id,
                     gueltig_ab,
                     gueltig_bis,
                     gemeinde,
                     hausnummer,
                     land,
                     organisation,
                     ort,
                     plz,
                     strasse,
                     zusatzzeile)
	SELECT * FROM (SELECT
					   UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', '')) as id,
					   '2016-01-01 00:00:00' as timestamp_erstellt,
					   '2016-01-01 00:00:00' as timestamp_mutiert,
					   'flyway' as user_erstellt,
					   'flyway' as user_mutiert,
					   0 as version,
					   null as vorgaenger_id,
					   '1000-01-01' as gueltig_ab,
					   '9999-12-31' as gueltig_bis,
					   null as gemeinde,
					   '21.0' as hausnummer,
					   'CH' as land,
					   null as organisation,
					   'Bern' as ort,
					   '3022.0' as plz,
					   'Nussbaumstrasse' as strasse,
					   null as zusatzzeile
	) AS tmp
	WHERE NOT EXISTS(
			SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))
		)
	LIMIT 1;

# Address2
INSERT INTO adresse (id,
					 timestamp_erstellt,
					 timestamp_mutiert,
					 user_erstellt,
					 user_mutiert,
					 version,
					 vorgaenger_id,
					 gueltig_ab,
					 gueltig_bis,
					 gemeinde,
					 hausnummer,
					 land,
					 organisation,
					 ort,
					 plz,
					 strasse,
					 zusatzzeile)
	SELECT * FROM (SELECT
					   UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')) as id,
					   '2016-01-01 00:00:00' as timestamp_erstellt,
					   '2016-01-01 00:00:00' as timestamp_mutiert,
					   'flyway' as user_erstellt,
					   'flyway' as user_mutiert,
					   0 as version,
					   null as vorgaenger_id,
					   '1000-01-01' as gueltig_ab,
					   '9999-12-31' as gueltig_bis,
					   null as gemeinde,
					   '21.0' as hausnummer,
					   'CH' as land,
					   null as organisation,
					   'Bern' as ort,
					   '3022.0' as plz,
					   'Nussbaumstrasse' as strasse,
					   null as zusatzzeile
	) AS tmp
	WHERE NOT EXISTS(
			SELECT id FROM adresse WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', ''))
		)
	LIMIT 1;



# InstitutionStammdate1
INSERT INTO institution_stammdaten (id,
                     timestamp_erstellt,
                     timestamp_mutiert,
                     user_erstellt,
                     user_mutiert,
                     version,
                     vorgaenger_id,
                     gueltig_ab,
                     gueltig_bis,
                     betreuungsangebot_typ,
                     iban,
                     adresse_id,
                     institution_id,
                     kontoinhaber,
                     adresse_kontoinhaber_id,
                     institution_stammdaten_tagesschule_id,
                     institution_stammdaten_ferieninsel_id,
                     mail,
                     telefon,
                     webseite,
                     oeffnungszeiten,
                     alterskategorie_baby,
                     alterskategorie_vorschule,
                     alterskategorie_kindergarten,
                     alterskategorie_schule,
                     subventionierte_plaetze,
                     anzahl_plaetze,
                     anzahl_plaetze_firmen)
	SELECT * FROM (SELECT
					   UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', '')) as id,
					   '2016-01-01 00:00:00' as timestamp_erstellt,
					   '2016-01-01 00:00:00' as timestamp_mutiert,
					   'flyway' as user_erstellt,
					   'flyway' as user_mutiert,
					   0 as version,
					   null as vorgaenger_id,
					   '1000-01-01' as gueltig_ab,
					   '9999-12-31' as gueltig_bis,
					   'KITA' as betreuungsangebot_typ,
					   null as iban,
					   UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', '')) as adresse_id,
					   UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', '')) as institution_id,
					   null as kontoinhaber,
					   null as adresse_kontoinhaber_id,
					   null as institution_stammdaten_tagesschule_id,
					   null as institution_stammdaten_ferieninsel_id,
					   'mail@example.com' as mail,
					   null as telefon,
					   null as webseite,
					   null as oeffnungszeiten,
					   false as alterskategorie_baby,
					   false as alterskategorie_vorschule,
					   false as alterskategorie_kindergarten,
					   false as alterskategorie_schule,
					   false as subventionierte_plaetze,
					   0.00 as anzahl_plaetze,
					   null as anzahl_plaetze_firmen
	) AS tmp
	WHERE NOT EXISTS(
			SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000000', '-', ''))
		)
	LIMIT 1;

# InstitutionStammdate1
INSERT INTO institution_stammdaten (id,
									timestamp_erstellt,
									timestamp_mutiert,
									user_erstellt,
									user_mutiert,
									version,
									vorgaenger_id,
									gueltig_ab,
									gueltig_bis,
									betreuungsangebot_typ,
									iban,
									adresse_id,
									institution_id,
									kontoinhaber,
									adresse_kontoinhaber_id,
									institution_stammdaten_tagesschule_id,
									institution_stammdaten_ferieninsel_id,
									mail,
									telefon,
									webseite,
									oeffnungszeiten,
									alterskategorie_baby,
									alterskategorie_vorschule,
									alterskategorie_kindergarten,
									alterskategorie_schule,
									subventionierte_plaetze,
									anzahl_plaetze,
									anzahl_plaetze_firmen)
	SELECT * FROM (SELECT
					   UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')) as id,
					   '2016-01-01 00:00:00' as timestamp_erstellt,
					   '2016-01-01 00:00:00' as timestamp_mutiert,
					   'flyway' as user_erstellt,
					   'flyway' as user_mutiert,
					   0 as version,
					   null as vorgaenger_id,
					   '1000-01-01' as gueltig_ab,
					   '9999-12-31' as gueltig_bis,
					   'TAGESFAMILIEN' as betreuungsangebot_typ,
					   null as iban,
					   UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')) as adresse_id,
					   UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', '')) as institution_id,
					   null as kontoinhaber,
					   null as adresse_kontoinhaber_id,
					   null as institution_stammdaten_tagesschule_id,
					   null as institution_stammdaten_ferieninsel_id,
					   'mail@example.com' as mail,
					   null as telefon,
					   null as webseite,
					   null as oeffnungszeiten,
					   false as alterskategorie_baby,
					   false as alterskategorie_vorschule,
					   false as alterskategorie_kindergarten,
					   false as alterskategorie_schule,
					   false as subventionierte_plaetze,
					   0.00 as anzahl_plaetze,
					   null as anzahl_plaetze_firmen
	) AS tmp
	WHERE NOT EXISTS(
		SELECT id FROM institution_stammdaten WHERE id = UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', ''))
	)
	LIMIT 1;
