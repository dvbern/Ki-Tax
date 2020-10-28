alter table betreuungsmitteilung_pensum modify monatliche_hauptmahlzeiten DECIMAL(19,2) NULL;
alter table betreuungsmitteilung_pensum modify monatliche_nebenmahlzeiten DECIMAL(19,2) NULL;
alter table betreuungsmitteilung_pensum_aud modify monatliche_hauptmahlzeiten DECIMAL(19,2) NULL;
alter table betreuungsmitteilung_pensum_aud modify monatliche_nebenmahlzeiten DECIMAL(19,2) NULL;

alter table betreuungspensum modify monatliche_hauptmahlzeiten DECIMAL(19,2) NOT NULL;
alter table betreuungspensum modify monatliche_nebenmahlzeiten DECIMAL(19,2) NOT NULL;
alter table betreuungspensum_aud modify monatliche_hauptmahlzeiten DECIMAL(19,2) NULL;
alter table betreuungspensum_aud modify monatliche_nebenmahlzeiten DECIMAL(19,2) NULL;

alter table betreuungspensum_abweichung modify monatliche_hauptmahlzeiten DECIMAL(19,2) NOT NULL;
alter table betreuungspensum_abweichung modify monatliche_nebenmahlzeiten DECIMAL(19,2) NOT NULL;
alter table betreuungspensum_abweichung_aud modify monatliche_hauptmahlzeiten DECIMAL(19,2) NULL;
alter table betreuungspensum_abweichung_aud modify monatliche_nebenmahlzeiten DECIMAL(19,2) NULL;