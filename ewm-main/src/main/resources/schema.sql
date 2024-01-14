DROP TABLE IF EXISTS categories CASCADE;

CREATE TABLE IF NOT EXISTS categories
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  varchar(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email varchar(254) NOT NULL,
    name  varchar(250) NOT NULL
    );