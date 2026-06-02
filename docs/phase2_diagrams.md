# Phase 2 Diagrams

These Mermaid diagrams can be copied into the Phase 2 report and exported as images if required.

Exportable SVG versions are also provided:

- `docs/assets/system_architecture.svg`
- `docs/assets/er_diagram.svg`

## Formal System Architecture Diagram

```mermaid
flowchart TB
    subgraph Users["Users"]
        Visitor["General Users\nPlayers / Coaches / Viewers"]
        Admin["Administrator"]
    end

    subgraph UI["Presentation Layer: Web Browser"]
        Home["Home Dashboard"]
        PlayersPage["Players Page"]
        StatsPage["Statistics Page"]
        MatchupPage["Matchup Page"]
        AdminPages["Admin Pages\nLogin / Add Player / Add Game Record"]
    end

    subgraph App["Application Layer: Backend Application"]
        JavaRoutes["Java HttpServer Route Controllers"]
        Auth["Authentication and Authorization"]
        PlayerService["Player Management"]
        GameService["Game Record Handling"]
        StatsService["Statistics Calculation"]
        SchemaService["Schema and Report Support"]
    end

    subgraph API["API Services"]
        PlayerAPI["Player APIs"]
        GameAPI["Game Record APIs"]
        StatsAPI["Statistics APIs"]
        MatchupAPI["Matchup APIs"]
        SchemaAPI["Schema Page"]
    end

    subgraph DB["Database Layer: Relational Database"]
        Tables["Players / Positions / Player Positions\nGame Records / Fielder Profiles / Pitcher Profiles / Users"]
    end

    Visitor --> Home
    Visitor --> PlayersPage
    Visitor --> StatsPage
    Visitor --> MatchupPage
    Admin --> AdminPages
    AdminPages --> Auth

    Home --> JavaRoutes
    PlayersPage --> JavaRoutes
    StatsPage --> JavaRoutes
    MatchupPage --> JavaRoutes
    AdminPages --> JavaRoutes

    JavaRoutes --> PlayerAPI
    JavaRoutes --> GameAPI
    JavaRoutes --> StatsAPI
    JavaRoutes --> MatchupAPI
    JavaRoutes --> SchemaAPI

    PlayerAPI --> PlayerService
    GameAPI --> GameService
    StatsAPI --> StatsService
    MatchupAPI --> StatsService
    SchemaAPI --> SchemaService

    Auth --> Tables
    PlayerService --> Tables
    GameService --> Tables
    StatsService --> Tables
    SchemaService --> Tables
```

## ER Diagram

```mermaid
erDiagram
    PLAYERS {
        int id PK
        varchar name
        varchar jersey_number
        varchar primary_position
        boolean is_pitcher
        date birth_date
        date join_date
        varchar contact_phone
    }

    POSITIONS {
        int id PK
        varchar name UK
        varchar description
    }

    PLAYER_POSITIONS {
        int player_id PK,FK
        int position_id PK,FK
    }

    FIELDER_PROFILES {
        int player_id PK,FK
        int at_bats_total
        int runs_total
        int hits_total
        int rbi_total
        int walks_total
        int strikeouts_batting_total
        decimal batting_average
        decimal on_base_percentage
        decimal slugging_percentage
        decimal ops
        int doubles
        int triples
        int home_runs_batting
        int total_bases
        int stolen_bases
        int errors_fielding
    }

    PITCHER_PROFILES {
        int player_id PK,FK
        decimal innings_pitched_total
        int hits_allowed_total
        int runs_allowed_total
        int earned_runs_total
        int walks_allowed_total
        int strikeouts_total
        int home_runs_allowed_total
        int pitches
        int strikes
        decimal era
        decimal whip
        decimal strike_percentage
    }

    GAME_RECORDS {
        int id PK
        int player_id FK
        date game_date
        varchar opponent
        boolean is_pitching_record
        int at_bats
        int runs
        int hits
        int rbi
        int walks
        int strikeouts
        int innings_pitched
        int hits_allowed
        int earned_runs
        int strikeouts_pitched
        boolean win
        boolean loss
        boolean save
    }

    USERS {
        int id PK
        varchar username UK
        varchar password_hash
        boolean is_admin
        datetime created_at
    }

    PLAYERS ||--o{ GAME_RECORDS : has
    PLAYERS ||--|| FIELDER_PROFILES : has
    PLAYERS ||--o| PITCHER_PROFILES : may_have
    PLAYERS ||--o{ PLAYER_POSITIONS : assigned_to
    POSITIONS ||--o{ PLAYER_POSITIONS : includes
```

## Diagram Notes

- The architecture diagram is a formal block diagram that separates users, web pages, backend application logic, API services, and the relational database.
- The ER diagram replaces the unclear handwritten Phase 1 diagram with readable entities, attributes, keys, and cardinalities.
- `PLAYER_POSITIONS` resolves the many-to-many relationship between `PLAYERS` and `POSITIONS`.
- `FIELDER_PROFILES` and `PITCHER_PROFILES` are now justified by their detailed aggregate statistics.
- `GAME_RECORDS` is the transaction table used to store per-player performance in each game.
