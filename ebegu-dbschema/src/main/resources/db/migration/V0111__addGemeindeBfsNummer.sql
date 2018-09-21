ALTER TABLE gemeinde ADD bfs_nummer BIGINT;
ALTER TABLE gemeinde_aud ADD bfs_nummer BIGINT;
UPDATE gemeinde SET bfs_nummer=351 WHERE name='Bern';
UPDATE gemeinde SET bfs_nummer=363 WHERE name='Ostermundigen';