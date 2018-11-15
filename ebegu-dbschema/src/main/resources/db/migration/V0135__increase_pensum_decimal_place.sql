-- increase pensum decimal places to 10
ALTER TABLE betreuungspensum MODIFY pensum DECIMAL(19, 10) NOT NULL;
ALTER TABLE betreuungspensum_aud MODIFY pensum DECIMAL(19, 10);

ALTER TABLE betreuungsmitteilung_pensum MODIFY pensum DECIMAL(19, 10) NOT NULL;
ALTER TABLE betreuungsmitteilung_pensum_aud MODIFY pensum DECIMAL(19, 10);