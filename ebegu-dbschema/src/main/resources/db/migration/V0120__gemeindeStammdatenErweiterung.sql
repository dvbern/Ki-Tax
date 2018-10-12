# Tabellen GemeindeStammdaten erweitern

ALTER TABLE gemeinde_stammdaten	ADD COLUMN anschrift VARCHAR(255) NULL;
ALTER TABLE gemeinde_stammdaten_aud	ADD COLUMN anschrift VARCHAR(36) NULL;

ALTER TABLE gemeinde_stammdaten	ADD COLUMN beschwerde_adresse_id VARCHAR(36) NULL;
ALTER TABLE gemeinde_stammdaten_aud	ADD COLUMN beschwerde_adresse_id VARCHAR(36) NULL;

ALTER TABLE gemeinde_stammdaten ADD COLUMN keine_beschwerde_adresse BIT NOT NULL DEFAULT TRUE;
ALTER TABLE gemeinde_stammdaten_aud ADD COLUMN keine_beschwerde_adresse BIT NULL;

ALTER TABLE gemeinde_stammdaten	ADD COLUMN korrespondenzsprache VARCHAR(16) NOT NULL DEFAULT 'DE';
ALTER TABLE gemeinde_stammdaten_aud	ADD COLUMN korrespondenzsprache VARCHAR(16) NULL;

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_gemeindestammdaten_beschwerdeadresse_id
FOREIGN KEY (beschwerde_adresse_id)
REFERENCES adresse (id);