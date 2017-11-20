CREATE EXTENSION IF NOT EXISTS citext;

DROP TABLE IF EXISTS Votes CASCADE;
DROP TABLE IF EXISTS Posts CASCADE;
DROP TABLE IF EXISTS Threads CASCADE;
DROP TABLE IF EXISTS Forums CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

DROP INDEX IF EXISTS users_lower_nickname_idx;
DROP INDEX IF EXISTS users_nickname_idx;
DROP INDEX IF EXISTS users_email_nickname_idx;
DROP INDEX IF EXISTS forums_slug_idx;
DROP INDEX IF EXISTS threads_slug_idx;
DROP INDEX IF EXISTS threads_forum_idx;
DROP INDEX IF EXISTS posts_thread_idx;
DROP INDEX IF EXISTS posts_created_idx;
DROP INDEX IF EXISTS posts_path_idx;
DROP INDEX IF EXISTS posts_path1_thread_idx;
DROP INDEX IF EXISTS posts_parent_thread_idx;
DROP INDEX IF EXISTS votes_username_thread_idx;

CREATE TABLE IF NOT EXISTS Users (
  nickname CITEXT NOT NULL PRIMARY KEY,
  fullname VARCHAR,
  about    TEXT,
  email    CITEXT NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS users_lower_nickname_idx ON Users(LOWER(nickname));
CREATE INDEX IF NOT EXISTS users_nickname_idx ON Users(nickname);
CREATE INDEX IF NOT EXISTS users_email_nickname_idx ON Users(LOWER(nickname),LOWER(email));

  CREATE TABLE IF NOT EXISTS Forums (
  id       SERIAL  NOT NULL PRIMARY KEY,
  title    VARCHAR NOT NULL,
  username CITEXT  NOT NULL REFERENCES Users (nickname),
  slug     CITEXT  NOT NULL UNIQUE,
  posts    INTEGER DEFAULT 0,
  threads  INTEGER DEFAULT 0
);

CREATE INDEX IF NOT EXISTS forums_slug_idx ON Forums(LOWER(slug));

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

CREATE INDEX IF NOT EXISTS threads_slug_idx ON Threads(LOWER(slug));
CREATE INDEX IF NOT EXISTS threads_forum_idx ON Threads(LOWER(forum));


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

CREATE INDEX IF NOT EXISTS posts_thread_idx ON Posts(thread);
CREATE INDEX IF NOT EXISTS posts_created_idx ON Posts(created);
CREATE INDEX IF NOT EXISTS posts_path_idx ON Posts(path);
CREATE INDEX IF NOT EXISTS posts_path1_thread_idx ON Posts((path[1]), thread);
CREATE INDEX IF NOT EXISTS posts_parent_thread_idx ON Posts(parent, thread);
-- CREATE INDEX IF NOT EXISTS posts_thread_idx ON Posts(thread, created);

CREATE TABLE IF NOT EXISTS Votes (
  id       SERIAL  NOT NULL PRIMARY KEY,
  username CITEXT  NOT NULL REFERENCES Users (nickname),
  voice    INTEGER,
  thread   INTEGER NOT NULL REFERENCES Threads (id),
  UNIQUE (username, thread)
);

CREATE INDEX IF NOT EXISTS votes_username_thread_idx ON Votes(thread, username);


