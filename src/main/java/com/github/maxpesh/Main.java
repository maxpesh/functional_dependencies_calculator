package com.github.maxpesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Main {
    static boolean hadError;
    static String source;

    public static void main(String[] args) throws IOException {
        runPrompt();
    }

    private static void runPrompt() throws IOException {
        var input = new BufferedReader(new InputStreamReader(System.in));
        for (; ; ) {
            System.out.print("> ");
            var line = input.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    private static void run(String src) {
        Main.source = src;
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.tokenize();
        if (hadError) {
            return;
        }
        Parser parser = new Parser(tokens);
        parser.parse();
        if (hadError) {
            return;
        }
    }

    static void error(int line, int character, String msg) {
        reportSyntaxError(source, line, character, msg);
    }

    static void error(Token token, int charInd, String msg) {
        reportSemanticError(source, token, charInd, msg);
    }

    private static void reportSyntaxError(String source, int lineNum, int character, String msg) {
        String line = source.split("\\n")[lineNum - 1];

        System.out.printf(inBold("fccalc.java:%d:%d:") + inRed(" error: ") + "%s\n", lineNum, character, msg);
        System.out.printf("%6d|\t\t%s" + inRed("%s") + "%s\n", lineNum,
                line.substring(0, character),
                line.charAt(character),
                line.substring(character + 1));
        System.out.printf("%7s\t\t" + inRed("%" + (character + 1) + "s") + "\n", "|", "^");
        hadError = true;
    }

    private static void reportSemanticError(String source, Token token, int charInd, String msg) {
        String line = source.split("\\n")[token.line() - 1];
        int tokenStart = token.startIndex();
        int tokenEnd = tokenStart + token.lexeme().length();

        System.out.printf(inBold("fccalc.java:%d:%d:") + inRed(" error: ") + "%s\n", token.line(), charInd, msg);
        System.out.printf("%6d|\t\t%s" + inRed("%s") + "%s\n", token.line(),
                line.substring(0, tokenStart),
                line.substring(tokenStart, tokenEnd),
                line.substring(tokenEnd));
        System.out.printf("%7s\t\t" + inRed("%" + tokenStart + "s" + "%s" + "%s") + "\n",
                "|", "~".repeat(charInd - tokenStart), "^", "~".repeat(tokenEnd - charInd - 1));
        hadError = true;
    }


    private static String inBold(String string) {
        return "\033[1m" + string + "\033[0m";
    }

    private static String inRed(String string) {
        return "\033[31m" + string + "\033[0m";
    }
}