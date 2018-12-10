DELETE FROM betreuungspensum_container
WHERE betreuung_id IN
	  (
		  SELECT id
		  FROM betreuung
		  WHERE institution_stammdaten_id IN
				(
					SELECT id
					FROM institution_stammdaten
					WHERE betreuungsangebot_typ = 'TAGI'));

DELETE FROM abwesenheit_container
WHERE betreuung_id IN
	  (
		  SELECT id
		  FROM betreuung
		  WHERE institution_stammdaten_id IN
				(
					SELECT id
					FROM institution_stammdaten
					WHERE betreuungsangebot_typ = 'TAGI'));


DELETE FROM betreuungsmitteilung_pensum
WHERE betreuungsmitteilung_id IN
	  (
		  SELECT id
		  FROM mitteilung
		  WHERE betreuung_id IN
				(
					SELECT id
					FROM betreuung
					WHERE institution_stammdaten_id IN
						  (
							  SELECT id
							  FROM institution_stammdaten
							  WHERE betreuungsangebot_typ = 'TAGI')));

DELETE FROM mitteilung
WHERE betreuung_id IN
	  (
		  SELECT id
		  FROM betreuung
		  WHERE institution_stammdaten_id IN
				(
					SELECT id
					FROM institution_stammdaten
					WHERE betreuungsangebot_typ = 'TAGI'));

DELETE FROM betreuung
WHERE institution_stammdaten_id IN (
	SELECT id
	FROM institution_stammdaten
	WHERE betreuungsangebot_typ = 'TAGI');

DELETE FROM institution_stammdaten
WHERE betreuungsangebot_typ = 'TAGI';