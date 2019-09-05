alter table gesuch add COLUMN anzahl_gesuch_zurueckgezogen INTEGER NOT NULL DEFAULT 0;
alter table gesuch_aud add COLUMN anzahl_gesuch_zurueckgezogen INTEGER;
