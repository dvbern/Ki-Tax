alter table gemeinde_stammdaten add COLUMN bg_adresse_id BINARY(16);
alter table gemeinde_stammdaten add COLUMN ts_adresse_id BINARY(16);
alter table gemeinde_stammdaten add COLUMN default_benutzer_id BINARY(16);

alter table gemeinde_stammdaten_aud add COLUMN bg_adresse_id BINARY(16);
alter table gemeinde_stammdaten_aud add COLUMN ts_adresse_id BINARY(16);
alter table gemeinde_stammdaten_aud add COLUMN default_benutzer_id BINARY(16);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_gemeindestammdaten_bg_adresse_id
FOREIGN KEY (bg_adresse_id)
REFERENCES adresse(id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT UK_gemeinde_stammdaten_bg_adresse_id UNIQUE (bg_adresse_id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_gemeindestammdaten_ts_adresse_id
FOREIGN KEY (ts_adresse_id)
REFERENCES adresse(id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT UK_gemeinde_stammdaten_ts_adresse_id UNIQUE (ts_adresse_id);

ALTER TABLE gemeinde_stammdaten
	ADD CONSTRAINT FK_gemeindestammdaten_defaultbenutzer_id
FOREIGN KEY (default_benutzer_id)
REFERENCES benutzer(id);

update gemeinde_stammdaten set default_benutzer_id = default_benutzerbg_id;
update gemeinde_stammdaten set default_benutzerbg_id = null;
