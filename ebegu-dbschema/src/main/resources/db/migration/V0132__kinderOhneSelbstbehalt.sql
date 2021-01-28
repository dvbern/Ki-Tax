ALTER TABLE lastenausgleich_detail CHANGE COLUMN total_belegungen total_belegungen_mit_selbstbehalt decimal(19,2) not null;
ALTER TABLE lastenausgleich_detail CHANGE COLUMN total_betrag_gutscheine total_betrag_gutscheine_mit_selbstbehalt decimal(19,2) not null;
ALTER TABLE lastenausgleich_detail_aud CHANGE COLUMN total_belegungen total_belegungen_mit_selbstbehalt decimal(19,2);
ALTER TABLE lastenausgleich_detail_aud CHANGE COLUMN total_betrag_gutscheine total_betrag_gutscheine_mit_selbstbehalt decimal(19,2);

ALTER TABLE lastenausgleich_detail ADD COLUMN total_belegungen_ohne_selbstbehalt decimal(19,2) not null;
ALTER TABLE lastenausgleich_detail ADD COLUMN total_betrag_gutscheine_ohne_selbstbehalt decimal(19,2) not null;
ALTER TABLE lastenausgleich_detail ADD COLUMN kosten_fuer_selbstbehalt decimal(19,2) not null;
ALTER TABLE lastenausgleich_detail_aud ADD COLUMN total_belegungen_ohne_selbstbehalt decimal(19,2);
ALTER TABLE lastenausgleich_detail_aud ADD COLUMN total_betrag_gutscheine_ohne_selbstbehalt decimal(19,2);
ALTER TABLE lastenausgleich_detail_aud ADD COLUMN kosten_fuer_selbstbehalt decimal(19,2);
