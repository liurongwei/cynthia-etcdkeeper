create table server_config (
  id int not null auto_increment,
  title varchar(64) not null default '',
  endpoints varchar(256) not null default '',
  use_tls bit not null default false ,
  key_file varchar(256),
  ca_file varchar(256),
  cert_file varchar(256),
  use_auth bit not null default false ,
  username varchar(64),
  password varchar(64),
  create_time timestamp default current_timestamp,
  update_time timestamp default current_timestamp on update current_timestamp,
  primary key (id)
);
