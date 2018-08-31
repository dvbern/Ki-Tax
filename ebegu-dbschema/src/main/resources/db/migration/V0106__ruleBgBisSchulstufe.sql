# Neue Regel BG_BIS_UND_MIT_SCHULSTUFE

# Fuer jede Gesuchsperiode einfuegen: System Default = KINDERGARTEN2
INSERT INTO einstellung (
	SELECT
		UUID(),
		p.timestamp_erstellt,
		p.timestamp_mutiert,
		p.user_erstellt,
		p.user_mutiert,
		p.version,
		'BG_BIS_UND_MIT_SCHULSTUFE',
		'KINDERGARTEN2',
		NULL,
		p.id,
		NULL

	FROM gesuchsperiode p
);

# Ostermundigen: Fuer jede GP den Wert VORSCHULALTER
INSERT INTO einstellung (
	SELECT
		UUID(),
		p.timestamp_erstellt,
		p.timestamp_mutiert,
		p.user_erstellt,
		p.user_mutiert,
		p.version,
		'BG_BIS_UND_MIT_SCHULSTUFE',
		'VORSCHULALTER',
		'80a8e496-b73c-4a4a-a163-a0b2caf76487',
		p.id,
		'e3736eb8-6eef-40ef-9e52-96ab48d8f220'

	FROM gesuchsperiode p
);

# Neue Regel KONTINGENTIERUNG_ENABLED
# Fuer jede Gesuchsperiode einfuegen: System Default = FALSE
INSERT INTO einstellung (
	SELECT
		UUID(),
		p.timestamp_erstellt,
		p.timestamp_mutiert,
		p.user_erstellt,
		p.user_mutiert,
		p.version,
		'KONTINGENTIERUNG_ENABLED',
		'false',
		NULL,
		p.id,
		NULL

	FROM gesuchsperiode p
);