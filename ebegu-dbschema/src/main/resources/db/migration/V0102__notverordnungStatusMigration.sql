update rueckforderung_formular set status = 'IN_PRUEFUNG_KANTON_STUFE_2' where status = 'IN_PRUEFUNG_KANTON_STUFE_2_PROVISORISCH';
update rueckforderung_formular_aud set status = 'IN_PRUEFUNG_KANTON_STUFE_2' where status = 'IN_PRUEFUNG_KANTON_STUFE_2_PROVISORISCH';
update rueckforderung_formular set has_been_provisorisch = false;
update rueckforderung_formular_aud set has_been_provisorisch = false;