alter table gemeinde add tagesschulanmeldungen_startdatum DATE;
alter table gemeinde add ferieninselanmeldungen_startdatum DATE;
alter table gemeinde_aud add tagesschulanmeldungen_startdatum DATE;
alter table gemeinde_aud add ferieninselanmeldungen_startdatum DATE;

UPDATE gemeinde SET gemeinde.tagesschulanmeldungen_startdatum='2020-08-01';
UPDATE gemeinde SET gemeinde.ferieninselanmeldungen_startdatum='2020-08-01';

alter table gemeinde modify tagesschulanmeldungen_startdatum DATE NOT NULL;
alter table gemeinde modify ferieninselanmeldungen_startdatum DATE NOT NULL;