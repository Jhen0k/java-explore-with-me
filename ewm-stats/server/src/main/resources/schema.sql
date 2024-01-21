DROP TABLE IF EXISTS statistic_watch CASCADE;

CREATE TABLE IF NOT EXISTS statistic_watch
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    app       varchar(320),
    uri       varchar(100),
    ip        varchar(100),
    timestamp TIMESTAMP WITHOUT TIME ZONE
    );