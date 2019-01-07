ALTER TABLE kind ADD COLUMN spricht_amtssprache BOOLEAN;
ALTER TABLE kind_aud ADD COLUMN spricht_amtssprache BOOLEAN;

UPDATE kind SET spricht_amtssprache = muttersprache_deutsch;

ALTER TABLE kind DROP COLUMN muttersprache_deutsch;
ALTER TABLE kind_aud DROP COLUMN muttersprache_deutsch;