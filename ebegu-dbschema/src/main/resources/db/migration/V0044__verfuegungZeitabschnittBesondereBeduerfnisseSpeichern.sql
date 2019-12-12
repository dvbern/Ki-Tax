alter table verfuegung_zeitabschnitt add besondere_beduerfnisse_bestaetigt bit not null default false;
alter table verfuegung_zeitabschnitt_aud add besondere_beduerfnisse_bestaetigt bit;

update verfuegung_zeitabschnitt
set besondere_beduerfnisse_bestaetigt = true
where verfuegung_betreuung_id in (
	select id from betreuung where id in (
		select betreuung_id from erweiterte_betreuung_container where erweiterte_betreuungja_id in (
			select id from erweiterte_betreuung where erweiterte_beduerfnisse = true and erweiterte_beduerfnisse_bestaetigt = true)));
