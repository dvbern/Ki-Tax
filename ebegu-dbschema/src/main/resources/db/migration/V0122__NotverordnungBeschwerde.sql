ALTER TABLE rueckforderung_formular add beschwerde_betrag decimal(19,2);
ALTER TABLE rueckforderung_formular add beschwerde_bemerkung varchar(2000);
ALTER TABLE rueckforderung_formular add beschwerde_ausbezahlt_am datetime;

ALTER TABLE rueckforderung_formular_aud add beschwerde_betrag decimal(19,2);
ALTER TABLE rueckforderung_formular_aud add beschwerde_bemerkung varchar(2000);
ALTER TABLE rueckforderung_formular_aud add beschwerde_ausbezahlt_am datetime;