
drop table if exists employees;

create table employees (
       id              serial primary key,
       first_name      varchar(255),
       last_name       varchar(255),
       trello_username varchar(255),
       email           varchar(255),
       add_at          timestamp DEFAULT current_timestamp
);       
