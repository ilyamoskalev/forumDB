CREATE UNIQUE INDEX IF NOT EXISTS users_lower_nickname_idx ON Users (LOWER(nickname));
CREATE UNIQUE INDEX IF NOT EXISTS users_lower_email_idx ON Users (LOWER(email));
CREATE INDEX IF NOT EXISTS posts_path1_idx ON Posts ((path [1]));
CREATE INDEX IF NOT EXISTS posts_path1_thread_idx ON Posts (thread, (path [1]));

CREATE INDEX IF NOT EXISTS boost_username_idx ON Boost (LOWER(username));

CREATE INDEX IF NOT EXISTS posts_thread_idx ON Posts (thread);
CREATE INDEX IF NOT EXISTS forums_slug_idx ON Forums (LOWER(slug));
CREATE INDEX IF NOT EXISTS threads_slug_idx ON Threads (LOWER(slug));
CREATE INDEX IF NOT EXISTS posts_parent_thread_idx ON Posts (thread, parent);
CREATE INDEX IF NOT EXISTS posts_path_thread_idx ON Posts (thread, path);
CREATE INDEX IF NOT EXISTS boost_slug_idx ON Boost (LOWER(slug), LOWER(username));
CREATE INDEX IF NOT EXISTS threads_forum_created_idx ON Threads (LOWER(forum), created);

CREATE INDEX IF NOT EXISTS votes_username_thread_idx ON Votes (username, thread);