package com.github.maxpesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    static boolean hadError;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: fccalc <file>");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
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

    private static void runFile(String path) throws IOException {
        var bytes = Files.readAllBytes(Path.of(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) {
            System.exit(65);
        }
    }

    private static void run(String src) {
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.tokenize();
        for (Token token : tokens) {
//            System.out.println(token);
        }
    }

    static void error(String source, int line, int character, String msg) {
        report(source, line, character, msg);
    }

    private static void report(String source, int lineNum, int character, String msg) {
        String line = source.split("\\n")[lineNum - 1];

        System.out.printf(inBold("fccalc.java:%d:%d:") + inRed(" error: ") + "%s\n", lineNum, character, msg);
        System.out.printf("%6d|\t\t%s" + inRed("%s") + "%s\n", lineNum,
                line.substring(0, character - 1),
                line.charAt(character - 1),
                line.substring(character));
        System.out.printf("%7s\t\t" + inRed("%" + character + "s") + "\n", "|", "^");
        hadError = true;
    }

    private static String inBold(String string) {
        return "\033[1m" + string + "\033[0m";
    }

    private static String inRed(String string) {
        return "\033[31m" + string + "\033[0m";
    }
}