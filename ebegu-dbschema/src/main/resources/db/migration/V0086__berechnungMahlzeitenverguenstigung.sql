ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN tarif_pro_hauptmahlzeit decimal(19,2);
ALTER TABLE institution_stammdaten_betreuungsgutscheine ADD COLUMN tarif_pro_nebenmahlzeit decimal(19,2);

ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN tarif_pro_hauptmahlzeit decimal(19,2);
ALTER TABLE institution_stammdaten_betreuungsgutscheine_aud ADD COLUMN tarif_pro_nebenmahlzeit decimal(19,2);

ALTER TABLE betreuungsmitteilung_pensum ADD COLUMN tarif_pro_hauptmahlzeit decimal(19,2) NOT NULL DEFAULT 0;
ALTER TABLE betreuungsmitteilung_pensum_aud ADD COLUMN tarif_pro_hauptmahlzeit decimal(19,2);

ALTER TABLE betreuungspensum_abweichung ADD COLUMN tarif_pro_hauptmahlzeit decimal(19,2) NOT NULL DEFAULT 0;
ALTER TABLE betreuungspensum_abweichung_aud ADD COLUMN tarif_pro_hauptmahlzeit decimal(19,2);

ALTER TABLE betreuungspensum ADD COLUMN tarif_pro_hauptmahlzeit decimal(19,2) NOT NULL DEFAULT 0;
ALTER TABLE betreuungspensum_aud ADD COLUMN tarif_pro_hauptmahlzeit decimal(19,2);

ALTER TABLE betreuungsmitteilung_pensum ADD COLUMN tarif_pro_nebenmahlzeit decimal(19,2) NOT NULL DEFAULT 0;
ALTER TABLE betreuungsmitteilung_pensum_aud ADD COLUMN tarif_pro_nebenmahlzeit decimal(19,2);

ALTER TABLE betreuungspensum_abweichung ADD COLUMN tarif_pro_nebenmahlzeit decimal(19,2) NOT NULL DEFAULT 0;
ALTER TABLE betreuungspensum_abweichung_aud ADD COLUMN tarif_pro_nebenmahlzeit decimal(19,2);

ALTER TABLE betreuungspensum ADD COLUMN tarif_pro_nebenmahlzeit decimal(19,2) NOT NULL DEFAULT 0;
ALTER TABLE betreuungspensum_aud ADD COLUMN tarif_pro_nebenmahlzeit decimal(19,2);

ALTER TABLE bgcalculation_result ADD COLUMN verguenstigung_hauptmahlzeiten_total decimal(19,2);
ALTER TABLE bgcalculation_result_aud ADD COLUMN verguenstigung_hauptmahlzeiten_total decimal(19,2);

ALTER TABLE bgcalculation_result ADD COLUMN verguenstigung_nebenmahlzeiten_total decimal(19,2);
ALTER TABLE bgcalculation_result_aud ADD COLUMN verguenstigung_nebenmahlzeiten_total decimal(19,2);

