FROM mysql:latest

ENV MYSQL_ROOT_PASSWORD=rootpassword
ENV MYSQL_DATABASE=ecommerce
ENV MYSQL_USER=ecommerce-admin
ENV MYSQL_PASSWORD=root

EXPOSE 3306

CMD ["mysqld"]