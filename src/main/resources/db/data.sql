insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, use_auth, username, `password`)
    values (1, 'local','127.0.0.1','127.0.0.1:2379', '2', false ,null ,null ,null , false ,null ,null )
on duplicate key update `name`='local';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, use_auth, username, `password`)
values (2, 'demo','192.168.87.9','192.168.87.9:2379', '2', false ,null ,null ,null , false ,null ,null )
on duplicate key update `name`='demo';

