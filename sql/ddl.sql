
drop table if exists projects;

create table projects ( 
       id serial primary key,
       name varchar(255)
);

drop table if exists employess;

create table employess (
       id         int primary key,
       first_name varchar(255),
       last_name  varchar(255),
       email      varchar(255),
       is_active  boolean
);       
