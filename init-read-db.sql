CHANGE MASTER TO
    MASTER_HOST='write-db',
    MASTER_USER='repl',
    MASTER_PASSWORD='1234',
    MASTER_LOG_FILE='mysql-bin.000001',
    MASTER_LOG_POS=4,
    MASTER_SSL=1,
    MASTER_SSL_CA='/pems/ca.pem',
    MASTER_SSL_CERT='/pems/client-cert.pem',
    MASTER_SSL_KEY='/pems/client-key.pem';
START SLAVE;