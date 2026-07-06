package com.example.duizhan.game.story;

public class BattlePrologue {
    public final String eraTitle;
    public final String matchupLine;
    public final String blueLine;
    public final String redLine;
    public final String themeLine;
    public final String closingLine;

    public BattlePrologue(String eraTitle, String matchupLine, String blueLine, String redLine,
                          String themeLine, String closingLine) {
        this.eraTitle = eraTitle;
        this.matchupLine = matchupLine;
        this.blueLine = blueLine;
        this.redLine = redLine;
        this.themeLine = themeLine;
        this.closingLine = closingLine;
    }

    public String fullNarration() {
        return eraTitle + "。"
                + matchupLine + "。"
                + blueLine + "。"
                + redLine + "。"
                + themeLine + "。"
                + closingLine;
    }
}
