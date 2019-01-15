ALTER TABLE verfuegung_zeitabschnitt ADD verguenstigung_ohne_beruecksichtigung_vollkosten DECIMAL(19, 2);
ALTER TABLE verfuegung_zeitabschnitt ADD verguenstigung_ohne_beruecksichtigung_minimalbeitrag DECIMAL(19, 2);
ALTER TABLE verfuegung_zeitabschnitt ADD verguenstigung DECIMAL(19, 2);
ALTER TABLE verfuegung_zeitabschnitt ADD minimaler_elternbeitrag DECIMAL(19, 2);

ALTER TABLE verfuegung_zeitabschnitt_aud ADD verguenstigung_ohne_beruecksichtigung_vollkosten DECIMAL(19, 2);
ALTER TABLE verfuegung_zeitabschnitt_aud ADD verguenstigung_ohne_beruecksichtigung_minimalbeitrag DECIMAL(19, 2);
ALTER TABLE verfuegung_zeitabschnitt_aud ADD verguenstigung DECIMAL(19, 2);
ALTER TABLE verfuegung_zeitabschnitt_aud ADD minimaler_elternbeitrag DECIMAL(19, 2);