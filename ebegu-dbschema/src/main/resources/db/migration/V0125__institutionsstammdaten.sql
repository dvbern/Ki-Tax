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