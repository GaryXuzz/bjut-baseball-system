# Phase 2 Demonstration Script

Target length: under 8 minutes.

## Before Recording

Prepare the following:

- start the Java Web app with `java-web/run.sh` or `java-web/run.bat`
- open `http://localhost:8080/`
- open the Phase 2 report diagrams
- prepare administrator account: `admin / admin123`
- prepare one test player name, for example `Demo Player`
- prepare one opponent name, for example `Demo University`

Suggested browser tabs:

- home page
- players page
- statistics page
- matchup page
- login page
- add player page
- add game record page
- schema page

## 0:00-0:40 Project Introduction

Say:

This is our BJUT Baseball Team Management and Match Statistics Database System. The final Phase 2 version is a Java Web application using Java HttpServer, JDBC, and MySQL. Its purpose is to store and manage baseball player information, game records, batting statistics, pitching statistics, and matchup information against different opponents.

The system has two user roles. General users can view players and statistics. Administrators can log in and modify data, including adding players and game records.

Show:

- home page
- navigation bar
- dashboard summary

## 0:40-1:30 Architecture and Database Design

Say:

The system uses a layered architecture. Users interact with web pages in the presentation layer. The Java backend handles routing, authentication, form processing, DAO calls, and statistics calculation. JDBC connects the Java application to the MySQL relational database.

In Phase 2, we improved the database design based on the Phase 1 feedback. We created a formal architecture diagram, a clear ER diagram, and complete MySQL `CREATE TABLE` statements.

Show:

- `docs/phase2_diagrams.md`
- architecture diagram
- ER diagram
- `sql/phase2_mysql_schema.sql`
- `java-web/README.md`

Mention:

- `player_positions` handles the many-to-many relationship between players and positions.
- `fielder_profiles` and `pitcher_profiles` now contain real summary statistics, not only `player_id`.
- the MySQL schema includes primary keys, foreign keys, `NOT NULL`, `UNIQUE`, `CHECK`, and referential actions.

## 1:30-2:30 General User: Player List

Say:

General users can view all players and their basic information. The demonstration database contains 27 players migrated from the earlier project prototype. The system stores player name, jersey number, primary position, player type, and multiple defensive positions.

Show:

- players page
- player table/cards
- search or browse if available
- one player detail/edit modal only if visible without login

Mention:

Player-position data is stored relationally instead of as a repeated text list.

## 2:30-3:30 General User: Statistics

Say:

The statistics page shows batting and pitching performance from the MySQL profile tables. The system stores batting average, on-base percentage, slugging percentage, OPS, ERA, WHIP, and strike percentage.

Show:

- statistics page
- batting leaderboard
- pitching leaderboard
- charts if visible

Mention:

These results are based on 230 historical game records and profile summary tables.

## 3:30-4:20 General User: Matchup Search

Say:

The matchup page allows users to analyze historical records by player, opponent, or player-opponent combination. This helps coaches and players understand performance against different universities.

Show:

- matchup page
- opponent dropdown/list
- perform one search
- batting and pitching result sections

Mention:

The current database contains historical game data from multiple opponents.

## 4:20-5:00 Administrator Login

Say:

Write operations are protected. A general user cannot add, edit, or delete data. The administrator must log in first.

Show:

- login page
- log in using `admin / admin123`
- administrator indicator in the navigation bar

Mention:

Administrator accounts are stored in the `users` table with hashed passwords.

## 5:00-6:00 Administrator: Add/Edit Player

Say:

After logging in, the administrator can add new players and assign positions. A player can have more than one position, which is stored through the `player_positions` relationship table.

Show:

- add player page
- create demo player
- return to player list
- edit the player if time allows

Suggested demo data:

- name: `Demo Player`
- jersey number: `88`
- positions: `捕手`, `一垒手`
- primary position: `捕手`

## 6:00-6:50 Administrator: Add Game Record

Say:

Administrators can add game records for batting or pitching performances. These records are the detailed source data used to update statistics.

Show:

- add game record page
- select the demo player or an existing player
- add one batting record

Suggested demo data:

- date: current date
- opponent: `Demo University`
- at-bats: 4
- hits: 2
- runs: 1
- RBI: 1
- walks: 0
- strikeouts: 1
- doubles: 1

## 6:50-7:25 Verification and Data Consistency

Say:

After inserting records, the system updates or displays statistics from MySQL. Delete operations are also protected and can only be performed by an administrator.

Show:

- player list or game statistics page
- updated player or game data
- delete demo data if needed

Mention:

The runtime checks verified page access, APIs, login, player CRUD, and game record creation.

## 7:25-8:00 Limitations and Conclusion

Say:

The main limitation is that this lightweight Java Web version focuses on the core course functions, so advanced PDF import and richer visual charts from the earlier Flask prototype are not fully replicated here. However, the final version satisfies the key Phase 2 technology requirement by using Java, a browser-based Web UI, JDBC, and MySQL.

In conclusion, the system provides a practical Java Web information system for managing BJUT baseball players and match statistics, with administrator access control, relational database design, and statistics analysis.

## Recording Checklist

- show the home page
- show architecture and ER diagrams
- show MySQL schema
- show Java Web project README or structure
- show player list
- show statistics page
- show matchup page
- log in as administrator
- add or edit a player
- add a game record
- mention limitations
- keep the final video under 8 minutes
