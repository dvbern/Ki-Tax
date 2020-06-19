ALTER TABLE rueckforderung_mitteilung DROP COLUMN gesendet_an_status;
ALTER TABLE rueckforderung_mitteilung_aud DROP COLUMN gesendet_an_status;

ALTER TABLE rueckforderung_mitteilung
	DROP CONSTRAINT FK_RueckforderungMitteilung_Benutzer_id;

ALTER TABLE rueckforderung_mitteilung
	DROP KEY UK_rueckforderung_mitteilung_absender;

ALTER TABLE rueckforderung_mitteilung
	ADD CONSTRAINT FK_RueckforderungMitteilung_Benutzer_id
		FOREIGN KEY (absender_id)
			REFERENCES benutzer (id);

ALTER TABLE rueckforderung_formular CHANGE COLUMN status status varchar(255) not null;
ALTER TABLE rueckforderung_formular_aud CHANGE COLUMN status status varchar(255);

DROP TABLE rueckforderung_mitteilung_aud;