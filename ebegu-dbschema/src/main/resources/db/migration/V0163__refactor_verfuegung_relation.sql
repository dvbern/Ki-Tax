# drop FK_betreuung_verfuegung_id"

ALTER TABLE betreuung
	DROP KEY UK_betreuung_verfuegung_id;



# drop uc

DROP INDEX UK_betreuung_verfuegung_id ON betreuung;



# drop feld betreuung.verfuegung_id und add verfung.betreuung_id

ALTER TABLE betreuung
	DROP COLUMN verfuegung_id;


#add fk_verfuegung_betreuung


alter table verfuegung
	add betreuung_id VARCHAR(255) not null;

alter table verfuegung drop primary key;

alter table verfuegung drop column id;

alter table verfuegung
	add constraint verfuegung_betreuung_id_pk
		primary key (betreuung_id);



-- todo drop fk FK_verfuegung_zeitabschnitt_verfuegung_id if rename does not work



ALTER TABLE verfuegung_zeitabschnitt 
RENAME COLUMN verfuegung_id TO verfuegung_betreuung_id;


ALTER TABLE verfuegung_zeitabschnitt_aud
RENAME COLUMN verfuegung_id TO verfuegung_betreuung_id;

