alter table gemeinde_stammdaten add if not exists benachrichtigung_bg_email_auto bit not null;
alter table gemeinde_stammdaten_aud add if not exists benachrichtigung_bg_email_auto bit;
alter table gemeinde_stammdaten add if not exists benachrichtigung_ts_email_auto bit not null;
alter table gemeinde_stammdaten_aud add if not exists benachrichtigung_ts_email_auto bit;
alter table gemeinde_stammdaten add if not exists standard_dok_signature bit not null;
alter table gemeinde_stammdaten_aud add if not exists standard_dok_signature bit;

alter table gemeinde_stammdaten add if not exists standard_dok_title varchar(255);
alter table gemeinde_stammdaten_aud add if not exists standard_dok_title varchar(255);
alter table gemeinde_stammdaten add if not exists standard_dok_unterschrift_titel varchar(255);
alter table gemeinde_stammdaten_aud add if not exists standard_dok_unterschrift_titel varchar(255);
alter table gemeinde_stammdaten add if not exists standard_dok_unterschrift_name varchar(255);
alter table gemeinde_stammdaten_aud add if not exists standard_dok_unterschrift_name varchar(255);
alter table gemeinde_stammdaten add if not exists standard_dok_unterschrift_titel2 varchar(255);
alter table gemeinde_stammdaten_aud add if not exists standard_dok_unterschrift_titel2 varchar(255);
alter table gemeinde_stammdaten add if not exists standard_dok_unterschrift_name2 varchar(255);
alter table gemeinde_stammdaten_aud add if not exists standard_dok_unterschrift_name2 varchar(255);

update gemeinde_stammdaten set benachrichtigung_bg_email_auto  = true;
update gemeinde_stammdaten set benachrichtigung_ts_email_auto  = true;
update gemeinde_stammdaten set standard_dok_signature = true;