ALTER TABLE fall DROP FOREIGN KEY FK_fall_verantwortlicher_id;
ALTER TABLE fall DROP FOREIGN KEY FK_fall_verantwortlicher_sch_id;
ALTER TABLE fall DROP INDEX IX_fall_verantwortlicher;
ALTER TABLE fall DROP INDEX IX_fall_verantwortlicher_sch;

ALTER TABLE fall DROP verantwortlicher_id;
ALTER TABLE fall DROP verantwortlichersch_id;

ALTER TABLE fall_aud DROP verantwortlicher_id;
ALTER TABLE fall_aud DROP verantwortlichersch_id;

CREATE INDEX IX_dossier_verantwortlicher_bg ON dossier (verantwortlicherbg_id);
CREATE INDEX IX_dossier_verantwortlicher_ts ON dossier (verantwortlicherts_id);
CREATE INDEX IX_dossier_verantwortlicher_gmde ON dossier (verantwortlichergmde_id);

UPDATE application_property SET name = 'DEFAULT_VERANTWORTLICHER_BG' WHERE name = 'DEFAULT_VERANTWORTLICHER';
UPDATE application_property SET name = 'DEFAULT_VERANTWORTLICHER_TS' WHERE name = 'DEFAULT_VERANTWORTLICHER_SCH';