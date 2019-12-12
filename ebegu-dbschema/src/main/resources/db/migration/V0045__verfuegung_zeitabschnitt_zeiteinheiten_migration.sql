ALTER TABLE ebegu.verfuegung_zeitabschnitt
	CHANGE COLUMN betreuungspensum betreuungspensum_prozent DECIMAL(19, 2) NULL;

ALTER TABLE ebegu.verfuegung_zeitabschnitt
	CHANGE COLUMN betreuungsstunden betreuungspensum_zeiteinheit DECIMAL(19, 2) NULL;

ALTER TABLE ebegu.verfuegung_zeitabschnitt_aud
	CHANGE COLUMN betreuungspensum betreuungspensum_prozent DECIMAL(19, 2) NULL;

ALTER TABLE ebegu.verfuegung_zeitabschnitt_aud
	CHANGE COLUMN betreuungsstunden betreuungspensum_zeiteinheit DECIMAL(19, 2) NULL;


UPDATE verfuegung_zeitabschnitt AS dest,
	(SELECT round(20 * anteilMonat * betreuungspensum_prozent / 100 * zeiteinheitFactor, 2) AS
	calc_betreuungspensum_zeiteinheiten, r.id
	 FROM (
			  SELECT vz.betreuungspensum_prozent,
					 vz.id,
					 (DATEDIFF(vz.gueltig_bis, vz.gueltig_ab) + 1) /
					 (DAYOFMONTH(LAST_DAY(vz.gueltig_ab))) AS anteilMonat,
					 (CASE i.betreuungsangebot_typ WHEN 'KITA'
													   THEN 1
												   WHEN 'TAGESFAMILIEN'
													   THEN 11 END) AS zeiteinheitFactor
			  FROM verfuegung_zeitabschnitt vz
				   INNER JOIN verfuegung v ON vz.verfuegung_betreuung_id = v.betreuung_id
				   INNER JOIN betreuung b ON v.betreuung_id = b.id
				   INNER JOIN institution_stammdaten i ON b.institution_stammdaten_id = i.id
		  ) AS r
	) AS src
SET dest.betreuungspensum_zeiteinheit            = src.calc_betreuungspensum_zeiteinheiten
WHERE src.id = dest.id;