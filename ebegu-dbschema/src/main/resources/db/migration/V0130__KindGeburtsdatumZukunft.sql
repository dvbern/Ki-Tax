ALTER TABLE kind add COLUMN zukunftige_geburtsdatum BIT NOT NULL;
ALTER TABLE kind_aud add COLUMN zukunftige_geburtsdatum BIT;
UPDATE kind set zukunftige_geburtsdatum = false;