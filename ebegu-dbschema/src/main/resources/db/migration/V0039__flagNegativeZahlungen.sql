alter table zahlungsauftrag add has_negative_zahlungen bit;
alter table zahlungsauftrag_aud add has_negative_zahlungen bit;

# bisher scheint es auf der Produktion noch keine negativen Zahlungen zu haben!
update zahlungsauftrag set has_negative_zahlungen = false;