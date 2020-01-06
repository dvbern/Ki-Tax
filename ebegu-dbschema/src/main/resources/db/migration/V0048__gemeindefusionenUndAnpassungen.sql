DELETE FROM bfs_gemeinde WHERE bfs_nummer = 873; # Kirchenthurnen
DELETE FROM bfs_gemeinde WHERE bfs_nummer = 874; # Lohnstorf
DELETE FROM bfs_gemeinde WHERE bfs_nummer = 876; # Mühlethurnen
DELETE FROM bfs_gemeinde WHERE bfs_nummer = 937; # Schwendibach
DELETE FROM bfs_gemeinde WHERE bfs_nummer = 996; # Wolfisberg

# Thurnen
INSERT INTO bfs_gemeinde (id, bfs_nummer, gueltig_ab, kanton, name, mandant_id, verbund_id) VALUES
       	('66c8fc22-21a0-11ea-a70b-f4390979fa3e', 889, '2020-01-01', 'BE', 'Thurnen', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), null);

# École cantonale de langue française => Es gibt eine Tagesschule, die direkt dem Kanton unterstellt ist. Für
# diese wird nun eine spezielle Gemeinde erstellt => KIBON-957
INSERT INTO bfs_gemeinde (id, bfs_nummer, gueltig_ab, kanton, name, mandant_id, verbund_id) VALUES
		('e27dff57-21a0-11ea-a70b-f4390979fa3e', 100000, '2020-01-01', 'BE', 'École cantonale de langue française', UNHEX(REPLACE('e3736eb8-6eef-40ef-9e52-96ab48d8f220', '-','')), null);
