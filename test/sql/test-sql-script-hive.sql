CREATE TABLE pokes
(
	foo INT,
	bar STRING
);

create table if not exists t_account
(
  id bigint,
  sex tinyint  COMMENT '性别',
  name String COMMENT '姓名',
  create_time timestamp,
  primary key(id) disable novalidate
) COMMENT '用户表'
partitioned by (year string)
clustered by (id) into 2 buckets
row format delimited fields terminated by '\t'
stored as orc TBLPROPERTIES('transactional'='true');