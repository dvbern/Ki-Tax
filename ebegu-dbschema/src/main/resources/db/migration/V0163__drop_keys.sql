
ALTER TABLE betreuung
	DROP FOREIGN KEY FK_betreuung_verfuegung_id;

ALTER TABLE betreuung
DROP KEY UK_betreuung_verfuegung_id;




ALTER TABLE verfuegung_zeitabschnitt
DROP FOREIGN KEY FK_verfuegung_zeitabschnitt_verfuegung_id;



ALTER TABLE verfuegung_zeitabschnitt_aud
	DROP FOREIGN KEY FK_verfuegung_zeitabschnitt_aud_rev;



ALTER TABLE zahlungsposition
	DROP FOREIGN KEY FK_Zahlungsposition_verfuegungZeitabschnitt_id;





ALTER TABLE verfuegung_aud
DROP FOREIGN KEY FK_verfuegung_aud_rev;
