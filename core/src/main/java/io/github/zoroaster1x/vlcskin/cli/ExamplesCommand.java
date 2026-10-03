package io.github.zoroaster1x.vlcskin.cli;

import io.github.zoroaster1x.vlcskin.example.ExampleSkins;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;

/**
 * List the built in example themes.
 */
@Command(name = "examples", description = "List the built in example themes.")
public final class ExamplesCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        for (ExampleSkins.Example example : ExampleSkins.catalog()) {
            System.out.printf("%-10s %-14s %s%n", example.id(), example.name(), example.description());
        }
        return 0;
    }
}
