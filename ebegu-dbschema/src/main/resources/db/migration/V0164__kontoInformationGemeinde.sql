ALTER TABLE gemeinde_stammdaten
	ADD bic VARCHAR(255) NOT NULL,
	ADD iban VARCHAR(34) NOT NULL,
	ADD kontoinhaber VARCHAR(255) NOT NULL;

ALTER TABLE gemeinde_stammdaten_aud
	ADD bic VARCHAR(255),
	ADD iban VARCHAR(34),
	ADD kontoinhaber VARCHAR(255);