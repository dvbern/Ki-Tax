# Before this script is executed the database must be removed, since all Fachstellen will be removed and exiting links
# cannot be kept. For this reason we don't need to take care of existing data

# Create new Field IntegrationTyp
ALTER TABLE pensum_fachstelle ADD COLUMN integration_typ VARCHAR(255);
ALTER TABLE pensum_fachstelle_aud ADD COLUMN integration_typ VARCHAR(255);

# just to avoid errors we set a default value. The value is taken randomly
UPDATE pensum_fachstelle SET integration_typ = 'SOZIALE_INTEGRATION';

ALTER TABLE pensum_fachstelle MODIFY integration_typ VARCHAR(255) NOT NULL;

# Delete old Fachstellen
DELETE FROM fachstelle WHERE id IN
(
'1d1dd5db-32f1-11e6-8ae4-ccee479414a5',
'1d1dddfe-32f1-11e6-8ae4-ccee479414a5',
'1d1de134-32f1-11e6-8ae4-ccee479414a5',
'1d1de323-32f1-11e6-8ae4-ccee479414a5',
'1d1de839-32f1-11e6-8ae4-ccee479414a5',
'1d1dea0a-32f1-11e6-8ae4-ccee479414a5',
'1d1dedc0-32f1-11e6-8ae4-ccee479414a5',
'1d1def8c-32f1-11e6-8ae4-ccee479414a5',
'1d1df238-32f1-11e6-8ae4-ccee479414a5',
'1d1df3e4-32f1-11e6-8ae4-ccee479414a5'
);

# Create new Fachstellen
INSERT INTo fachstelle(id,
					   timestamp_erstellt,
					   timestamp_mutiert,
					   user_erstellt,
					   user_mutiert,
					   version,
					   vorgaenger_id,
					   behinderungsbestaetigung,
					   beschreibung,
					   name,
					   fachstelle_anspruch,
					   fachstelle_erweiterte_betreuung)
VALUES ('1d1dd5db-32f1-11e6-8ae4-abab47941400',
		'2016-06-15 14:03:04',
		'2016-06-15 14:03:04',
		'anonymous',
		'anonymous',
		0,
		null,
		0,
		'Mütter- und Väterberatung Bern',
		'Mütter- und Väterberatung Bern',
		1,
		0),

	   ('1d1dd5db-32f1-11e6-8ae4-abab47941401',
		'2016-06-15 14:03:04',
		'2016-06-15 14:03:04',
		'anonymous',
		'anonymous',
		0,
		null,
		0,
		'Sozialdienst',
		'Sozialdienst',
		1,
		0),

	   ('1d1dd5db-32f1-11e6-8ae4-abab47941402',
		'2016-06-15 14:03:04',
		'2016-06-15 14:03:04',
		'anonymous',
		'anonymous',
		0,
		null,
		0,
		'Kindes- und Erwachsenenschutzbehörde',
		'Kindes- und Erwachsenenschutzbehörde',
		1,
		0),

	   ('1d1dd5db-32f1-11e6-8ae4-abab47941403',
		'2016-06-15 14:03:04',
		'2016-06-15 14:03:04',
		'anonymous',
		'anonymous',
		0,
		null,
		0,
		'Erziehungsberatung',
		'Erziehungsberatung',
		1,
		0);
