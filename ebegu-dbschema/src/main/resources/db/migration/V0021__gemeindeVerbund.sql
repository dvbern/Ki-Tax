alter table bfs_gemeinde
	add verbund_id varchar(36);

alter table bfs_gemeinde
	add constraint FK_bfsgemeinde_verbund_id
foreign key (verbund_id)
references bfs_gemeinde (id);