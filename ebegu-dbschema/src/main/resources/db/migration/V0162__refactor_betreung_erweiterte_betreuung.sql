# drop FK_betreuung_verfuegung_id"
# drop uc ohne namen ...
# drop feld betreuung.verfuegung_id und add verfung.betreuung_id
alter table erweiterte_betreuung_container drop key UK_erweiterte_betreuung_betreuung;

alter table erweiterte_betreuung_container drop primary key;

alter table erweiterte_betreuung_container drop column id;

alter table erweiterte_betreuung_container modify betreuung_id varchar(36) not null;

alter table erweiterte_betreuung_container
	add constraint erweiterte_betreuung_container_betreuung_id_pk
		primary key (betreuung_id);




