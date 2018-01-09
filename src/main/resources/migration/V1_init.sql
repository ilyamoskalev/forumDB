CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;

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
  nickname CITEXT COLLATE "ucs_basic" NOT NULL UNIQUE,
  fullname VARCHAR,
  about    TEXT,
  email    CITEXT COLLATE "ucs_basic" NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS users_lower_nickname_idx
  ON Users (LOWER(nickname));
CREATE INDEX IF NOT EXISTS users_nickname_idx
  ON Users (nickname);
CREATE INDEX IF NOT EXISTS users_email_nickname_idx
  ON Users (LOWER(nickname), LOWER(email));

CREATE TABLE IF NOT EXISTS Forums (
  id       SERIAL                     NOT NULL PRIMARY KEY,
  title    VARCHAR                    NOT NULL,
  username CITEXT                     NOT NULL REFERENCES Users (nickname),
  slug     CITEXT COLLATE "ucs_basic" NOT NULL UNIQUE,
  posts    INTEGER DEFAULT 0,
  threads  INTEGER DEFAULT 0
);

CREATE INDEX IF NOT EXISTS forums_slug_idx
  ON Forums (LOWER(slug));

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

CREATE INDEX IF NOT EXISTS threads_slug_idx
  ON Threads (LOWER(slug));
CREATE INDEX IF NOT EXISTS threads_forum_idx
  ON Threads (LOWER(forum));


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

CREATE INDEX IF NOT EXISTS posts_thread_idx
  ON Posts (thread);
CREATE INDEX IF NOT EXISTS posts_created_idx
  ON Posts (created);
CREATE INDEX IF NOT EXISTS posts_path_idx
  ON Posts (path);
CREATE INDEX IF NOT EXISTS posts_path1_thread_idx
  ON Posts ((path [1]), thread);
CREATE INDEX IF NOT EXISTS posts_parent_thread_idx
  ON Posts (parent, thread);
CREATE INDEX IF NOT EXISTS posts_path1_idx
  ON Posts ((path [1]));

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

CREATE INDEX IF NOT EXISTS boost_slug_idx
  ON Boost (LOWER(slug));
CREATE INDEX IF NOT EXISTS boost_username_idx
  ON Boost (username);
CREATE INDEX IF NOT EXISTS votes_username_thread_idx
  ON Votes (thread, username);

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

CREATE TRIGGER add_user_after_insert_thread
  AFTER INSERT
  ON Threads
  FOR EACH ROW
EXECUTE PROCEDURE add_user_to_boost();

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

CREATE TRIGGER add_path_after_insert_post
  BEFORE INSERT
  ON Posts
  FOR EACH ROW
EXECUTE PROCEDURE add_path();

CREATE FUNCTION thread_inc() RETURNS trigger
LANGUAGE plpgsql
AS $$BEGIN
  UPDATE forums SET threads = threads + 1
  WHERE slug = NEW.forum;
  RETURN NEW;
END$$;

CREATE TRIGGER thread_inc
AFTER INSERT
  ON Threads
FOR EACH ROW
EXECUTE PROCEDURE thread_inc();

CREATE FUNCTION post_inc() RETURNS trigger
LANGUAGE plpgsql
AS $$BEGIN
  UPDATE forums SET posts = posts + 1
  WHERE slug = NEW.forum;
  RETURN NEW;
END$$;

CREATE TRIGGER post_inc
AFTER INSERT
  ON Posts
FOR EACH ROW
EXECUTE PROCEDURE post_inc();


