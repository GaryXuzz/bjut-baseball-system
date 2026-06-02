# Phase 2 Database Design Notes

## Purpose

This document records the Phase 2 database improvements for the BJUT Baseball Team Management and Match Statistics Database System.

The final Java Web implementation uses the formal MySQL schema in `sql/phase2_mysql_schema.sql`.

## Response to Phase 1 Feedback

### 1. More formal relational schema

Phase 1 only listed relation names and attributes. Phase 2 now provides complete MySQL `CREATE TABLE` statements with:

- concrete data types
- primary keys
- foreign keys
- `NOT NULL` constraints
- `UNIQUE` constraints
- `CHECK` constraints
- `ON UPDATE` and `ON DELETE` referential actions
- indexes for common search operations

### 2. Better justification for profile tables

In Phase 1, `PITCHER_PROFILE` and `FIELDER_PROFILE` only contained `player_id`, so they did not add enough information.

In the Phase 2 implementation, these two tables are meaningful summary tables:

- `fielder_profiles` stores batting and fielding totals, including at-bats, hits, runs, RBIs, walks, strikeouts, extra-base hits, stolen bases, fielding errors, batting average, OBP, SLG, and OPS.
- `pitcher_profiles` stores pitching totals, including innings pitched, hits allowed, earned runs, walks allowed, strikeouts, pitches, strikes, ERA, WHIP, strike percentage, and wild pitches.

The detailed transaction data remains in `game_records`. The profile tables are derived from these game records and are used to support efficient ranking and statistics pages.

### 3. Clearer handling of the many-to-many position relationship

A player can have more than one position, and one position can belong to many players. This is implemented with:

- `players`
- `positions`
- `player_positions`

The `player_positions` table uses a composite primary key, which prevents duplicate player-position pairs.

### 4. Mandatory and optional fields

The schema now makes required data explicit:

- `players.name`, `players.jersey_number`, `players.primary_position`, and `players.join_date` are mandatory.
- `game_records.player_id`, `game_records.game_date`, and `game_records.opponent` are mandatory.
- profile statistics are mandatory numeric fields with default value `0`.
- optional descriptive data such as `birth_date`, `contact_phone`, and `positions.description` can be `NULL`.

## Normalization

The design separates core entities into different tables:

- `players` stores player identity and basic profile data.
- `positions` stores the position dictionary.
- `player_positions` resolves the many-to-many relationship between players and positions.
- `game_records` stores per-player, per-game batting or pitching records.
- `users` stores administrator login data.

This removes repeating groups and avoids storing multiple positions in a single text field. It also avoids duplicating position names across player rows.

The profile tables are controlled summary tables. They are not the primary source of match data; they are derived from `game_records` so that the application can show leaderboards and aggregate statistics efficiently. This is an intentional implementation trade-off, and the application includes synchronization logic to keep the profile totals consistent with the detailed records.

## Main Relationships

- One player can have many game records.
- One player has one fielder profile.
- A pitcher player can also have one pitcher profile.
- One player can have many positions.
- One position can be assigned to many players.
- Administrator users can log in to perform write operations such as adding, editing, and deleting players or game records.

## Integrity Rules

The MySQL schema includes business rules such as:

- player names and jersey numbers cannot be empty.
- usernames must be unique.
- position names must be unique.
- hits cannot exceed at-bats.
- doubles, triples, and home runs cannot exceed total hits.
- strikes cannot exceed total pitches.
- earned runs cannot exceed total runs allowed.
- a pitching record cannot be both a win and a loss.

These constraints make the final database more reliable than the Phase 1 draft and directly address the teacher's comments about missing constraints.
