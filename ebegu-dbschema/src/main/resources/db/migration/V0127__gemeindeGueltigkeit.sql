ALTER TABLE gemeinde ADD gueltig_bis DATE NOT NULL DEFAULT "9999-12-31";
ALTER TABLE gemeinde_aud ADD gueltig_bis DATE;