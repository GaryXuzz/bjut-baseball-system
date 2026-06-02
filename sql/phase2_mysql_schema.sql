-- POWER ARENA / BJUT Baseball System
-- Phase 2 final MySQL schema
-- Target DBMS: MySQL 8.0+

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS game_records;
DROP TABLE IF EXISTS pitcher_profiles;
DROP TABLE IF EXISTS fielder_profiles;
DROP TABLE IF EXISTS player_positions;
DROP TABLE IF EXISTS positions;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS players;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE players (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    jersey_number VARCHAR(10) NOT NULL,
    primary_position VARCHAR(50) NOT NULL DEFAULT '未指定',
    is_pitcher BOOLEAN NOT NULL DEFAULT FALSE,
    birth_date DATE NULL,
    join_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    contact_phone VARCHAR(20) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_players_name_jersey (name, jersey_number),
    CHECK (name <> ''),
    CHECK (jersey_number <> '')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE users (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    is_admin BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    CHECK (username <> ''),
    CHECK (password_hash <> '')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE positions (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL,
    description VARCHAR(100) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_positions_name (name),
    CHECK (name <> '')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE player_positions (
    player_id INT UNSIGNED NOT NULL,
    position_id INT UNSIGNED NOT NULL,
    PRIMARY KEY (player_id, position_id),
    CONSTRAINT fk_player_positions_player
        FOREIGN KEY (player_id) REFERENCES players (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_player_positions_position
        FOREIGN KEY (position_id) REFERENCES positions (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE fielder_profiles (
    player_id INT UNSIGNED NOT NULL,
    at_bats_total INT UNSIGNED NOT NULL DEFAULT 0,
    runs_total INT UNSIGNED NOT NULL DEFAULT 0,
    hits_total INT UNSIGNED NOT NULL DEFAULT 0,
    rbi_total INT UNSIGNED NOT NULL DEFAULT 0,
    walks_total INT UNSIGNED NOT NULL DEFAULT 0,
    strikeouts_batting_total INT UNSIGNED NOT NULL DEFAULT 0,
    batting_average DECIMAL(5,3) NOT NULL DEFAULT 0.000,
    on_base_percentage DECIMAL(5,3) NOT NULL DEFAULT 0.000,
    slugging_percentage DECIMAL(5,3) NOT NULL DEFAULT 0.000,
    ops DECIMAL(5,3) NOT NULL DEFAULT 0.000,
    doubles INT UNSIGNED NOT NULL DEFAULT 0,
    triples INT UNSIGNED NOT NULL DEFAULT 0,
    home_runs_batting INT UNSIGNED NOT NULL DEFAULT 0,
    total_bases INT UNSIGNED NOT NULL DEFAULT 0,
    hit_by_pitch INT UNSIGNED NOT NULL DEFAULT 0,
    stolen_bases INT UNSIGNED NOT NULL DEFAULT 0,
    caught_stealing INT UNSIGNED NOT NULL DEFAULT 0,
    sacrifice_flys INT UNSIGNED NOT NULL DEFAULT 0,
    sacrifice_hits INT UNSIGNED NOT NULL DEFAULT 0,
    errors_fielding INT UNSIGNED NOT NULL DEFAULT 0,
    passed_balls INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id),
    CONSTRAINT fk_fielder_profiles_player
        FOREIGN KEY (player_id) REFERENCES players (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CHECK (hits_total <= at_bats_total),
    CHECK (doubles + triples + home_runs_batting <= hits_total),
    CHECK (batting_average BETWEEN 0.000 AND 1.000),
    CHECK (on_base_percentage BETWEEN 0.000 AND 1.000),
    CHECK (slugging_percentage >= 0.000),
    CHECK (ops >= 0.000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE pitcher_profiles (
    player_id INT UNSIGNED NOT NULL,
    innings_pitched_total DECIMAL(5,1) NOT NULL DEFAULT 0.0,
    hits_allowed_total INT UNSIGNED NOT NULL DEFAULT 0,
    runs_allowed_total INT UNSIGNED NOT NULL DEFAULT 0,
    earned_runs_total INT UNSIGNED NOT NULL DEFAULT 0,
    walks_allowed_total INT UNSIGNED NOT NULL DEFAULT 0,
    strikeouts_total INT UNSIGNED NOT NULL DEFAULT 0,
    home_runs_allowed_total INT UNSIGNED NOT NULL DEFAULT 0,
    pitches INT UNSIGNED NOT NULL DEFAULT 0,
    strikes INT UNSIGNED NOT NULL DEFAULT 0,
    hit_by_pitch_allowed INT UNSIGNED NOT NULL DEFAULT 0,
    batters_faced INT UNSIGNED NOT NULL DEFAULT 0,
    era DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    whip DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    strike_percentage DECIMAL(5,1) NOT NULL DEFAULT 0.0,
    wild_pitches INT UNSIGNED NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id),
    CONSTRAINT fk_pitcher_profiles_player
        FOREIGN KEY (player_id) REFERENCES players (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CHECK (strikes <= pitches),
    CHECK (innings_pitched_total >= 0.0),
    CHECK (era >= 0.00),
    CHECK (whip >= 0.00),
    CHECK (strike_percentage BETWEEN 0.0 AND 100.0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE game_records (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    player_id INT UNSIGNED NOT NULL,
    game_date DATE NOT NULL,
    opponent VARCHAR(100) NOT NULL,
    is_pitching_record BOOLEAN NOT NULL DEFAULT FALSE,

    at_bats INT UNSIGNED NOT NULL DEFAULT 0,
    runs INT UNSIGNED NOT NULL DEFAULT 0,
    hits INT UNSIGNED NOT NULL DEFAULT 0,
    rbi INT UNSIGNED NOT NULL DEFAULT 0,
    walks INT UNSIGNED NOT NULL DEFAULT 0,
    strikeouts INT UNSIGNED NOT NULL DEFAULT 0,
    doubles INT UNSIGNED NOT NULL DEFAULT 0,
    triples INT UNSIGNED NOT NULL DEFAULT 0,
    home_runs_batting INT UNSIGNED NOT NULL DEFAULT 0,
    total_bases INT UNSIGNED NOT NULL DEFAULT 0,
    hit_by_pitch INT UNSIGNED NOT NULL DEFAULT 0,
    stolen_bases INT UNSIGNED NOT NULL DEFAULT 0,
    caught_stealing INT UNSIGNED NOT NULL DEFAULT 0,
    sacrifice_flys INT UNSIGNED NOT NULL DEFAULT 0,
    sacrifice_hits INT UNSIGNED NOT NULL DEFAULT 0,

    pitches INT UNSIGNED NOT NULL DEFAULT 0,
    strikes INT UNSIGNED NOT NULL DEFAULT 0,
    hit_by_pitch_allowed INT UNSIGNED NOT NULL DEFAULT 0,
    batters_faced INT UNSIGNED NOT NULL DEFAULT 0,
    wild_pitches INT UNSIGNED NOT NULL DEFAULT 0,
    innings_pitched DECIMAL(4,1) NOT NULL DEFAULT 0.0,
    hits_allowed INT UNSIGNED NOT NULL DEFAULT 0,
    runs_allowed INT UNSIGNED NOT NULL DEFAULT 0,
    earned_runs INT UNSIGNED NOT NULL DEFAULT 0,
    walks_allowed INT UNSIGNED NOT NULL DEFAULT 0,
    strikeouts_pitched INT UNSIGNED NOT NULL DEFAULT 0,
    home_runs_allowed INT UNSIGNED NOT NULL DEFAULT 0,

    errors_fielding INT UNSIGNED NOT NULL DEFAULT 0,
    passed_balls INT UNSIGNED NOT NULL DEFAULT 0,

    win BOOLEAN NOT NULL DEFAULT FALSE,
    loss BOOLEAN NOT NULL DEFAULT FALSE,
    save BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (id),
    KEY idx_game_records_player_date (player_id, game_date),
    KEY idx_game_records_opponent_date (opponent, game_date),
    CONSTRAINT fk_game_records_player
        FOREIGN KEY (player_id) REFERENCES players (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CHECK (opponent <> ''),
    CHECK (hits <= at_bats),
    CHECK (doubles + triples + home_runs_batting <= hits),
    CHECK (strikes <= pitches),
    CHECK (innings_pitched >= 0.0),
    CHECK (earned_runs <= runs_allowed),
    CHECK (NOT (win = TRUE AND loss = TRUE))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO positions (name, description) VALUES
    ('投手', 'Pitcher'),
    ('捕手', 'Catcher'),
    ('一垒手', 'First baseman'),
    ('二垒手', 'Second baseman'),
    ('三垒手', 'Third baseman'),
    ('游击手', 'Shortstop'),
    ('左外野手', 'Left fielder'),
    ('中外野手', 'Center fielder'),
    ('右外野手', 'Right fielder');
