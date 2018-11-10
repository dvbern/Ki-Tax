-- increase pensum decimal places to 10
ALTER TABLE betreuungspensum ALTER COLUMN pensum DECIMAL(19, 10)
ALTER TABLE betreuungspensum_aud ALTER COLUMN pensum DECIMAL(19, 10)

ALTER TABLE betreuungsmitteilung_pensum ALTER COLUMN pensum DECIMAL(19, 10)
ALTER TABLE betreuungsmitteilung_pensum_aud ALTER COLUMN pensum DECIMAL(19, 10)