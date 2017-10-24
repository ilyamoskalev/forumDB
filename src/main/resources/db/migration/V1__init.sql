CREATE EXTENSION citext;

CREATE TABLE IF NOT EXISTS Users (
  nickname  VARCHAR     PRIMARY KEY,
  fullname  VARCHAR,
  about     TEXT,
  email     CITEXT      NOT NULL UNIQUE
);


CREATE UNIQUE INDEX IF NOT EXISTS indexUniqueEmail ON Users (email);
CREATE UNIQUE INDEX IF NOT EXISTS uniqueUpNickname ON Users (UPPER(nickname));
CREATE UNIQUE INDEX IF NOT EXISTS indexUniqueNickname ON Users (nickname);
CREATE UNIQUE INDEX IF NOT EXISTS indexUniqueNicknameLow ON Users (LOWER(nickname collate "ucs_basic"));


CREATE TABLE IF NOT EXISTS Forums (
  id        SERIAL      NOT NULL PRIMARY KEY,
  title     VARCHAR     NOT NULL,
  username  VARCHAR     NOT NULL REFERENCES Users (nickname),
  slug      CITEXT      NOT NULL UNIQUE,
  posts     INTEGER     DEFAULT 0,
  threads   INTEGER     DEFAULT 0
);


CREATE INDEX IF NOT EXISTS indexForumsUser ON Forums(username);
CREATE UNIQUE INDEX IF NOT EXISTS indexUniqueSlugForums ON Forums (slug);


CREATE TABLE IF NOT EXISTS Threads (
  id        SERIAL                      NOT NULL PRIMARY KEY,
  author    VARCHAR                     NOT NULL REFERENCES Users(nickname),
  created   TIMESTAMP WITH TIME ZONE    DEFAULT current_timestamp,
  forum     INTEGER                     NOT NULL REFERENCES Forums(id),
  message   TEXT                        NOT NULL,
  slug      CITEXT                      UNIQUE,
  title     VARCHAR                     NOT NULL,
  votes     INTEGER                     DEFAULT 0
);


CREATE INDEX IF NOT EXISTS indexThreadUser ON Threads (author);
CREATE INDEX IF NOT EXISTS indexThreadForum ON Threads (forum);
CREATE UNIQUE INDEX IF NOT EXISTS indexUniqueSlugThread ON Threads (slug);


CREATE TABLE IF NOT EXISTS Posts (
  id        SERIAL                      NOT NULL PRIMARY KEY,
  author    VARCHAR                     NOT NULL REFERENCES Users(nickname),
  created   TIMESTAMP WITH TIME ZONE    DEFAULT current_timestamp,
  forum     VARCHAR,
  isEdited  BOOLEAN                     DEFAULT FALSE,
  message   TEXT                        NOT NULL,
  parent    INTEGER                     DEFAULT 0,
  thread    INTEGER                     NOT NULL REFERENCES Threads(id),
  path      BIGINT                      ARRAY
);


CREATE INDEX IF NOT EXISTS indexPostAuthor ON Posts (author);
CREATE INDEX IF NOT EXISTS indexPostForum ON Posts (forum);
CREATE INDEX IF NOT EXISTS indexPostThread ON Posts (thread);
CREATE INDEX IF NOT EXISTS indexPostCreated ON Posts (created);
CREATE INDEX IF NOT EXISTS indexPostPath ON Posts ((path [1]));
CREATE INDEX IF NOT EXISTS indexPostThreadCreateId ON Posts (thread, created, id);
CREATE INDEX IF NOT EXISTS indexPostParentThreadId ON Posts (parent, thread, id);
CREATE INDEX IF NOT EXISTS indexPostIdThread ON Posts (id, thread);
CREATE INDEX IF NOT EXISTS indexPostThreadPath ON Posts (thread, path);


CREATE TABLE IF NOT EXISTS Votes (
  id        SERIAL      NOT NULL PRIMARY KEY,
  username  VARCHAR     NOT NULL REFERENCES Users (nickname),
  voice     INTEGER,
  thread    INTEGER     NOT NULL REFERENCES Threads (id),
  UNIQUE(username, thread)
);