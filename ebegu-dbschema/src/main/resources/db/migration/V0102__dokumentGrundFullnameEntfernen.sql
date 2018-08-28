ALTER TABLE dokument_grund
	DROP full_name;
ALTER TABLE dokument_grund_aud
	DROP full_name;

DELETE FROM dokument
WHERE dokument.dokument_grund_id IN (
	SELECT id
	FROM dokument_grund
	WHERE dokument_grund.person_type = 'FREETEXT');

DELETE FROM dokument_grund
WHERE dokument_grund.person_type = 'FREETEXT';
DELETE FROM dokument_grund_aud
WHERE dokument_grund_aud.person_type = 'FREETEXT';