UPDATE verfuegung_zeitabschnitt AS dest,
	(SELECT round(20 * anteilMonat * bgPensum * zeiteinheitFactor, 2) AS calc_verfuegte_anzahl_zeiteinheiten,
			round(20 * anteilMonat * anspruchberechtigtes_pensum / 100 * zeiteinheitFactor,
				  2) AS calc_anspruchsberechtigte_anzahl_zeiteinheiten,
			r.calc_zeiteinheit,
			r.id
	 FROM (
			  SELECT vz.anspruchberechtigtes_pensum,
					 vz.id,
					 least(vz.betreuungspensum, vz.anspruchberechtigtes_pensum) / 100 AS bgPensum,
					 (DATEDIFF(vz.gueltig_bis, vz.gueltig_ab) + 1) /
					 (DAYOFMONTH(LAST_DAY(vz.gueltig_ab))) AS anteilMonat,
					 (CASE i.betreuungsangebot_typ WHEN 'KITA'
													   THEN 1
												   WHEN 'TAGESFAMILIEN'
													   THEN 11 END) AS zeiteinheitFactor,
					 (CASE i.betreuungsangebot_typ WHEN 'KITA'
													   THEN 'DAYS'
												   WHEN 'TAGESFAMILIEN'
													   THEN 'HOURS' END) AS calc_zeiteinheit
			  FROM verfuegung_zeitabschnitt vz
				   INNER JOIN verfuegung v ON vz.verfuegung_betreuung_id = v.betreuung_id
				   INNER JOIN betreuung b ON v.betreuung_id = b.id
				   INNER JOIN institution_stammdaten i ON b.institution_stammdaten_id = i.id
		  ) AS r
	) AS src
SET dest.verfuegte_anzahl_zeiteinheiten            = src.calc_verfuegte_anzahl_zeiteinheiten,
	dest.anspruchsberechtigte_anzahl_zeiteinheiten = src.calc_verfuegte_anzahl_zeiteinheiten,
	dest.zeiteinheit                               = src.calc_zeiteinheit
WHERE src.id = dest.id;
