CREATE TABLE bfs_gemeinde (
	id            VARCHAR(36)  NOT NULL,
	bezirk        VARCHAR(255) NOT NULL,
	bezirk_nummer BIGINT       NOT NULL,
	bfs_nummer    BIGINT       NOT NULL,
	name	      VARCHAR(255) NOT NULL,
	gueltig_ab    DATE         NOT NULL,
	hist_nummer   BIGINT       NOT NULL,
	kanton        VARCHAR(255) NOT NULL,
	mandant_id    VARCHAR(36)  NOT NULL,
	PRIMARY KEY (id)
);

ALTER TABLE bfs_gemeinde
	ADD CONSTRAINT FK_bfs_gemeinde_mandant_id
FOREIGN KEY (mandant_id)
REFERENCES mandant (id);