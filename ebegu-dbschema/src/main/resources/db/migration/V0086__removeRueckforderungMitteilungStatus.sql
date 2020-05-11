ALTER TABLE rueckforderung_mitteilung DROP COLUMN gesendet_an_status;
ALTER TABLE rueckforderung_mitteilung_aud DROP COLUMN gesendet_an_status;

alter table rueckforderung_mitteilung
	drop constraint FK_RueckforderungMitteilung_Benutzer_id;

alter table rueckforderung_mitteilung
	drop key UK_rueckforderung_mitteilung_absender;

alter table rueckforderung_mitteilung
	add constraint FK_RueckforderungMitteilung_Benutzer_id
		foreign key (absender_id)
			references benutzer (id);
