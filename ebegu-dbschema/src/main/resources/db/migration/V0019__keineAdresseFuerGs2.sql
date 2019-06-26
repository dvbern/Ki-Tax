-- Alle Wohnadressen, Rechnungsadressen, Postadressen für GS2 loeschen
-- Wegen Abhaengigkeiten: Zuerst ein Flag setzen, danach diejenigen mit dem Flag löschen

alter table adresse add COLUMN gs2 bit not null default 0;

-- Alle Adressen vom Typ JA von GS2 markieren
update adresse set gs2 = 1 where id in (
	select gesuchsteller_adresseja_id from gesuchsteller_adresse_container
	where gesuchsteller_container_id in
		  (select id from gesuchsteller_container where id in (select gesuchsteller2_id from gesuch)
		  )
	);

-- Alle Adressen vom Typ GS von GS2 markieren
update adresse set gs2 = 1 where id in (
	select gesuchsteller_adressegs_id from gesuchsteller_adresse_container
	where gesuchsteller_container_id in
		  (select id from gesuchsteller_container where id in (select gesuchsteller2_id from gesuch)
		  )
	);

alter table gesuchsteller_adresse add COLUMN gs2 bit not null default 0;

-- Alle GesuchstellerAdressen vom Typ JA von GS2 loeschen
update gesuchsteller_adresse  set gs2 = 1 where id in (
	select gesuchsteller_adresseja_id from gesuchsteller_adresse_container
	where gesuchsteller_container_id in
		(select id from gesuchsteller_container where id in (select gesuchsteller2_id from gesuch)
		)
	);

-- Alle GesuchstellerAdressen vom Typ GS von GS2 loeschen
update gesuchsteller_adresse set gs2 = 1 where id in (
	select gesuchsteller_adressegs_id from gesuchsteller_adresse_container
	where gesuchsteller_container_id in
		(select id from gesuchsteller_container where id in (select gesuchsteller2_id from gesuch)
		)
	);

alter table gesuchsteller_adresse_container add COLUMN gs2 bit not null default 0;

-- Alle GesuchstellerAdresseContainer von GS2 (d.h. einige GS2 haben mehrere Adressen) loeschen
update gesuchsteller_adresse_container set gs2 = 1 where gesuchsteller_container_id in (
	select id from gesuchsteller_container where id in (select gesuchsteller2_id from gesuch)
	);

-- Jetzt koennen die markierten geloescht werden
delete from gesuchsteller_adresse_container where gs2 = 1;
delete from gesuchsteller_adresse where gs2 = 1;
delete from adresse where gs2 = 1;