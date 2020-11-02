ALTER TABLE rueckforderung_formular add stufe_2_provisorisch_verfuegt_datum datetime;
ALTER TABLE rueckforderung_formular_aud add stufe_2_provisorisch_verfuegt_datum datetime;

UPDATE rueckforderung_formular rf
    LEFT JOIN generated_notrecht_dokument gnd ON rf.id = gnd.rueckforderung_formular_id
SET rf.stufe_2_provisorisch_verfuegt_datum = gnd.timestamp_erstellt
WHERE gnd.typ = 'NOTRECHT_PROVISORISCHE_VERFUEGUNG';