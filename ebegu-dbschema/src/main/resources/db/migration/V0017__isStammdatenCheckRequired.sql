ALTER TABLE institution ADD COLUMN stammdaten_check_required BIT NOT NULL DEFAULT FALSE;
ALTER TABLE institution_aud ADD COLUMN stammdaten_check_required BIT;