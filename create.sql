-- ──────────────────────────────
--  StudyHub - DDL (MySQL 8.x)
--  ENGINE = InnoDB / utf8mb4
-- ──────────────────────────────
SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;

-- ── users ─────────────────────
CREATE TABLE users (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    is_blocked       BOOLEAN       NOT NULL,
    created_at       DATETIME(6),
    role             VARCHAR(20),
    name             VARCHAR(50)   NOT NULL,
    username         VARCHAR(50)   NOT NULL UNIQUE,
    email            VARCHAR(100)  UNIQUE,
    major            VARCHAR(100),
    password         VARCHAR(100)  NOT NULL,
    university       VARCHAR(100),
    education_status ENUM ('DROPPED_OUT','ENROLLED','GRADUATED','HIGH_SCHOOL','LEAVE_OF_ABSENCE')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── boards ────────────────────
CREATE TABLE boards (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at  DATETIME(6) NOT NULL,
    description VARCHAR(255),
    name        VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── posts ─────────────────────
CREATE TABLE posts (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    dislike_count INT          NOT NULL,
    is_notice     BOOLEAN      NOT NULL,
    like_count    INT          NOT NULL,
    view_count    INT          NOT NULL,
    board_id      BIGINT       NOT NULL,
    user_id       BIGINT       NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6),
    content       TEXT         NOT NULL,
    title         VARCHAR(255) NOT NULL,
    CONSTRAINT fk_post_board  FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_user   FOREIGN KEY (user_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── post_likes ────────────────
CREATE TABLE post_likes (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    post_id   BIGINT       NOT NULL,
    user_id   BIGINT       NOT NULL,
    type      ENUM('DISLIKE','LIKE') NOT NULL,
    UNIQUE KEY uk_post_user (post_id, user_id),
    CONSTRAINT fk_postlike_post  FOREIGN KEY (post_id) REFERENCES posts(id)  ON DELETE CASCADE,
    CONSTRAINT fk_postlike_user  FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── post_comments ─────────────
CREATE TABLE post_comments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    like_count INT         NOT NULL,
    created_at DATETIME(6) NOT NULL,
    parent_id  BIGINT,
    post_id    BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    content    TEXT        NOT NULL,
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES post_comments(id),
    CONSTRAINT fk_comment_post   FOREIGN KEY (post_id)   REFERENCES posts(id)  ON DELETE CASCADE,
    CONSTRAINT fk_comment_user   FOREIGN KEY (user_id)   REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── reports ───────────────────
CREATE TABLE reports (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    reported_at   DATETIME(6) NOT NULL,
    resolved_at   DATETIME(6),
    reporter_id   BIGINT,
    resolver_id   BIGINT,
    target_id     BIGINT       NOT NULL,
    description   TEXT,
    resolution_note TEXT,
    reason        ENUM ('COPYRIGHT_VIOLATION','HARASSMENT','INAPPROPRIATE_CONTENT','OTHER','SPAM') NOT NULL,
    status        ENUM ('APPROVED','PENDING','REJECTED','RESOLVED') NOT NULL,
    target_type   ENUM ('COMMENT','POST','USER') NOT NULL,
    CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT fk_report_resolver FOREIGN KEY (resolver_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── studies ───────────────────
CREATE TABLE studies (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    leader_id         BIGINT       NOT NULL,
    deadline          DATE,
    recruitment_limit INT,
    created_at        DATETIME(6),
    description       TEXT,
    requirement       TEXT,
    title             VARCHAR(255) NOT NULL,
    CONSTRAINT fk_study_leader FOREIGN KEY (leader_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── study_applications ────────
CREATE TABLE study_applications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id    BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    applied_at  DATETIME(6),
    status      ENUM ('APPROVED','PENDING','REJECTED') NOT NULL,
    UNIQUE KEY uk_user_study (user_id, study_id),
    CONSTRAINT fk_application_study FOREIGN KEY (study_id) REFERENCES studies(id),
    CONSTRAINT fk_application_user  FOREIGN KEY (user_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── study_comments ────────────
CREATE TABLE study_comments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id   BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    content    TEXT        NOT NULL,
    CONSTRAINT fk_studycomment_study FOREIGN KEY (study_id) REFERENCES studies(id),
    CONSTRAINT fk_studycomment_user  FOREIGN KEY (user_id)  REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET foreign_key_checks = 1;