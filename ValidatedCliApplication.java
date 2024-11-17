package com.example.demo;

import java.util.Arrays;
import java.util.Locale;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@NullMarked
@SpringBootApplication
public class ValidatedCliApplication implements CommandLineRunner {
    @Autowired
    private CommandLine.IFactory factory;

    @Autowired
    private GreetingCommand command;

    public static void main(String[] args) {
        SpringApplication.run(ValidatedCliApplication.class, args);
    }

    @Override
    public void run(@Nullable String... args) {
        int exitCode = new CommandLine(command, factory).execute(args);
        System.exit(exitCode);
    }
}

@NullMarked
@Component
@Command(
    name = "greet",
    description = "挨拶アプリ（バリデーション付き）",
    mixinStandardHelpOptions = true
)
class GreetingCommand implements Runnable {
    // 方法1: 定義済みの値リストを使用
    @Nullable
    @Option(
        names = {"-l", "--language"},
        description = "挨拶の言語",
        defaultValue = "en",
        converter = LanguageConverter.class
    )
    private Language language;

    // 方法2: パターンマッチングを使用
    @Option(
        names = "--code",
        description = "言語コード (en, ja, fr のみ)",
        defaultValue = "en"
    )
    private @Nullable String languageCode;

    @Nullable
    @Parameters(
        index = "0",
        description = "名前",
        defaultValue = "World"
    )
    private String name;

    @Override
    public void run() {
        String greeting = switch (language) {
            case JAPANESE -> "こんにちは、" + name + "さん！";
            case FRENCH -> "Bonjour, " + name + "!";
            case ENGLISH -> "Hello, " + name + "!";
        };

        System.out.printf("Language: %s, Code: %s\n", language, languageCode);
        System.out.println(greeting);
    }
}

@NullMarked
enum Language {
    ENGLISH("en"),
    JAPANESE("ja"),
    FRENCH("fr");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Language fromCode(String code) {
        return Arrays.stream(values())
            .filter(lang -> lang.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Invalid language code: " + code + ". Must be one of: en, ja, fr"));
    }
}

@NullMarked
class LanguageConverter implements CommandLine.ITypeConverter<Language> {
    @Override
    public Language convert(@SuppressWarnings("null") String value) {
        return Language.fromCode(value.toLowerCase(Locale.ENGLISH));
    }
}
