# InstitutionStammdaten Spalten E-Mail & Telefon:

ALTER TABLE institution_stammdaten ADD mail VARCHAR(255);
ALTER TABLE institution_stammdaten ADD telefon VARCHAR(255);
ALTER TABLE institution_stammdaten_aud ADD mail VARCHAR(255);
ALTER TABLE institution_stammdaten_aud ADD telefon VARCHAR(255);
