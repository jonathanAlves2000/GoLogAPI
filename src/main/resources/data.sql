create table user_table (
    id int auto_increment primary key,
    user_name varchar(255) not null,
    email varchar(255) not null unique,
    password varchar(255) not null,
    type varchar(255) not null
);

create table driver_table (
    id int auto_increment primary key,
    name varchar(255) not null,
    cpf varchar(20) not null unique,
    cnh varchar(20) not null unique,
    cell_number varchar(20) not null unique,
    user_id int
);

alter table driver_table
add constraint fk_driver_user foreign key (user_id) references user_table(id);


