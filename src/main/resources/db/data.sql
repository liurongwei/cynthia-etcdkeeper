insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, secure, use_auth, username, `password`)
values (1, 'local','127.0.0.1','127.0.0.1:2379', '2', false ,null ,null ,null, false , false ,null ,null )
on duplicate key update `name`='local';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, secure, use_auth, username, `password`)
values (2, 'v2','192.168.87.9','192.168.87.9:2379', '2', false ,null ,null ,null, false , false ,null ,null )
on duplicate key update `name`='v2';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, secure, use_auth, username, `password`)
values (3, 'v2-tls-secure','192.168.87.10','192.168.87.10:2379', '2', true ,'D:/linux/k8s/node0/tls/etcd-key.pem' ,
           'D:/linux/k8s/node0/tls/ca.pem' ,'D:/linux/k8s/node0/tls/etcd.pem',true , false ,null ,null )
on duplicate key update `name`='v2-tls-secure';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, secure, use_auth, username, `password`)
values (4, 'v3-tls-secure','192.168.87.10','192.168.87.10:2379', '3', true ,'D:/linux/k8s/node0/tls/pkcs8-key.pem' ,
           'D:/linux/k8s/node0/tls/ca.pem' ,'D:/linux/k8s/node0/tls/etcd.pem' , true ,false ,null ,null )
on duplicate key update `name`='v3-tls-secure';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, secure, use_auth, username, `password`)
values (5, 'v3','192.168.87.9','192.168.87.9:2379', '3', false ,null ,null ,null,false , false ,null ,null )
on duplicate key update `name`='v3';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, secure, use_auth, username, `password`)
values (6, 'v3-tls-home','192.168.31.127','192.168.31.127:2379', '3', false ,'D:/linux/k8s/node0/tls/pkcs8-key.pem' ,
        'D:/linux/k8s/node0/tls/ca.pem' ,'D:/linux/k8s/node0/tls/etcd.pem', true , false ,null ,null )
    on duplicate key update `name`='v3-tls-home';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, secure, use_auth, username, `password`)
values (7, 'v3-tls-insecure','192.168.87.10','192.168.87.10:2379', '3', true ,'D:/linux/k8s/node0/tls/pkcs8-key.pem' ,
           'D:/linux/k8s/node0/tls/ca.pem' ,'D:/linux/k8s/node0/tls/etcd.pem' , false ,false ,null ,null )
on duplicate key update `name`='v3-tls-insecure';

insert into server_config (id ,name, title, endpoints, api_version, use_tls, key_file, ca_file, cert_file, secure, use_auth, username, `password`)
values (8, 'v2-tls-insecure','192.168.87.10','192.168.87.10:2379', '2', true ,'D:/linux/k8s/node0/tls/etcd-key.pem' ,
           'D:/linux/k8s/node0/tls/ca.pem' ,'D:/linux/k8s/node0/tls/etcd.pem',false , false ,null ,null )
on duplicate key update `name`='v2-tls-insecure';
