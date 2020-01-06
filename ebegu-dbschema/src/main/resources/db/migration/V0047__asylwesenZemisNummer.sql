ALTER TABLE ebegu.kind
	ADD COLUMN aus_asylwesen BIT DEFAULT 0 NULL;

ALTER TABLE ebegu.kind_aud
	ADD COLUMN aus_asylwesen BIT DEFAULT 0 NULL;

ALTER TABLE ebegu.kind
	ADD COLUMN zemis_nummer VARCHAR(10) NULL;

ALTER TABLE ebegu.kind_aud
	ADD COLUMN zemis_nummer VARCHAR(10) NULL;
