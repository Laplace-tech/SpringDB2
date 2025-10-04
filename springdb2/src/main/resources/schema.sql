drop table if exists item;
create table if not exists item(
	id bigint auto_increment primary key,
	item_name varchar(255) not null,
	price int not null,
	quantity int not null
);