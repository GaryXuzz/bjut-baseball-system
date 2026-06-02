# Phase 2 Runtime Check

## Local Environment

- Python: 3.9.6
- Virtual environment: `.venv`
- Dependency installation command:

```bash
.venv/bin/python -m pip install -i https://pypi.tuna.tsinghua.edu.cn/simple --timeout 120 -r requirements.txt
```

## Runtime Fix

The local Python environment does not provide `hashlib.scrypt`, while newer Werkzeug versions may use `scrypt` as the default password hashing method.

To keep administrator login stable across local machines, `User.set_password()` now explicitly uses:

```text
pbkdf2:sha256
```

This keeps password hashes secure enough for this course project and avoids runtime failure during default administrator creation.

## Database State

The application initializes the database on startup and creates the default administrator if it does not exist:

- username: `admin`
- password: `admin123`

Verified data after initialization:

- players: 27
- game records: 230
- positions: 9
- fielder profiles: 27
- pitcher profiles: 27
- users: 1

## Checked Pages and APIs

The following checks passed using Flask's local test client:

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
- unauthenticated `POST /api/players` is blocked
- administrator login works with `admin / admin123`
- authenticated `POST /api/players` works
- authenticated `PUT /api/players/<id>` works
- authenticated `POST /api/game_records` works
- authenticated `DELETE /api/players/<id>` works

## Java Web Final Version

To satisfy the Java + MySQL + Web requirement, the repository now includes a standalone Java Web implementation under `java-web/`.

The Java version uses:

- Java built-in `HttpServer`
- JDBC
- MySQL
- browser-based HTML pages

Compilation check:

```bash
javac -encoding UTF-8 -d java-web/bin $(find java-web/src -name '*.java')
```

This check passed locally with `javac 25.0.1`.

The Java Web version is the final Phase 2 demonstration target. The earlier Flask website remains a deployed prototype and reference implementation.

## Java Web Runtime Verification

Local MySQL was started successfully and the Java Web version was run against the `bjut_baseball` database.

Verified setup:

- MySQL Server: 9.6.0
- MySQL database: `bjut_baseball`
- MySQL Connector/J: `java-web/lib/mysql-connector-j.jar`
- Java Web URL: `http://127.0.0.1:8080/`

Verified imported data:

- players: 27
- game records: 230
- positions: 9
- fielder profiles: 27
- pitcher profiles: 27
- users: 1

Verified Java Web pages:

- `GET /`
- `GET /players`
- `GET /stats`
- `GET /matchups`
- `GET /schema`
- `GET /login`
- `GET /export/players.csv`
- `GET /export/game-records.csv`

Verified administrator operations:

- administrator login works with `admin / admin123`
- adding a player through the Java Web form inserts a row into MySQL
- deleting a player through the Java Web form removes the row from MySQL
- adding and deleting one game record through the Java Web routes updates MySQL
- matchup filtering by player works
- CSV export for player summaries and game records works
- statistics page includes batting and pitching bar-chart visualizations
- responsive layout check passed at a 390px mobile viewport

The original Flask/SQLite data was migrated into MySQL using `sql/original_project_data.sql`, so the Java Web version now demonstrates the real project player list and historical game records.
