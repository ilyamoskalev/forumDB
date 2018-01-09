CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;

CREATE TABLE IF NOT EXISTS Users (
  nickname CITEXT COLLATE "ucs_basic" NOT NULL UNIQUE,
  fullname VARCHAR,
  about    TEXT,
  email    CITEXT COLLATE "ucs_basic" NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Forums (
  id       SERIAL                     NOT NULL PRIMARY KEY,
  title    VARCHAR                    NOT NULL,
  username CITEXT                     NOT NULL REFERENCES Users (nickname),
  slug     CITEXT COLLATE "ucs_basic" NOT NULL UNIQUE,
  posts    INTEGER DEFAULT 0,
  threads  INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS Threads (
  id      SERIAL NOT NULL PRIMARY KEY,
  author  CITEXT NOT NULL REFERENCES Users (nickname),
  created TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
  forum   CITEXT NOT NULL REFERENCES Forums (slug),
  message TEXT   NOT NULL,
  slug    CITEXT COLLATE "ucs_basic" UNIQUE,
  title   TEXT   NOT NULL,
  votes   INTEGER                  DEFAULT 0
);

CREATE TABLE IF NOT EXISTS Posts (
  id       SERIAL                   NOT NULL PRIMARY KEY,
  author   CITEXT                   NOT NULL REFERENCES Users (nickname),
  created  TIMESTAMP WITH TIME ZONE NOT NULL,
  forum    CITEXT REFERENCES Forums (slug),
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

CREATE TABLE IF NOT EXISTS Boost (
  username CITEXT NOT NULL REFERENCES Users (nickname),
  slug     CITEXT NOT NULL REFERENCES Forums (slug),
  UNIQUE (username, slug)
);

CREATE OR REPLACE FUNCTION add_user_to_boost()
  RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO Boost (username, slug) VALUES (NEW.author, NEW.forum)
  ON CONFLICT DO NOTHING;
  RETURN NEW;
END
$$;

DROP TRIGGER add_user_after_insert_thread ON Threads;
CREATE TRIGGER add_user_after_insert_thread
  AFTER INSERT
  ON Threads
  FOR EACH ROW
EXECUTE PROCEDURE add_user_to_boost();

DROP TRIGGER add_user_after_insert_thread ON Posts;
CREATE TRIGGER add_user_after_insert_thread
  AFTER INSERT
  ON Posts
  FOR EACH ROW
EXECUTE PROCEDURE add_user_to_boost();


CREATE OR REPLACE FUNCTION add_path() RETURNS trigger
LANGUAGE plpgsql
AS $$BEGIN
  NEW.path=array_append((SELECT path FROM posts WHERE id = NEW.parent), NEW.id);
  RETURN NEW;
END$$;

DROP TRIGGER add_path_after_insert_post ON Posts;
CREATE TRIGGER add_path_after_insert_post
  BEFORE INSERT
  ON Posts
  FOR EACH ROW
EXECUTE PROCEDURE add_path();


CREATE OR REPLACE FUNCTION thread_inc() RETURNS trigger
LANGUAGE plpgsql
AS $$BEGIN
  UPDATE forums SET threads = threads + 1
  WHERE slug = NEW.forum;
  RETURN NEW;
END$$;

DROP TRIGGER thread_inc ON Threads;
CREATE TRIGGER thread_inc
AFTER INSERT
  ON Threads
FOR EACH ROW
EXECUTE PROCEDURE thread_inc();


CREATE OR REPLACE FUNCTION post_inc() RETURNS trigger
LANGUAGE plpgsql
AS $$BEGIN
  UPDATE forums SET posts = posts + 1
  WHERE slug = NEW.forum;
  RETURN NEW;
END$$;

DROP TRIGGER post_inc ON Posts;
CREATE TRIGGER post_inc
AFTER INSERT
  ON Posts
FOR EACH ROW
EXECUTE PROCEDURE post_inc();


