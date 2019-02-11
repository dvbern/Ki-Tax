ALTER TABLE gemeinde_stammdaten
	ADD bic VARCHAR(255) NOT NULL,
	ADD iban VARCHAR(34) NOT NULL,
	ADD konto_inhaber VARCHAR(255) NOT NULL;

ALTER TABLE gemeinde_stammdaten_aud
	ADD bic VARCHAR(255),
	ADD iban VARCHAR(34),
	ADD konto_inhaber VARCHAR(255);