# Cynthia-EtcdKeeper
## etcdkeeper java api implemention 

This project was inspired by https://github.com/evildecay/etcdkeeper, thanks to evildecay, because the original project has some minor problems, so I took the time to develop a modified version of the java API, I hope to help everyone, currently The project can be used normally, but it has not been tested a lot. If you have any problems during the use, please contact me to issue an issue, you can solve the problem in use together.

The project uses spring boot development, the front and back end are integrated in a project, easy to deploy debugging, has now implemented most of the original etcdkeeper API, and for the ETCD API v2 and v3 versions, added the etcd server configuration function, making It is more convenient to manage multiple etcd instances and clusters. The configuration information is stored in h2's built-in DB. It can be persisted to disk files or stored in memory. You can adjust the storage mode by using system property :h2db parameter.

## Project roadmap:
1. Refactor the UI, share views and data (vue or angular)
2. to achieve a secure connection to the v3 API (currently there is a problem that cannot be securely connected)
3. Implement the page function of server configuration management.
