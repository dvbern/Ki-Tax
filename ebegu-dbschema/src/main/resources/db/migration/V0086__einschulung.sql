ALTER TABLE kind ADD einschulung_typ VARCHAR(36);
ALTER TABLE kind_aud ADD einschulung_typ VARCHAR(36);
UPDATE kind SET einschulung_typ = 'VORSCHULALTER' WHERE einschulung = 0;
UPDATE kind SET einschulung_typ = 'KLASSE1' WHERE einschulung = 1;
ALTER TABLE kind DROP COLUMN einschulung;
UPDATE kind_aud SET einschulung_typ = 'VORSCHULALTER' WHERE einschulung = 0;
UPDATE kind_aud SET einschulung_typ = 'KLASSE1' WHERE einschulung = 1;
ALTER TABLE kind_aud DROP COLUMN einschulung;