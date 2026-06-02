# COMP2004J Information System Project Phase 2 Report Draft

## Project Title

BJUT Baseball Team Management and Match Statistics Database System

## Group Members

- 23219724 Ou Yangjian
- 23219744 Xu Zixuan
- 23219730 Zhou Weiqi
- 23219727 Zhang Shichen

## 1. System Overview

The project is a Baseball Team Management and Match Statistics Database System for the BJUT baseball team. The system stores and manages player information, defensive positions, match records, batting statistics, pitching statistics, opponent matchup information, and administrator accounts.

The system is intended for two main user groups:

- General users, including players, coaches, and viewers, who can browse player lists, game statistics, rankings, and matchup records.
- Administrators, who can log in and perform write operations such as adding, editing, and deleting players, entering game records, and importing match data from PDF files.

The final Phase 2 implementation provides a Java Web interface backed by a MySQL database. It uses Java's built-in HTTP server, JDBC, and the MySQL schema prepared for this project. The earlier Flask website remains a deployed prototype and UI reference, while the Java Web version is the implementation used to satisfy the Java + MySQL requirement.

## 2. Improvements Based on Phase 1 Feedback

The Phase 1 feedback identified four main areas for improvement. Phase 2 addresses them as follows.

### 2.1 Formal System Architecture Diagram

The Phase 1 architecture diagram was understandable but informal. In Phase 2, the architecture is represented as a formal block diagram showing the interaction between:

- users
- web pages
- backend route controllers
- API services
- application services
- relational database tables

The diagram is included in `docs/phase2_diagrams.md`.

Report image asset: `docs/assets/system_architecture.svg`

### 2.2 Clear Electronic ER Diagram

The handwritten Phase 1 ER diagram has been replaced with a Mermaid ER diagram. It clearly shows entities, attributes, primary keys, foreign keys, and cardinalities.

The final ER diagram includes:

- `PLAYERS`
- `POSITIONS`
- `PLAYER_POSITIONS`
- `GAME_RECORDS`
- `FIELDER_PROFILES`
- `PITCHER_PROFILES`
- `USERS`

Report image asset: `docs/assets/er_diagram.svg`

### 2.3 Improved Profile Table Design

In Phase 1, `PITCHER_PROFILE` and `FIELDER_PROFILE` only contained `player_id`, so their purpose was unclear.

In Phase 2, these tables are meaningful summary tables:

- `fielder_profiles` stores batting and fielding totals such as at-bats, hits, RBIs, walks, strikeouts, extra-base hits, stolen bases, batting average, on-base percentage, slugging percentage, and OPS.
- `pitcher_profiles` stores pitching totals such as innings pitched, hits allowed, earned runs, walks allowed, strikeouts, pitches, strikes, ERA, WHIP, and strike percentage.

Detailed per-game data is stored in `game_records`, while profile tables support efficient statistics and leaderboard pages.

### 2.4 Complete MySQL Schema

The Phase 1 relational model was closer to a schema draft than complete SQL. Phase 2 provides a formal MySQL schema in `sql/phase2_mysql_schema.sql`.

The schema includes:

- data types
- `PRIMARY KEY`
- `FOREIGN KEY`
- `NOT NULL`
- `UNIQUE`
- `CHECK`
- indexes
- `ON UPDATE` and `ON DELETE` rules

## 3. System Architecture

The system uses a layered architecture.

Insert Figure 1 here: `docs/assets/system_architecture.svg`

### Presentation Layer

Users access the system through web pages:

- home dashboard
- players page
- statistics page
- game statistics page
- matchup page
- login page
- add player page
- add game record page
- PDF import and viewer pages

### Application Layer

The backend application handles:

- request routing
- authentication and administrator authorization
- player management
- game record handling
- statistics calculation
- matchup search
- JDBC database access
- form processing and redirects

### Java Web Services

The Java Web system exposes browser pages and form endpoints for:

- player CRUD
- batting and pitching statistics
- leaderboards
- game record creation
- matchup search
- schema explanation

### Database Layer

The relational database stores:

- player profiles
- defensive positions
- player-position relationships
- game records
- batting and fielding summary profiles
- pitching summary profiles
- administrator users

In the Java version, the data flow is:

```text
Browser -> Java HttpServer routes -> DAO classes -> JDBC -> MySQL
```

## 4. Final Database Design

The final database contains seven main tables.

Insert Figure 2 here: `docs/assets/er_diagram.svg`

| Table | Purpose |
| --- | --- |
| `players` | Stores player identity and basic information. |
| `positions` | Stores the defensive position dictionary. |
| `player_positions` | Resolves the many-to-many relationship between players and positions. |
| `game_records` | Stores per-player performance in each game. |
| `fielder_profiles` | Stores batting and fielding summary statistics. |
| `pitcher_profiles` | Stores pitching summary statistics. |
| `users` | Stores administrator login accounts. |

The full MySQL schema is provided in `sql/phase2_mysql_schema.sql`.

## 5. Normalization

The design follows normalization principles:

- Player data is stored once in `players`.
- Position names are stored once in `positions`.
- The many-to-many relationship between players and positions is represented by `player_positions`, avoiding repeated position columns or comma-separated lists.
- Game-level transaction data is stored in `game_records`.
- Administrator account data is stored separately in `users`.

The profile tables are controlled summary tables derived from `game_records`. They are used for efficient rankings and aggregate statistics. The application includes synchronization logic to keep summary statistics consistent with the detailed records.

## 6. Implemented Functional Requirements

### 6.1 General User Functions

General users can:

- view the home dashboard
- view all players
- view player batting and pitching statistics
- view batting and pitching leaderboards
- search matchup data by player and opponent
- view the MySQL schema and table constraints used by the system

### 6.2 Administrator Functions

Administrators can:

- log in with an administrator account
- add players
- edit player information
- delete players
- add game records

Write operations are protected by administrator login checks.

### 6.3 Statistics Functions

The system calculates and displays:

- batting average
- on-base percentage
- slugging percentage
- OPS
- ERA
- WHIP
- strike percentage
- batting leaderboards
- pitching leaderboards
- player-versus-opponent matchup summaries

## 7. User Interface

The final Java system provides a browser-based user interface using a dark sports-style theme. The main pages include:

- home dashboard
- player list
- add player form
- add game record form
- statistics page
- matchup statistics page
- administrator login page
- schema page

Screenshots should be inserted here in the final PDF report:

- Java Web home page screenshot
- Java Web players page screenshot
- Java Web statistics page screenshot
- Java Web matchup page screenshot
- Java Web administrator login screenshot
- Java Web add player or add game record screenshot

## 8. Runtime Verification

Runtime checks were first performed on the existing Flask prototype to understand the implemented feature set. The Java Web version should be used as the final demonstration target after MySQL is configured.

Verified database state:

- players: 27
- game records: 230
- positions: 9
- fielder profiles: 27
- pitcher profiles: 27
- users: 1

Prototype checks included:

- `GET /`
- `GET /players`
- `GET /stats`
- `GET /game_stats`
- `GET /matchup_stats`
- `GET /login`
- `GET /api/players`
- `GET /api/stats/batting_leaderboard`
- `GET /api/stats/pitching_leaderboard`
- `GET /api/matchup/opponents`
- unauthenticated write operations are blocked
- administrator login works
- authenticated add/edit/delete player operations work
- authenticated add game record operation works

The detailed verification notes are recorded in `docs/phase2_runtime_check.md`.

For the final Java Web version, the completed verification checklist is:

- `javac` compiles all files under `java-web/src`.
- MySQL schema imports successfully from `sql/phase2_mysql_schema.sql`.
- original project data imports successfully from `sql/original_project_data.sql`.
- `java-web/run.sh` or `java-web/run.bat` starts the server.
- `http://localhost:8080/` opens the Java dashboard.
- administrator login works with `admin / admin123`.
- player list, add, and delete functions work through MySQL.
- statistics and matchup pages read 27 players and 230 game records from MySQL.

## 9. Limitations

The current system has several limitations:

- The Java Web version focuses on the core course functions and does not fully replicate the richer Flask prototype UI.
- The Java Web version requires MySQL Connector/J to be placed in `java-web/lib`.
- PDF parsing and advanced charts remain in the Flask prototype and are not part of the lightweight Java Web version.
- The system focuses on one team, BJUT, and treats other universities as opponents.
- Advanced security features such as password reset, role groups, audit logs, and CSRF protection are not fully implemented.
- Some summary statistics are stored in profile tables for performance, so synchronization logic is needed to keep them consistent with game records.

## 10. Team Member Contributions

| Student ID | Name | Contribution |
| --- | --- | --- |
| 23219724 | Ou Yangjian | Project idea, system description, implementation checking, report review. |
| 23219744 | Xu Zixuan | Assumptions, architecture, task breakdown, document integration. |
| 23219730 | Zhou Weiqi | ER diagram design, database relationship checking. |
| 23219727 | Zhang Shichen | Relational model, normalization, SQL schema, contribution table. |

## 11. Conclusion

The Phase 2 system implements a database-backed baseball team management and match statistics information system. It provides player management, game record management, statistics calculation, leaderboards, matchup search, administrator login, and PDF data import support.

Compared with Phase 1, the database design has been significantly improved through a formal architecture diagram, clear ER diagram, meaningful profile tables, complete MySQL schema, stronger constraints, and runtime verification.
