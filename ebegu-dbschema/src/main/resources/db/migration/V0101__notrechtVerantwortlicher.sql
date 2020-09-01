alter table rueckforderung_formular add if not exists verantwortlicher_id  BINARY(16);
alter table rueckforderung_formular_aud add if not exists verantwortlicher_id  BINARY(16);


ALTER TABLE rueckforderung_formular
	ADD CONSTRAINT FK_rueckforderung_verantwortlicher_id
FOREIGN KEY (verantwortlicher_id)
REFERENCES benutzer(id);