ALTER TABLE erwerbspensum DROP zuschlag_zu_erwerbspensum;
ALTER TABLE erwerbspensum DROP zuschlagsgrund;
ALTER TABLE erwerbspensum DROP zuschlagsprozent;
ALTER TABLE erwerbspensum_aud DROP zuschlag_zu_erwerbspensum;
ALTER TABLE erwerbspensum_aud DROP zuschlagsgrund;
ALTER TABLE erwerbspensum_aud DROP zuschlagsprozent;

ALTER TABLE verfuegung DROP kategorie_zuschlag_zum_erwerbspensum;
ALTER TABLE verfuegung_aud DROP kategorie_zuschlag_zum_erwerbspensum;