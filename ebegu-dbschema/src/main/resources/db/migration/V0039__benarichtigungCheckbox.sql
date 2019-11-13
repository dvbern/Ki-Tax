alter table gemeinde_stammdaten add if not exists benachrichtigung_bg_email_auto bit not null;
alter table gemeinde_stammdaten_aud add if not exists benachrichtigung_bg_email_auto  bit;
alter table gemeinde_stammdaten add if not exists benachrichtigung_ts_email_auto  bit not null;
alter table gemeinde_stammdaten_aud add if not exists benachrichtigung_ts_email_auto  bit;

update gemeinde_stammdaten set benachrichtigung_bg_email_auto  = true;
update gemeinde_stammdaten set benachrichtigung_ts_email_auto  = true;