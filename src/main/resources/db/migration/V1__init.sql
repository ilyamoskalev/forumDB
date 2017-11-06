CREATE TABLE IF NOT EXISTS Users (
  nickname  citext     NOT NULL PRIMARY KEY,
  fullname  VARCHAR,
  about     TEXT,
  email     citext      NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Forums (
  id        SERIAL      NOT NULL PRIMARY KEY,
  title     VARCHAR     NOT NULL,
  username  VARCHAR     NOT NULL REFERENCES Users (nickname),
  slug      citext      NOT NULL UNIQUE,
  posts     INTEGER     DEFAULT 0,
  threads   INTEGER     DEFAULT 0
);

CREATE TABLE IF NOT EXISTS Threads (
  id        SERIAL                      NOT NULL PRIMARY KEY,
  author    VARCHAR                     NOT NULL REFERENCES Users(nickname),
  created   TIMESTAMP WITH TIME ZONE    NOT NULL ,
  forum     VARCHAR                     NOT NULL REFERENCES Forums(slug),
  message   TEXT                        NOT NULL,
  slug      citext                      UNIQUE,
  title     VARCHAR                     NOT NULL,
  votes     INTEGER                     DEFAULT 0
);


CREATE TABLE IF NOT EXISTS Posts (
  id        SERIAL                      NOT NULL PRIMARY KEY,
  author    VARCHAR                     NOT NULL REFERENCES Users(nickname),
  created   TIMESTAMP WITH TIME ZONE    NOT NULL ,
  forum     VARCHAR,
  isEdited  BOOLEAN                     DEFAULT FALSE,
  message   TEXT                        NOT NULL,
  parent    INTEGER                     DEFAULT 0,
  thread    INTEGER                     NOT NULL REFERENCES Threads(id),
  path      BIGINT                      ARRAY
);


CREATE TABLE IF NOT EXISTS Votes (
  id        SERIAL      NOT NULL PRIMARY KEY,
  username  VARCHAR     NOT NULL REFERENCES Users (nickname),
  voice     INTEGER,
  thread    INTEGER     NOT NULL REFERENCES Threads (id),
  UNIQUE(username, thread)
);