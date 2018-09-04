ALTER TABLE gemeinde ADD COLUMN status VARCHAR(16);
ALTER TABLE gemeinde_aud ADD COLUMN status VARCHAR(16);
UPDATE gemeinde SET status = 'AKTIV' WHERE enabled = TRUE;
UPDATE gemeinde SET status = 'GESPERRT' WHERE enabled = FALSE;
UPDATE gemeinde_aud SET status = 'AKTIV' WHERE enabled = TRUE;
UPDATE gemeinde_aud SET status = 'GESPERRT' WHERE enabled = FALSE;
ALTER TABLE gemeinde DROP COLUMN enabled;
ALTER TABLE gemeinde_aud DROP COLUMN enabled;
