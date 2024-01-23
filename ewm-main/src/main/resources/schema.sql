DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS locations CASCADE;DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS compilations CASCADE;
DROP TABLE IF EXISTS compilations_to_event CASCADE;
DROP TABLE If EXISTS requests CASCADE;

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email varchar(254) UNIQUE,
    name  varchar(250)
    );

CREATE TABLE IF NOT EXISTS categories
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  varchar(50) UNIQUE
    );

CREATE TABLE IF NOT EXISTS locations
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    lat  float NOT NULL,
    lon  float NOT NULL
    );

CREATE TABLE IF NOT EXISTS events
(
    id BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    annotation         varchar(2000) NOT NULL,
    category_id        BIGINT,
    confirmed_requests BIGINT,
    created_on         TIMESTAMP WITHOUT TIME ZONE,
    description        varchar(7000),
    event_date         TIMESTAMP WITHOUT TIME ZONE,
    initiator_id       BIGINT,
    location_id        BIGINT,
    paid               BOOLEAN,
    participant_limit  INTEGER DEFAULT 0,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN,
    eventStatus              varchar(200),
    title              varchar(120),
    CONSTRAINT fk_events_to_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_events_to_users FOREIGN KEY (initiator_id) REFERENCES users (id),
    CONSTRAINT fk_events_to_locations FOREIGN KEY (location_id) REFERENCES locations (id)
    );

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id     BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    create_date  TIMESTAMP WITHOUT TIME ZONE,
    status       VARCHAR(20),
    CONSTRAINT fk_requests_to_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_requests_to_user FOREIGN KEY (requester_id) REFERENCES users (id)
    );

CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY UNIQUE,
    pinned BOOLEAN      NOT NULL,
    title  VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS compilations_to_event
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id       BIGINT NOT NULL,
    compilation_id BIGINT NOT NULL,
    CONSTRAINT fk_event_compilation_to_event FOREIGN KEY (event_id) REFERENCES events (id) ON UPDATE CASCADE,
    CONSTRAINT fk_event_compilation_to_compilation FOREIGN KEY (compilation_id) REFERENCES compilations (id) ON UPDATE CASCADE
    );