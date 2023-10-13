CREATE TABLE IF NOT EXISTS PERSON (
                                      id         uuid          not null primary key,
                                      apelido    varchar(32)   not null,
                                      nome       varchar(100)  not null,
                                      nascimento date          not null,
                                      stack      varchar(1000) not null,
                                      search     varchar(1000) not null
);