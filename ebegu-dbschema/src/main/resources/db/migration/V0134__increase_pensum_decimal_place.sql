# InstitutionStammdatenTagesschule / InstitutionStammdatenFerieninsel neu zeitabhängig:

ALTER TABLE betreuungspensum ALTER COLUMN pensum DECIMAL(19, 10)
ALTER TABLE betreuungspensum_aud DECIMAL(19, 10)

ALTER TABLE betreuungsmitteilung_pensum DECIMAL(19, 10)
ALTER TABLE betreuungsmitteilung_pensum_aud DECIMAL(19, 10)