CREATE EXTENSION IF NOT EXISTS citext;

DROP TABLE IF EXISTS Votes CASCADE;
DROP TABLE IF EXISTS Posts CASCADE;
DROP TABLE IF EXISTS Threads CASCADE;
DROP TABLE IF EXISTS Forums CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

CREATE TABLE IF NOT EXISTS Users (
  nickname CITEXT NOT NULL PRIMARY KEY,
  fullname VARCHAR,
  about    TEXT,
  email    CITEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Forums (
  id       SERIAL  NOT NULL PRIMARY KEY,
  title    VARCHAR NOT NULL,
  username CITEXT  NOT NULL REFERENCES Users (nickname),
  slug     CITEXT  NOT NULL UNIQUE,
  posts    INTEGER DEFAULT 0,
  threads  INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS Threads (
  id      SERIAL                   NOT NULL PRIMARY KEY,
  author  CITEXT                   NOT NULL REFERENCES Users (nickname),
  created TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
  forum   CITEXT                   NOT NULL REFERENCES Forums (slug),
  message TEXT                     NOT NULL,
  slug    CITEXT UNIQUE,
  title   VARCHAR                  NOT NULL,
  votes   INTEGER DEFAULT 0
);


CREATE TABLE IF NOT EXISTS Posts (
  id       SERIAL                   NOT NULL PRIMARY KEY,
  author   CITEXT                   NOT NULL REFERENCES Users (nickname),
  created  TIMESTAMP WITH TIME ZONE NOT NULL,
  forum    VARCHAR,
  isEdited BOOLEAN DEFAULT FALSE,
  message  TEXT                     NOT NULL,
  parent   INTEGER DEFAULT 0,
  thread   INTEGER                  NOT NULL REFERENCES Threads (id),
  path     INTEGER ARRAY
);


CREATE TABLE IF NOT EXISTS Votes (
  id       SERIAL  NOT NULL PRIMARY KEY,
  username CITEXT  NOT NULL REFERENCES Users (nickname),
  voice    INTEGER,
  thread   INTEGER NOT NULL REFERENCES Threads (id),
  UNIQUE (username, thread)
);

CREATE INDEX forums_username_idx ON Forums (username);
CREATE INDEX threads_forum_idx ON Threads (forum);
CREATE INDEX threads_author_idx ON Threads (author);
CREATE INDEX posts_forum_idx ON Posts (forum);
CREATE INDEX posts_author_idx ON Posts (author);
CREATE INDEX posts_thread_idx ON Posts (thread);
CREATE INDEX posts_path_idx ON Posts (path);
CREATE INDEX posts_thread_id_idx ON posts(thread, id);
CREATE INDEX posts_thread_path_idx ON posts(thread, path);
CREATE INDEX posts_thread_path_1_idx ON posts(thread, (path[1]));
CREATE INDEX posts_path_1_idx ON Posts ((path[1]));
