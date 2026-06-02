package bjut.baseball;

import java.math.BigDecimal;
import java.time.LocalDate;

final class Player {
    int id;
    String name;
    String jerseyNumber;
    String primaryPosition;
    boolean pitcher;
    LocalDate joinDate;
}

final class BattingStat {
    String playerName;
    String jerseyNumber;
    int atBats;
    int hits;
    int runs;
    int rbi;
    BigDecimal battingAverage;
    BigDecimal ops;
}

final class PitchingStat {
    String playerName;
    String jerseyNumber;
    BigDecimal innings;
    int strikeouts;
    BigDecimal era;
    BigDecimal whip;
}

final class MatchupRow {
    int id;
    String playerName;
    LocalDate gameDate;
    String opponent;
    boolean pitchingRecord;
    int hits;
    int rbi;
    int strikeouts;
    BigDecimal innings;
    int strikeoutsPitched;
    int earnedRuns;
}
