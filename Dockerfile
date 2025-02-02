FROM mysql:latest

ENV MYSQL_ROOT_PASSWORD=rootpassword
    MYSQL_DATABASE=ecommerce
    MYSQL_USER=ecommerce-admin
    MYSQL_PASSWORD=root

EXPOSE 3306

CMD ["mysqld"]