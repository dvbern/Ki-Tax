update authorisierter_benutzer set role = 'ADMIN_BG' where role = 'ADMIN';
update authorisierter_benutzer set role = 'SACHBEARBEITER_BG' where role = 'SACHBEARBEITER_JA';
update authorisierter_benutzer set role = 'ADMIN_TS' where role = 'ADMINISTRATOR_SCHULAMT';
update authorisierter_benutzer set role = 'SACHBEARBEITER_TS' where role = 'SCHULAMT';

update berechtigung set role = 'ADMIN_BG' where role = 'ADMIN';
update berechtigung set role = 'SACHBEARBEITER_BG' where role = 'SACHBEARBEITER_JA';
update berechtigung set role = 'ADMIN_TS' where role = 'ADMINISTRATOR_SCHULAMT';
update berechtigung set role = 'SACHBEARBEITER_TS' where role = 'SCHULAMT';

update berechtigung_aud set role = 'ADMIN_BG' where role = 'ADMIN';
update berechtigung_aud set role = 'SACHBEARBEITER_BG' where role = 'SACHBEARBEITER_JA';
update berechtigung_aud set role = 'ADMIN_TS' where role = 'ADMINISTRATOR_SCHULAMT';
update berechtigung_aud set role = 'SACHBEARBEITER_TS' where role = 'SCHULAMT';

update berechtigung_history set role = 'ADMIN_BG' where role = 'ADMIN';
update berechtigung_history set role = 'SACHBEARBEITER_BG' where role = 'SACHBEARBEITER_JA';
update berechtigung_history set role = 'ADMIN_TS' where role = 'ADMINISTRATOR_SCHULAMT';
update berechtigung_history set role = 'SACHBEARBEITER_TS' where role = 'SCHULAMT';

update berechtigung_history_aud set role = 'ADMIN_BG' where role = 'ADMIN';
update berechtigung_history_aud set role = 'SACHBEARBEITER_BG' where role = 'SACHBEARBEITER_JA';
update berechtigung_history_aud set role = 'ADMIN_TS' where role = 'ADMINISTRATOR_SCHULAMT';
update berechtigung_history_aud set role = 'SACHBEARBEITER_TS' where role = 'SCHULAMT';