CREATE TABLE IF NOT EXISTS USERS
(
    id         serial primary key not null,
    email      varchar(255)       not null unique,
    first_name varchar(100)       not null,
    last_name  varchar(100),
    password   varchar(255)       not null,
    user_role  varchar(255)       not null
);