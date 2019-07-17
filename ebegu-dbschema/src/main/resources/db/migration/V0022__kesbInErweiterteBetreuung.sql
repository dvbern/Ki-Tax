#  add field to erweiterte_betreuung
ALTER TABLE erweiterte_betreuung
	ADD COLUMN keine_kesb_platzierung BIT NOT NULL;
ALTER TABLE erweiterte_betreuung_aud
	ADD COLUMN keine_kesb_platzierung BIT;

# set all new keine_kesb_platzierung to false
# noinspection SqlWithoutWhere
UPDATE erweiterte_betreuung
SET keine_kesb_platzierung = false;
# copy value from betreuung to erweiterte_betreuung JA-container
UPDATE erweiterte_betreuung
SET keine_kesb_platzierung = true
WHERE id IN (SELECT erweiterte_betreuungja_id
			 from erweiterte_betreuung_container ebc
				  JOIN betreuung b on ebc.betreuung_id = b.id
			 WHERE b.keine_kesb_platzierung = true);
# copy value from betreuung to erweiterte_betreuung GS-container
UPDATE erweiterte_betreuung
SET keine_kesb_platzierung = true
WHERE id IN (SELECT erweiterte_betreuunggs_id
			 from erweiterte_betreuung_container ebc
				  JOIN betreuung b on ebc.betreuung_id = b.id
			 WHERE b.keine_kesb_platzierung = true);

# remove field from betreuung
ALTER TABLE betreuung
	DROP COLUMN keine_kesb_platzierung;
ALTER TABLE betreuung_aud DROP COLUMN keine_kesb_platzierung;