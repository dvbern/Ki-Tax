ALTER TABLE benutzer
	MODIFY COLUMN externaluuid VARCHAR(255);
ALTER TABLE benutzer_aud
	MODIFY COLUMN externaluuid VARCHAR(255);