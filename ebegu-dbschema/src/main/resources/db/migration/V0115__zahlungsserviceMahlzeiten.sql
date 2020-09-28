# InstitutionId und InstituionName umbenennen, damit es auch fuer Mahlzeiten passt
ALTER TABLE zahlung	CHANGE COLUMN institution_id empfaenger_id binary(16) not null;
ALTER TABLE zahlung	CHANGE COLUMN institution_name empfaenger_name varchar(255) not null;
ALTER TABLE zahlung_aud	CHANGE COLUMN institution_id empfaenger_id binary(16);
ALTER TABLE zahlung_aud	CHANGE COLUMN institution_name empfaenger_name varchar(255);

# Neuer Zahlungsstatus fuer Mahlzeiten-Zahlungslaeufe
ALTER TABLE verfuegung_zeitabschnitt ADD zahlungsstatus_mahlzeitenverguenstigung varchar(255) not null;
ALTER TABLE verfuegung_zeitabschnitt_aud ADD zahlungsstatus_mahlzeitenverguenstigung varchar(255);