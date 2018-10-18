package net.jemand771.domevlcextradip;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DisplayParser {

    private ResultSet resultSet;
    private String noInfoString;
    private String parsingError = "ERROR PARSING CONFIG";
    private boolean flagNoInfo = false;

    public DisplayParser(ResultSet resultSet, String noInfoString) {

        this.resultSet = resultSet;
        this.noInfoString = noInfoString;
    }

    public String parse(String config) throws SQLException {

        String output = "";

        String[] segments = config.split("\\{");
        for (int i = 0; i < segments.length; i++) {

            String segment = segments[i];
            String[] commandAndLiteralString = segment.split("}");
            // this does not work anyway
//            if (segment.indexOf(segment.length() - 1) != '{' && i != 0) // ends with }
//                if (commandAndLiteralString.length != 2) return parsingError;

            String plaintext = "";
            String command = "";
            if (i != 0) {
                command = commandAndLiteralString[0];
                if (i != segments.length - 1)
                    plaintext = commandAndLiteralString[1];
            } else {
                plaintext = commandAndLiteralString[0];
            }
            output += parseCommand(command);
            output += plaintext;
        }

        if (flagNoInfo) return noInfoString;
        return output;
    }

    private String parseCmdRequire(String args) throws SQLException {

        int column = Integer.valueOf(args);
        String data = resultSet.getString(column);
        if (data == null) flagNoInfo = true;
        return "";
    }

    private String parseCmdColumn(String args) throws SQLException {

        int column = Integer.valueOf(args);
        String data = resultSet.getString(column);
        if (data == null) return "";
        return data;
    }

    private String parseCmdIfset(String args) throws SQLException {

        String cString = args.split("-")[0];
        int column = Integer.valueOf(cString);
        String text = args.substring(cString.length() + 1);
        String data = resultSet.getString(column);
        if (data == null) return "";
        return text;
    }

    private String parseCmdIfNotset(String args) throws SQLException {

        String cString = args.split("-")[0];
        int column = Integer.valueOf(cString);
        String text = args.substring(cString.length() + 1);
        String data = resultSet.getString(column);
        if (data != null) return "";
        return text;
    }

    private String parseIfEqual(String args) throws SQLException {

        String cString = args.split("-")[0];
        int column = Integer.valueOf(cString);
        String allText = args.substring(cString.length() + 1);
        String[] textSplit = allText.split("-");
        String search = textSplit[0];
        String text = allText.substring(search.length() + 1);
        String data = resultSet.getString(column);
        if (data == null) data = "";
        if (data.equals(search)) return text;
        return "";
    }

    private String parseIfNotEqual(String args) throws SQLException {

        String cString = args.split("-")[0];
        int column = Integer.valueOf(cString);
        String allText = args.substring(cString.length() + 1);
        String[] textSplit = allText.split("-");
        String search = textSplit[0];
        String text = allText.substring(search.length() + 1);
        String data = resultSet.getString(column);
        if (data == null) data = "";
        if (!data.equals(search)) return text;
        return "";
    }

    private String parseIfEqualI(String args) throws SQLException {

        String cString = args.split("-")[0];
        int column = Integer.valueOf(cString);
        String allText = args.substring(cString.length() + 1);
        String[] textSplit = allText.split("-");
        String search = textSplit[0];
        String text = allText.substring(search.length() + 1);
        String data = resultSet.getString(column);
        if (data == null) data = "";
        if (data.equalsIgnoreCase(search)) return text;
        return "";
    }

    private String parseIfNotEqualI(String args) throws SQLException {

        String cString = args.split("-")[0];
        int column = Integer.valueOf(cString);
        String allText = args.substring(cString.length() + 1);
        String[] textSplit = allText.split("-");
        String search = textSplit[0];
        String text = allText.substring(search.length() + 1);
        String data = resultSet.getString(column);
        if (data == null) data = "";
        if (!data.equalsIgnoreCase(search)) return text;
        return "";
    }

    private String parseCommand(String cmd) throws SQLException {

        if (cmd.equals("")) return "";

        String actualCmd = cmd.split("-")[0].toLowerCase();
        String rest = cmd.substring(actualCmd.length() + 1);
        switch (actualCmd) {
            case "require":
                return parseCmdRequire(rest);
            case "column":
                return parseCmdColumn(rest);
            case "ifset":
                return parseCmdIfset(rest);
            case "ifnotset":
                return parseCmdIfNotset(rest);
            case "ifequal":
                return parseIfEqual(rest);
            case "ifnotequal":
                return parseIfNotEqual(rest);
            case "ifequali":
                return parseIfEqualI(rest);
            case "ifnotequali":
                return parseIfNotEqualI(rest);
            default:
                return "";
        }
    }
}