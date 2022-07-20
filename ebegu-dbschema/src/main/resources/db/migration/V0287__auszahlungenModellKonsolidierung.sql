# Vorbereitung: Auf den Auszahlungsdaten fehlt das Feld InfomaBankcode

ALTER TABLE auszahlungsdaten ADD COLUMN IF NOT EXISTS infoma_bankcode VARCHAR(255);
ALTER TABLE auszahlungsdaten_aud ADD COLUMN IF NOT EXISTS infoma_bankcode VARCHAR(255);

# 1: Infoma-Daten von InstitutionStammdatenBetreuungsgutscheine auf Auszahlungsdaten verschieben

# Daten kopieren, falls aussen vorhanden, innen aber nicht
update auszahlungsdaten a, institution_stammdaten_betreuungsgutscheine isb
set a.infoma_kreditorennummer = isb.infoma_kreditorennummer
where a.id = isb.auszahlungsdaten_id
		and isb.infoma_kreditorennummer is not null
		and a.infoma_kreditorennummer is null;

update auszahlungsdaten a, institution_stammdaten_betreuungsgutscheine isb
set a.infoma_bankcode = isb.infoma_bankcode
where a.id = isb.auszahlungsdaten_id
		and isb.infoma_bankcode is not null
		and a.infoma_bankcode is null;

# Die Felder auf den InstitutionStammdatenBetreuungsgutscheine entfernen
alter table institution_stammdaten_betreuungsgutscheine drop IF EXISTS infoma_kreditorennummer;
alter table institution_stammdaten_betreuungsgutscheine drop IF EXISTS infoma_bankcode;
alter table institution_stammdaten_betreuungsgutscheine_aud drop IF EXISTS infoma_kreditorennummer;
alter table institution_stammdaten_betreuungsgutscheine_aud drop IF EXISTS infoma_bankcode;

# 1: Infoma-Daten von Familiensituation auf Auszahlungsdaten Infoma verschieben

update auszahlungsdaten a, familiensituation fs
set a.infoma_kreditorennummer = fs.infoma_kreditorennummer
where a.id = fs.auszahlungsdaten_infoma_id
		and fs.infoma_kreditorennummer is not null
		and a.infoma_kreditorennummer is null;

update auszahlungsdaten a, familiensituation fs
set a.infoma_bankcode = fs.infoma_bankcode
where a.id = fs.auszahlungsdaten_infoma_id
		and fs.infoma_bankcode is not null
		and a.infoma_bankcode is null;

# Die Felder auf der Familiensituation entfernen
alter table familiensituation drop IF EXISTS infoma_kreditorennummer;
alter table familiensituation drop IF EXISTS infoma_bankcode;
alter table familiensituation_aud drop IF EXISTS infoma_kreditorennummer;
alter table familiensituation_aud drop IF EXISTS infoma_bankcode;


