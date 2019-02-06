# generate alter-table script to insert virtual id_text column for a readable uuid
SELECT concat('alter table ', table_name, ' add column id_text  generated always as
 (insert(
    insert(
      insert(
        insert(hex(id),9,0,''-''),
        14,0,''-''),
      19,0,''-''),
    24,0,''-'')
 ) virtual,
name varchar(200));')
FROM information_schema.tables
WHERE table_schema = 'ebegu' AND table_type = 'base table';