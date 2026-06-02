-- Optional demo data for the Java Web version.
-- Run after sql/phase2_mysql_schema.sql.

INSERT INTO users (username, password_hash, is_admin)
VALUES ('admin', 'admin123', TRUE)
ON DUPLICATE KEY UPDATE is_admin = TRUE;

INSERT INTO players (name, jersey_number, primary_position, is_pitcher, join_date) VALUES
    ('Zhang San', '18', '投手', TRUE, CURRENT_DATE),
    ('Li Si', '10', '左外野手', FALSE, CURRENT_DATE),
    ('Wang Wu', '6', '游击手', FALSE, CURRENT_DATE),
    ('Zhao Liu', '2', '捕手', FALSE, CURRENT_DATE)
ON DUPLICATE KEY UPDATE
    primary_position = VALUES(primary_position),
    is_pitcher = VALUES(is_pitcher);

INSERT IGNORE INTO player_positions (player_id, position_id)
SELECT p.id, pos.id
FROM players p
JOIN positions pos ON pos.name = p.primary_position
WHERE p.name IN ('Zhang San', 'Li Si', 'Wang Wu', 'Zhao Liu');

INSERT IGNORE INTO fielder_profiles (player_id)
SELECT id FROM players WHERE name IN ('Zhang San', 'Li Si', 'Wang Wu', 'Zhao Liu');

INSERT IGNORE INTO pitcher_profiles (player_id)
SELECT id FROM players WHERE name = 'Zhang San';

DELETE gr FROM game_records gr
JOIN players p ON gr.player_id = p.id
WHERE p.name IN ('Li Si', 'Wang Wu', 'Zhang San')
  AND gr.game_date = '2026-05-01'
  AND gr.opponent = 'Tsinghua University';

INSERT INTO game_records
(player_id, game_date, opponent, is_pitching_record, at_bats, hits, runs, rbi, walks, strikeouts, doubles, total_bases)
SELECT id, '2026-05-01', 'Tsinghua University', FALSE, 4, 2, 1, 2, 1, 0, 1, 3
FROM players WHERE name = 'Li Si';

INSERT INTO game_records
(player_id, game_date, opponent, is_pitching_record, at_bats, hits, runs, rbi, walks, strikeouts, doubles, total_bases)
SELECT id, '2026-05-01', 'Tsinghua University', FALSE, 5, 3, 2, 1, 0, 1, 0, 3
FROM players WHERE name = 'Wang Wu';

INSERT INTO game_records
(player_id, game_date, opponent, is_pitching_record, innings_pitched, hits_allowed, runs_allowed, earned_runs, walks_allowed, strikeouts_pitched, pitches, strikes)
SELECT id, '2026-05-01', 'Tsinghua University', TRUE, 6.0, 4, 2, 1, 2, 7, 88, 58
FROM players WHERE name = 'Zhang San';

UPDATE fielder_profiles fp
JOIN (
    SELECT player_id,
           COALESCE(SUM(at_bats), 0) at_bats_total,
           COALESCE(SUM(runs), 0) runs_total,
           COALESCE(SUM(hits), 0) hits_total,
           COALESCE(SUM(rbi), 0) rbi_total,
           COALESCE(SUM(walks), 0) walks_total,
           COALESCE(SUM(strikeouts), 0) strikeouts_total,
           COALESCE(SUM(doubles), 0) doubles,
           COALESCE(SUM(total_bases), 0) total_bases
    FROM game_records
    WHERE is_pitching_record = FALSE
    GROUP BY player_id
) s ON fp.player_id = s.player_id
SET fp.at_bats_total = s.at_bats_total,
    fp.runs_total = s.runs_total,
    fp.hits_total = s.hits_total,
    fp.rbi_total = s.rbi_total,
    fp.walks_total = s.walks_total,
    fp.strikeouts_batting_total = s.strikeouts_total,
    fp.doubles = s.doubles,
    fp.total_bases = s.total_bases,
    fp.batting_average = IF(s.at_bats_total > 0, ROUND(s.hits_total / s.at_bats_total, 3), 0),
    fp.slugging_percentage = IF(s.at_bats_total > 0, ROUND(s.total_bases / s.at_bats_total, 3), 0),
    fp.on_base_percentage = IF(s.at_bats_total + s.walks_total > 0, ROUND((s.hits_total + s.walks_total) / (s.at_bats_total + s.walks_total), 3), 0),
    fp.ops =
        IF(s.at_bats_total + s.walks_total > 0, ROUND((s.hits_total + s.walks_total) / (s.at_bats_total + s.walks_total), 3), 0)
        + IF(s.at_bats_total > 0, ROUND(s.total_bases / s.at_bats_total, 3), 0);

UPDATE pitcher_profiles pp
JOIN (
    SELECT player_id,
           COALESCE(SUM(innings_pitched), 0) innings,
           COALESCE(SUM(hits_allowed), 0) hits_allowed,
           COALESCE(SUM(runs_allowed), 0) runs_allowed,
           COALESCE(SUM(earned_runs), 0) earned_runs,
           COALESCE(SUM(walks_allowed), 0) walks_allowed,
           COALESCE(SUM(strikeouts_pitched), 0) strikeouts,
           COALESCE(SUM(pitches), 0) pitches,
           COALESCE(SUM(strikes), 0) strikes
    FROM game_records
    WHERE is_pitching_record = TRUE
    GROUP BY player_id
) s ON pp.player_id = s.player_id
SET pp.innings_pitched_total = s.innings,
    pp.hits_allowed_total = s.hits_allowed,
    pp.runs_allowed_total = s.runs_allowed,
    pp.earned_runs_total = s.earned_runs,
    pp.walks_allowed_total = s.walks_allowed,
    pp.strikeouts_total = s.strikeouts,
    pp.pitches = s.pitches,
    pp.strikes = s.strikes,
    pp.era = IF(s.innings > 0, ROUND((s.earned_runs * 9) / s.innings, 2), 0),
    pp.whip = IF(s.innings > 0, ROUND((s.walks_allowed + s.hits_allowed) / s.innings, 2), 0),
    pp.strike_percentage = IF(s.pitches > 0, ROUND((s.strikes / s.pitches) * 100, 1), 0);
