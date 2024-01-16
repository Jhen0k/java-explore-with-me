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

CREATE TABLE IF NOT EXISTS locations
(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    lat float NOT NULL,
    lon float NOT NULL
    );

CREATE TABLE IF NOT EXISTS events
(
    id BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annotation         varchar(2000) NOT NULL,
    category_id        BIGINT,
    confirmed_requests BIGINT,
    created_on          TIMESTAMP WITHOUT TIME ZONE,
    description        varchar(7000),
    event_date          TIMESTAMP WITHOUT TIME ZONE,
    initiator_id       BIGINT,
    location_id        BIGINT,
    paid               BOOLEAN,
    participant_limit   BIGINT,
    published_on        TIMESTAMP WITHOUT TIME ZONE,
    request_moderation  BOOLEAN,
    state              varchar(50),
    title              varchar,
    views              BIGINT,
    CONSTRAINT fk_events_to_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_events_to_users FOREIGN KEY (initiator_id) REFERENCES users (id),
    CONSTRAINT fk_events_to_locations FOREIGN KEY (location_id) REFERENCES locations (id)
    );