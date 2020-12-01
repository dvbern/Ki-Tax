ALTER TABLE rueckforderung_formular add beschwerde_betrag decimal(19,2);
ALTER TABLE rueckforderung_formular add beschwerde_bemerkung text;
ALTER TABLE rueckforderung_formular add beschwerde_ausbezahlt_am datetime;

ALTER TABLE rueckforderung_formular_aud add beschwerde_betrag decimal(19,2);
ALTER TABLE rueckforderung_formular_aud add beschwerde_bemerkung text;
ALTER TABLE rueckforderung_formular_aud add beschwerde_ausbezahlt_am datetime;

ALTER TABLE rueckforderung_formular MODIFY bemerkung_fuer_verfuegung text;
alter table rueckforderung_formular_aud MODIFY bemerkung_fuer_verfuegung text;