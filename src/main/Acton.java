package main;

import main.ast.node.Program;
import main.visitor.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import main.parsers.actonLexer; // import from the package your parser & lexer is generated in
import main.parsers.actonParser;

import java.io.IOException;

// Visit https://stackoverflow.com/questions/26451636/how-do-i-use-antlr-generated-parser-and-lexer
public class Acton {
    public static void main(String[] args) throws IOException {
        CharStream reader = CharStreams.fromFileName(args[1]);
        actonLexer lexer = new actonLexer(reader);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        actonParser parser = new actonParser(tokens);
        Program program = parser.program().p; /* assuming that the name of the Program ast node
                                                 that the program rule returns is p */
        Visitor dataCollector = new dataCollectorImpl();
        dataCollector.visit(program);
//        System.out.println("split here **************");
        Visitor compilerError = new compilerErrorsImpl();
        compilerError.visit(program);
        if (!Errors.hasErrors()) {
            Visitor visitor = new VisitorImpl();
            visitor.visit(program);
        }
        else {
            Errors.sortByValue();
            Errors.getErrorMap().entrySet().forEach(entry->{
                System.out.println(entry.getKey());
            });
        }
    }
}