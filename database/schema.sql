-- ============================================================
--  EcoLearn Swing – MySQL Schema
--  Converted from MongoDB (GamEd MERN project)
-- ============================================================

CREATE DATABASE IF NOT EXISTS ecolearn_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ecolearn_db;

-- ──────────────────────────────────────────────
-- 1. USERS  (was MongoDB User model)
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(50)  NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,          -- BCrypt hash
    role         ENUM('student','teacher','admin') NOT NULL DEFAULT 'student',
    points       INT          NOT NULL DEFAULT 0,
    level        INT          NOT NULL DEFAULT 1,
    streak       INT          NOT NULL DEFAULT 0,
    last_active  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ──────────────────────────────────────────────
-- 2. QUIZZES  (was quizSchema)
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS quizzes (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    category     ENUM('math','science','coding','general','language','environment') NOT NULL DEFAULT 'environment',
    difficulty   ENUM('easy','medium','hard') NOT NULL DEFAULT 'easy',
    time_limit   INT          NOT NULL DEFAULT 10,   -- minutes
    total_points INT          NOT NULL DEFAULT 0,
    is_active    TINYINT(1)   NOT NULL DEFAULT 1,
    created_by   INT,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ──────────────────────────────────────────────
-- 3. QUESTIONS  (was embedded questionSchema)
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS questions (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id        INT          NOT NULL,
    question_text  TEXT         NOT NULL,
    option_a       VARCHAR(500) NOT NULL,
    option_b       VARCHAR(500) NOT NULL,
    option_c       VARCHAR(500) NOT NULL,
    option_d       VARCHAR(500) NOT NULL,
    correct_option TINYINT      NOT NULL,           -- 0=A,1=B,2=C,3=D
    explanation    TEXT,
    points         INT          NOT NULL DEFAULT 5,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- ──────────────────────────────────────────────
-- 4. SCORES  (was completedQuizzes embedded in User)
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS scores (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    quiz_id          INT NOT NULL,
    score            INT NOT NULL DEFAULT 0,
    correct_answers  INT NOT NULL DEFAULT 0,
    total_questions  INT NOT NULL DEFAULT 0,
    completed_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- ──────────────────────────────────────────────
-- 5. BADGES  (was badges array in User)
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS badges (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT          NOT NULL,
    name       VARCHAR(100) NOT NULL,
    icon       VARCHAR(10)  NOT NULL DEFAULT '🏅',
    earned_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ──────────────────────────────────────────────
-- SEED DATA  – default admin + sample quiz
-- ──────────────────────────────────────────────

-- Default admin user  (password = "admin123")
INSERT IGNORE INTO users (name, email, password, role, points, level)
VALUES ('Admin', 'admin@ecolearn.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHqK',
        'admin', 500, 5);

-- A sample Environmental Quiz
INSERT IGNORE INTO quizzes (id, title, description, category, difficulty, time_limit, total_points, is_active, created_by)
VALUES (1, 'Environmental Awareness Basics',
        'Test your knowledge about the environment and sustainability!',
        'environment', 'easy', 10, 25, 1, 1);

-- 5 sample questions for quiz 1
INSERT IGNORE INTO questions (quiz_id, question_text, option_a, option_b, option_c, option_d, correct_option, explanation, points) VALUES
(1, 'What is the main cause of global warming?',
 'Volcanic eruptions', 'Greenhouse gas emissions', 'Ocean currents', 'Solar flares',
 1, 'Greenhouse gases like CO2 trap heat in the atmosphere, driving global warming.', 5),

(1, 'Which gas makes up the largest portion of Earth''s atmosphere?',
 'Oxygen', 'Carbon Dioxide', 'Nitrogen', 'Argon',
 2, 'Nitrogen makes up about 78% of Earth''s atmosphere.', 5),

(1, 'What does "reduce, reuse, recycle" primarily aim to reduce?',
 'Air pollution', 'Noise pollution', 'Solid waste', 'Water pollution',
 2, 'The 3Rs target reduction of solid waste going to landfills.', 5),

(1, 'Which renewable energy source harnesses the power of the sun?',
 'Wind energy', 'Hydropower', 'Solar energy', 'Geothermal energy',
 2, 'Solar panels convert sunlight directly into electricity.', 5),

(1, 'What percentage of Earth''s water is fresh water?',
 'About 71%', 'About 50%', 'About 3%', 'About 20%',
 2, 'Only around 3% of Earth''s water is freshwater, and most of that is frozen.', 5);

-- Update quiz total points
UPDATE quizzes SET total_points = 25 WHERE id = 1;
