delete from einstellung where einstellung_key = 'TAGESSCHULE_ENABLED_FOR_MANDANT';
delete from einstellung where einstellung_key = 'BETREUUNGSGUTSCHEINE_ENABLED_FOR_GEMEINDE';
delete from einstellung where einstellung_key = 'TAGESSCHULE_ENABLED_FOR_GEMEINDE';
delete from einstellung where einstellung_key = 'FERIENINSEL_ENABLED_FOR_GEMEINDE';

alter table gemeinde add angebotbg bit not null;
alter table gemeinde add angebotts bit not null;
alter table gemeinde add angebotfi bit not null;
alter table gemeinde_aud add angebotbg bit not null;
alter table gemeinde_aud add angebotts bit not null;
alter table gemeinde_aud add angebotfi bit not null;

alter table mandant add  angebotts bit not null;
alter table mandant add  angebotfi bit not null;
alter table mandant_aud add  angebotts bit not null;
alter table mandant_aud add  angebotfi bit not null;

update mandant set angebotts = false;
update mandant set angebotfi = false;

update gemeinde set angebotbg = true;
update gemeinde set angebotts = false;
update gemeinde set angebotfi = false;