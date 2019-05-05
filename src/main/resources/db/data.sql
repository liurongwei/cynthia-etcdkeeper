insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, use_auth, username, `password`)
    values (1, 'local','127.0.0.1','127.0.0.1:2379', '2', false ,null ,null ,null , false ,null ,null )
on duplicate key update `name`='local';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, use_auth, username, `password`)
values (2, 'demo','192.168.87.9','192.168.87.9:2379', '2', false ,null ,null ,null , false ,null ,null )
on duplicate key update `name`='demo';


insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, use_auth, username, `password`)
values (3, 'demo-tls','192.168.87.10','192.168.87.10:2379', '2', true ,'D:/linux/k8s/node0/tls/etcd-key.pem' ,
           'D:/linux/k8s/node0/tls/ca.pem' ,'D:/linux/k8s/node0/tls/etcd.pem' , false ,null ,null )
on duplicate key update `name`='demo-tls';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, use_auth, username, `password`)
values (4, 'demo-tls-v3','192.168.87.10','192.168.87.10:2379', '3', true ,'D:/linux/k8s/node0/tls/etcd-key.pem' ,
           'D:/linux/k8s/node0/tls/ca.pem' ,'D:/linux/k8s/node0/tls/etcd.pem' , false ,null ,null )
on duplicate key update `name`='demo-tls';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, use_auth, username, `password`)
values (5, 'demo-v3','192.168.87.9','192.168.87.9:2379', '3', false ,null ,null ,null , false ,null ,null )
on duplicate key update `name`='demo-v3';
