create table if not exists hibernatesearch_LuceneIndexesData
(
	ID_COLUMN varchar(255) not null
		primary key,
	DATA_COLUMN blob not null,
	TIMESTAMP_COLUMN bigint not null
);

create index hibernatesearch_LuceneIndexesData_timestamp_index
	on hibernatesearch_LuceneIndexesData (TIMESTAMP_COLUMN);





create table if not exists hibernatesearch_LuceneIndexesMetadata
(
	ID_COLUMN varchar(255) not null
		primary key,
	DATA_COLUMN blob not null,
	TIMESTAMP_COLUMN bigint not null
);

create index hibernatesearch_LuceneIndexesMetadata_timestamp_index
	on hibernatesearch_LuceneIndexesMetadata (TIMESTAMP_COLUMN);

