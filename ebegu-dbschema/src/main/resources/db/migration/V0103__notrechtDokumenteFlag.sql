alter table rueckforderung_formular add if not exists unchecked_documents bit not null default 0;
alter table rueckforderung_formular_aud add if not exists unchecked_documents bit;

update rueckforderung_formular set unchecked_documents = false;