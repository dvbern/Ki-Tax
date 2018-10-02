ALTER TABLE gemeinde ADD COLUMN betreuungsgutscheine_startdatum DATE;

UPDATE gemeinde g
JOIN einstellung e
ON g.id = e.gemeinde_id AND e.einstellung_key = 'BEGU_ANBIETEN_AB'
SET g.betreuungsgutscheine_startdatum = e.value;

DELETE FROM einstellung WHERE einstellung_key = 'BEGU_ANBIETEN_AB';