alter table bfs_gemeinde
	add verbund_id varchar(36);

alter table bfs_gemeinde
	add constraint FK_bfsgemeinde_verbund_id
foreign key (verbund_id)
references bfs_gemeinde (id);


alter table bfs_gemeinde drop column bezirk;
alter table bfs_gemeinde drop column bezirk_nummer;
alter table bfs_gemeinde drop column hist_nummer;


--  10014	Syndicat Scolaire du Grand-Val
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
('2dbe837b-aca8-48bc-979c-f2d432fb4f76', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10014, 'Syndicat Scolaire du Grand-Val', '2010-01-01');
-- 681	Belprahon
-- 687	Corcelles
-- 691	Crémines
-- 692	Eschert
-- 694	Grandval
UPDATE bfs_gemeinde set verbund_id = '2dbe837b-aca8-48bc-979c-f2d432fb4f76' where bfs_nummer = 681;
UPDATE bfs_gemeinde set verbund_id = '2dbe837b-aca8-48bc-979c-f2d432fb4f76' where bfs_nummer = 687;
UPDATE bfs_gemeinde set verbund_id = '2dbe837b-aca8-48bc-979c-f2d432fb4f76' where bfs_nummer = 691;
UPDATE bfs_gemeinde set verbund_id = '2dbe837b-aca8-48bc-979c-f2d432fb4f76' where bfs_nummer = 692;
UPDATE bfs_gemeinde set verbund_id = '2dbe837b-aca8-48bc-979c-f2d432fb4f76' where bfs_nummer = 694;


-- 10017	Aare-Oenz
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
('ab3a4d6d-7a5f-46fd-89d3-e72c880731d9', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10017, 'Gemeindeverband Schule Aare-Oenz', '2010-01-01');
-- 972	Berken
-- 976	Graben
-- 977	Heimenhausen
-- 980	Inkwil
UPDATE bfs_gemeinde set verbund_id = 'ab3a4d6d-7a5f-46fd-89d3-e72c880731d9' where bfs_nummer = 972;
UPDATE bfs_gemeinde set verbund_id = 'ab3a4d6d-7a5f-46fd-89d3-e72c880731d9' where bfs_nummer = 976;
UPDATE bfs_gemeinde set verbund_id = 'ab3a4d6d-7a5f-46fd-89d3-e72c880731d9' where bfs_nummer = 977;
UPDATE bfs_gemeinde set verbund_id = 'ab3a4d6d-7a5f-46fd-89d3-e72c880731d9' where bfs_nummer = 980;


-- 10019	Plateau de Diesse Communauté scolaire
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
('2a660093-f4a6-4527-bc84-a15441b76469', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10019, 'Communauté scolaire du Plateau de Diesse', '2010-01-01');
-- 724	Nods
-- 726	Plateau de Diesse
UPDATE bfs_gemeinde set verbund_id = '2a660093-f4a6-4527-bc84-a15441b76469' where bfs_nummer = 724;
UPDATE bfs_gemeinde set verbund_id = '2a660093-f4a6-4527-bc84-a15441b76469' where bfs_nummer = 726;


-- 10020	Hilterfingen Schulverband alle
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
('1b902ee0-9a99-47b8-ad1a-fb28cd213e7f', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10020, 'Schulverband Hilterfingen', '2010-01-01');
-- 929	Hilterfingen
-- 934	Oberhofen am Thunersee
-- 942	Thun
-- 927	Heiligenschwendi
UPDATE bfs_gemeinde set verbund_id = '1b902ee0-9a99-47b8-ad1a-fb28cd213e7f' where bfs_nummer = 929;
UPDATE bfs_gemeinde set verbund_id = '1b902ee0-9a99-47b8-ad1a-fb28cd213e7f' where bfs_nummer = 934;
UPDATE bfs_gemeinde set verbund_id = '1b902ee0-9a99-47b8-ad1a-fb28cd213e7f' where bfs_nummer = 942;
UPDATE bfs_gemeinde set verbund_id = '1b902ee0-9a99-47b8-ad1a-fb28cd213e7f' where bfs_nummer = 927;


-- 10027	Koppigen Schulverband
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
('2d70bc24-d7bf-4865-9a1c-73552c3b5ff5', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10027, 'Gemeindeverband Koppigen', '2010-01-01');
-- 402	Alchenstorf
-- 408	Hellsau
-- 410	Höchstetten
-- 413	Koppigen
-- 423	Willadingen
UPDATE bfs_gemeinde set verbund_id = '2d70bc24-d7bf-4865-9a1c-73552c3b5ff5' where bfs_nummer = 402;
UPDATE bfs_gemeinde set verbund_id = '2d70bc24-d7bf-4865-9a1c-73552c3b5ff5' where bfs_nummer = 408;
UPDATE bfs_gemeinde set verbund_id = '2d70bc24-d7bf-4865-9a1c-73552c3b5ff5' where bfs_nummer = 410;
UPDATE bfs_gemeinde set verbund_id = '2d70bc24-d7bf-4865-9a1c-73552c3b5ff5' where bfs_nummer = 413;
UPDATE bfs_gemeinde set verbund_id = '2d70bc24-d7bf-4865-9a1c-73552c3b5ff5' where bfs_nummer = 423;


-- 10030	Courtelary-Cormoret-Villeret, Syndicat scolaire
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
('7f452302-d400-4b3a-92e5-ded5b0f7beef', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10030, 'Syndicat scolaire Courtelary-Cormoret-Villeret, ', '2010-01-01');
-- 432	Cormoret
-- 434	Courtelary
-- 448	Villeret
UPDATE bfs_gemeinde set verbund_id = '7f452302-d400-4b3a-92e5-ded5b0f7beef' where bfs_nummer = 432;
UPDATE bfs_gemeinde set verbund_id = '7f452302-d400-4b3a-92e5-ded5b0f7beef' where bfs_nummer = 434;
UPDATE bfs_gemeinde set verbund_id = '7f452302-d400-4b3a-92e5-ded5b0f7beef' where bfs_nummer = 448;


-- 10032	Utzenstorf alle, untere Emme Schulverband
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
('03d50e7c-5b87-4cf8-968a-b3094f1c783b', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10032, 'Schulverband untere Emme', '2010-01-01');
-- 533	Bätterkinden
-- 552	Utzenstorf
-- 554	Wiler bei Utzenstorf
-- 556	Zielebach
UPDATE bfs_gemeinde set verbund_id = '03d50e7c-5b87-4cf8-968a-b3094f1c783b' where bfs_nummer = 533;
UPDATE bfs_gemeinde set verbund_id = '03d50e7c-5b87-4cf8-968a-b3094f1c783b' where bfs_nummer = 552;
UPDATE bfs_gemeinde set verbund_id = '03d50e7c-5b87-4cf8-968a-b3094f1c783b' where bfs_nummer = 554;
UPDATE bfs_gemeinde set verbund_id = '03d50e7c-5b87-4cf8-968a-b3094f1c783b' where bfs_nummer = 556;


-- 10038	Valbirse Syndicat Scolaire de l'école secondaire du bas de la vallée
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
('0f261686-77bc-4118-9658-93be4ec3c6e5', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10038, 'Syndicat Scolaire de l''école secondaire du bas de la vallée', '2010-01-01');
-- 683	Champoz
-- 690	Court
-- 711	Sorvilier
-- 717	Valbirse
UPDATE bfs_gemeinde set verbund_id = '0f261686-77bc-4118-9658-93be4ec3c6e5' where bfs_nummer = 683;
UPDATE bfs_gemeinde set verbund_id = '0f261686-77bc-4118-9658-93be4ec3c6e5' where bfs_nummer = 690;
UPDATE bfs_gemeinde set verbund_id = '0f261686-77bc-4118-9658-93be4ec3c6e5' where bfs_nummer = 711;
UPDATE bfs_gemeinde set verbund_id = '0f261686-77bc-4118-9658-93be4ec3c6e5' where bfs_nummer = 717;


-- 10044	Bettenhausen-Thörigen-Ochlenberg (Schulverband)
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
('bb0419f6-12ae-44c8-97f8-eb190e853c8d', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10044, 'Schulverband Bettenhausen-Ochlenberg-Thörigen', '2010-01-01');
-- 973	Bettenhausen
-- 985	Ochlenberg
-- 989	Thörigen
UPDATE bfs_gemeinde set verbund_id = 'bb0419f6-12ae-44c8-97f8-eb190e853c8d' where bfs_nummer = 973;
UPDATE bfs_gemeinde set verbund_id = 'bb0419f6-12ae-44c8-97f8-eb190e853c8d' where bfs_nummer = 985;
UPDATE bfs_gemeinde set verbund_id = 'bb0419f6-12ae-44c8-97f8-eb190e853c8d' where bfs_nummer = 989;


-- 10046	Schulimont (Schulverband)
INSERT INTO bfs_gemeinde (id, mandant_id, kanton, bfs_nummer, name, gueltig_ab) VALUES
('fc48f783-3d59-4503-adeb-5d5200b29b15', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), 'BE', 10046, 'Gemeindeverband Schulimont', '2010-01-01');
-- 494	Gals
-- 495	Gampelen
-- 497	Lüscherz
-- 501	Tschugg
-- 502	Vinelz
UPDATE bfs_gemeinde set verbund_id = 'fc48f783-3d59-4503-adeb-5d5200b29b15' where bfs_nummer = 494;
UPDATE bfs_gemeinde set verbund_id = 'fc48f783-3d59-4503-adeb-5d5200b29b15' where bfs_nummer = 495;
UPDATE bfs_gemeinde set verbund_id = 'fc48f783-3d59-4503-adeb-5d5200b29b15' where bfs_nummer = 497;
UPDATE bfs_gemeinde set verbund_id = 'fc48f783-3d59-4503-adeb-5d5200b29b15' where bfs_nummer = 501;
UPDATE bfs_gemeinde set verbund_id = 'fc48f783-3d59-4503-adeb-5d5200b29b15' where bfs_nummer = 502;