# InstitutionStammdatenTagesschule / InstitutionStammdatenFerieninsel neu zeitabhängig:

ALTER TABLE institution_stammdaten_tagesschule ADD gueltig_ab DATE;
ALTER TABLE institution_stammdaten_tagesschule ADD gueltig_bis DATE;

ALTER TABLE institution_stammdaten_tagesschule_aud ADD gueltig_ab DATE;
ALTER TABLE institution_stammdaten_tagesschule_aud ADD gueltig_bis DATE;

ALTER TABLE institution_stammdaten_ferieninsel ADD gueltig_ab DATE;
ALTER TABLE institution_stammdaten_ferieninsel ADD gueltig_bis DATE;

ALTER TABLE institution_stammdaten_ferieninsel_aud ADD gueltig_ab DATE;
ALTER TABLE institution_stammdaten_ferieninsel_aud ADD gueltig_bis DATE;

UPDATE institution_stammdaten_tagesschule SET gueltig_ab = '1000-01-01';
UPDATE institution_stammdaten_tagesschule SET gueltig_bis = '9999-12-31';

UPDATE institution_stammdaten_ferieninsel SET gueltig_ab = '1000-01-01';
UPDATE institution_stammdaten_ferieninsel SET gueltig_bis = '9999-12-31';

ALTER TABLE institution_stammdaten_tagesschule MODIFY gueltig_ab DATE NOT NULL;
ALTER TABLE institution_stammdaten_tagesschule MODIFY gueltig_bis DATE NOT NULL;

ALTER TABLE institution_stammdaten_ferieninsel MODIFY gueltig_ab DATE NOT NULL;
ALTER TABLE institution_stammdaten_ferieninsel MODIFY gueltig_bis DATE NOT NULL;

# oeffnungsstunden und oeffnungstage sind neu eine Einstellung

ALTER TABLE institution_stammdaten DROP oeffnungsstunden;
ALTER TABLE institution_stammdaten DROP oeffnungstage;

ALTER TABLE institution_stammdaten_aud DROP oeffnungsstunden;
ALTER TABLE institution_stammdaten_aud DROP oeffnungstage;

# Relation InstitutionStammdaten und Institution ist neu 1:1. d.h. neuer UK, pro Institution darf es in DB nur
# noch ein Stammdaten haben. Alle Referenzen muessen geloescht werden

DELETE FROM zahlungsposition;
DELETE FROM zahlung;
DELETE FROM pain001dokument;
DELETE FROM zahlungsauftrag;

DELETE FROM betreuungspensum_container WHERE betreuung_id IN (
	SELECT id FROM betreuung WHERE institution_stammdaten_id IN (
		SELECT id FROM institution_stammdaten WHERE gueltig_bis < now()));

DELETE FROM betreuungsmitteilung_pensum WHERE betreuungsmitteilung_id IN (
	SELECT id FROM mitteilung WHERE betreuung_id IN (
		SELECT id FROM betreuung WHERE institution_stammdaten_id IN (
			SELECT id FROM institution_stammdaten WHERE gueltig_bis < now())));

DELETE FROM mitteilung WHERE betreuung_id IN (
	SELECT id FROM betreuung WHERE institution_stammdaten_id IN (
		SELECT id FROM institution_stammdaten WHERE gueltig_bis < now()));

DELETE FROM abwesenheit_container WHERE betreuung_id IN (
	SELECT id FROM betreuung WHERE institution_stammdaten_id IN (
		SELECT id FROM institution_stammdaten WHERE gueltig_bis < now()));

DELETE FROM betreuung WHERE institution_stammdaten_id IN (
	SELECT id FROM institution_stammdaten WHERE gueltig_bis < now());

DELETE FROM institution_stammdaten WHERE gueltig_bis < now();

ALTER TABLE institution_stammdaten
	ADD CONSTRAINT UK_institution_stammdaten_institution_id UNIQUE (institution_id);

# Neues Feld Status auf Institution

ALTER TABLE institution ADD status VARCHAR(255) NOT NULL DEFAULT 'EINGELADEN';
ALTER TABLE institution_aud ADD status VARCHAR(255);