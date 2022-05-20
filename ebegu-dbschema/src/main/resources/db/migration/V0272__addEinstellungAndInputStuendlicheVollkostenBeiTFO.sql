ALTER table betreuungspensum add stuendliche_vollkosten DECIMAL(19, 2);
ALTER table betreuungspensum_aud add stuendliche_vollkosten DECIMAL(19, 2);

ALTER table betreuungspensum_abweichung add stuendliche_vollkosten DECIMAL(19, 2);
ALTER table betreuungspensum_abweichung_aud add stuendliche_vollkosten DECIMAL(19, 2);

ALTER table betreuungsmitteilung_pensum add stuendliche_vollkosten DECIMAL(19, 2);
ALTER table betreuungsmitteilung_pensum_aud add stuendliche_vollkosten DECIMAL(19, 2);