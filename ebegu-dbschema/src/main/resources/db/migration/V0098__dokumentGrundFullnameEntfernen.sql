ALTER TABLE dokument_grund
	DROP full_name;
ALTER TABLE dokument_grund_aud
	DROP full_name;

DELETE FROM dokument_grund
WHERE dokument_grund.person_type = 'FREETEXT';
DELETE FROM dokument_grund_aud
WHERE dokument_grund.person_type = 'FREETEXT';