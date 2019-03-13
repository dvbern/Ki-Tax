ALTER TABLE familiensituation
	ADD start_konkubinat DATE;

ALTER TABLE familiensituation_aud
	ADD start_konkubinat DATE;

UPDATE familiensituation SET familienstatus='KONKUBINAT_KEIN_KIND'
WHERE familienstatus in ('LAENGER_FUENF_JAHRE', 'WENIGER_FUENF_JAHRE');