package main.visitor;

import com.sun.org.apache.xpath.internal.operations.Bool;
import main.ast.node.Main;
import main.ast.node.Program;
import main.ast.node.declaration.ActorDeclaration;
import main.ast.node.declaration.ActorInstantiation;
import main.ast.node.declaration.VarDeclaration;
import main.ast.node.declaration.handler.HandlerDeclaration;
import main.ast.node.declaration.handler.MsgHandlerDeclaration;
import main.ast.node.expression.*;
import main.ast.node.expression.values.BooleanValue;
import main.ast.node.expression.values.IntValue;
import main.ast.node.expression.values.StringValue;
import main.ast.node.statement.*;
import main.ast.type.arrayType.ArrayType;
import main.symbolTable.*;
import main.symbolTable.itemException.ItemAlreadyExistsException;
import main.symbolTable.itemException.ItemNotFoundException;
import main.symbolTable.symbolTableVariableItem.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class compilerErrorsImpl implements Visitor {
    @Override
    public void visit(Program program) {
        if (program.getActors() != null) {
            HashMap<String, Boolean> visitedActors = new HashMap<String, Boolean>();
            for (ActorDeclaration actorDeclaration : program.getActors()) {
                actorDeclaration.accept(this);
                if (visitedActors.get(actorDeclaration.getName().getName()) != null)
                    continue;
                visitedActors.put(actorDeclaration.getName().getName(), true);
                SymbolTableActorItem current = new SymbolTableActorItem(actorDeclaration);
                SymbolTableActorItem parent = null;
                if (current.getParentName() != null) {
                    Boolean cycleFound = false;
                    HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
                    visited.put(current.getActorDeclaration().getName().getName(), true);
                    while (true) {
                        try {
                            if (current.getActorDeclaration().getParentName() == null)
                                break;
                            parent = (SymbolTableActorItem) (SymbolTable.root.get(SymbolTableActorItem.STARTKEY + current.getActorDeclaration().getParentName().getName()));
                        } catch (ItemNotFoundException e) {
                            break;
                        }
                        if (visited.get(parent.getActorDeclaration().getName().getName()) != null) {
                            if (visited.get(parent.getActorDeclaration().getName().getName()))
                                if (parent.getActorDeclaration().getName().getName() == actorDeclaration.getName().getName()) {
//                                    System.out.print("Line:");
//                                    System.out.print(actorDeclaration.getLine());
//                                    System.out.print(":Cyclic inheritance involving actor ");
//                                    System.out.println(actorDeclaration.getName().getName());
                                    String errorName = "Line:" + actorDeclaration.getLine() + ":Cyclic inheritance involving actor " + actorDeclaration.getName().getName();
                                    errors.put(errorName, actorDeclaration.getLine());
                                    cycleFound = true;
                                }
                            break;
                        }
                        visited.put(parent.getActorDeclaration().getName().getName(), true);
                        current = parent;
                    }
                    if (cycleFound) {
                        current = new SymbolTableActorItem(actorDeclaration);
                        while (true) {
                            try {
                                parent = (SymbolTableActorItem) (SymbolTable.root.get(SymbolTableActorItem.STARTKEY + current.getActorDeclaration().getParentName().getName()));
                            } catch (ItemNotFoundException e) {
                                break;
                            }
                            if (actorDeclaration.getName().getName() == parent.getActorDeclaration().getName().getName())
                                break;
                            visitedActors.put(parent.getActorDeclaration().getName().getName(), true);
                            current = parent;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void visit(ActorDeclaration actorDeclaration) {
        SymbolTableActorItem newSymbolActorItem = null;
        try {
            newSymbolActorItem = (SymbolTableActorItem)(SymbolTable.root.get(SymbolTableActorItem.STARTKEY + actorDeclaration.getName().getName()));
        }
        catch (ItemNotFoundException e){

        }
        SymbolTable symbolTable = new SymbolTable(SymbolTable.root, actorDeclaration.getName().getName());
        SymbolTable.push(symbolTable);
        newSymbolActorItem.setActorSymbolTable(symbolTable.top);
        if (actorDeclaration.getName() != null)
            actorDeclaration.getName().accept(this);

        if (actorDeclaration.getParentName() != null)
            actorDeclaration.getParentName().accept(this);

        if (actorDeclaration.getKnownActors() != null) {
            for (VarDeclaration varDeclaration : actorDeclaration.getKnownActors()) {
                varDeclaration.accept(this);
                SymbolTableKnownActorItem newKnownActor = new SymbolTableKnownActorItem(varDeclaration);
                Boolean error = false;
                try {
                    SymbolTable.top.put(newKnownActor);
                }
                catch (ItemAlreadyExistsException e) {
                    error = true;
//                    System.out.print("Line:");
//                    System.out.print(varDeclaration.getLine());
//                    System.out.print(":Redefinition of variable ");
//                    System.out.println(varDeclaration.getIdentifier().getName());
                    String errorName = "Line:" + varDeclaration.getLine() + ":Redefinition of variable " + varDeclaration.getIdentifier().getName();
                    errors.put(errorName, varDeclaration.getLine());
                }
                if (error == false) {
                    SymbolTableActorItem current = new SymbolTableActorItem(actorDeclaration);
                    SymbolTableActorItem parent = null;
                    if (current.getParentName() != null) {
                        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
                        visited.put(current.getActorDeclaration().getName().getName(), true);
                        while (true) {
//                            visited.entrySet().forEach(entry->{
//                                System.out.println(entry.getKey() + ": " + entry.getValue());
//                            });

                            try {
                                parent = (SymbolTableActorItem) (SymbolTable.root.get(SymbolTableActorItem.STARTKEY + current.getActorDeclaration().getParentName().getName()));
                            }
                            catch (ItemNotFoundException e) {
                                break;
                            }
                            if (visited.get(parent.getActorDeclaration().getName().getName()) != null) {
                                if (visited.get(parent.getActorDeclaration().getName().getName()))
                                    break;
                            }
                            visited.put(parent.getActorDeclaration().getName().getName(), true);
                            try {
                                SymbolTableItem tempVar = parent.getActorSymbolTable().get(SymbolTableVariableItem.STARTKEY + varDeclaration.getIdentifier().getName());
                            }
                            catch (ItemNotFoundException e) {

                                current = parent;
                                if (current.getParentName() != null) {
                                    continue;
                                }
                                break;
                            }
//                            System.out.print("Line:");
//                            System.out.print(varDeclaration.getLine());
//                            System.out.print(":Redefinition of variable ");
//                            System.out.println(varDeclaration.getIdentifier().getName());
                            String errorName = "Line:" + varDeclaration.getLine() + ":Redefinition of variable " + varDeclaration.getIdentifier().getName();
                            errors.put(errorName, varDeclaration.getLine());
                            break;
                        }
                    }
                }
            }
        }

        if (actorDeclaration.getActorVars() != null) {
            for (VarDeclaration varDeclaration : actorDeclaration.getActorVars()) {
                varDeclaration.accept(this);
                SymbolTableActorVariableItem newActorVariable = new SymbolTableActorVariableItem(varDeclaration);
                Boolean error = false;
                try {
                    SymbolTable.top.put(newActorVariable);
                }
                catch (ItemAlreadyExistsException e) {
                    error = true;
//                    System.out.print("Line:");
//                    System.out.print(varDeclaration.getLine());
//                    System.out.print(":Redefinition of variable ");
//                    System.out.println(varDeclaration.getIdentifier().getName());
                    String errorName = "Line:" + varDeclaration.getLine() + ":Redefinition of variable " + varDeclaration.getIdentifier().getName();
                    errors.put(errorName, varDeclaration.getLine());
                }
                if (error == false) {
                    SymbolTableActorItem current = new SymbolTableActorItem(actorDeclaration);
                    SymbolTableActorItem parent = null;
                    if (current.getParentName() != null) {
                        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
                        visited.put(current.getActorDeclaration().getName().getName(), true);
                        while (true) {
//                            visited.entrySet().forEach(entry->{
//                                System.out.println(entry.getKey() + ": " + entry.getValue());
//                            });

                            try {
                                parent = (SymbolTableActorItem) (SymbolTable.root.get(SymbolTableActorItem.STARTKEY + current.getActorDeclaration().getParentName().getName()));
                            }
                            catch (ItemNotFoundException e) {
                                break;
                            }
                            if (visited.get(parent.getActorDeclaration().getName().getName()) != null) {
                                if (visited.get(parent.getActorDeclaration().getName().getName()))
                                    break;
                            }
                            visited.put(parent.getActorDeclaration().getName().getName(), true);
                            try {
                                SymbolTableItem tempVar = parent.getActorSymbolTable().get(SymbolTableVariableItem.STARTKEY + varDeclaration.getIdentifier().getName());
                            }
                            catch (ItemNotFoundException e) {

                                current = parent;
                                if (current.getParentName() != null) {
                                    continue;
                                }
                                break;
                            }
//                            System.out.print("Line:");
//                            System.out.print(varDeclaration.getLine());
//                            System.out.print(":Redefinition of variable ");
//                            System.out.println(varDeclaration.getIdentifier().getName());
                            String errorName = "Line:" + varDeclaration.getLine() + ":Redefinition of variable " + varDeclaration.getIdentifier().getName();
                            errors.put(errorName, varDeclaration.getLine());
                            break;
                        }
                    }
                }
            }
        }

        if (actorDeclaration.getInitHandler() != null) {
            actorDeclaration.getInitHandler().accept(this);
        }

        if (actorDeclaration.getMsgHandlers() != null) {
            for (MsgHandlerDeclaration msgHandlerDeclaration : actorDeclaration.getMsgHandlers()) {
                msgHandlerDeclaration.accept(this);
                SymbolTableHandlerItem newHandlerItem = new SymbolTableHandlerItem(msgHandlerDeclaration);
                Boolean error = false;
                try {
                    SymbolTable.top.put(newHandlerItem);
                }
                catch (ItemAlreadyExistsException e) {
                    error = true;
//                    System.out.print("Line:");
//                    System.out.print(msgHandlerDeclaration.getLine());
//                    System.out.print(":Redefinition of msghandler ");
//                    System.out.println(msgHandlerDeclaration.getName().getName());
                    String errorName = "Line:" + msgHandlerDeclaration.getLine() + ":Redefinition of msghandler " + msgHandlerDeclaration.getName().getName();
                    errors.put(errorName, msgHandlerDeclaration.getLine());
                }
                if (error == false) {
                    SymbolTableActorItem current = new SymbolTableActorItem(actorDeclaration);
                    SymbolTableActorItem parent = null;
                    if (current.getParentName() != null) {
                        HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
                        visited.put(current.getActorDeclaration().getName().getName(), true);
                        while (true) {
//                            visited.entrySet().forEach(entry->{
//                                System.out.println(entry.getKey() + ": " + entry.getValue());
//                            });

                            try {
                                parent = (SymbolTableActorItem) (SymbolTable.root.get(SymbolTableActorItem.STARTKEY + current.getActorDeclaration().getParentName().getName()));
                            }
                            catch (ItemNotFoundException e) {
                                break;
                            }
                            if (visited.get(parent.getActorDeclaration().getName().getName()) != null) {
                                if (visited.get(parent.getActorDeclaration().getName().getName()))
                                    break;
                            }
                            visited.put(parent.getActorDeclaration().getName().getName(), true);
                            try {
                                SymbolTableItem tempVar = parent.getActorSymbolTable().get(SymbolTableHandlerItem.STARTKEY + msgHandlerDeclaration.getName().getName());
                            }
                            catch (ItemNotFoundException e) {

                                current = parent;
                                if (current.getParentName() != null) {
                                    continue;
                                }
                                break;
                            }
//                            System.out.print("Line:");
//                            System.out.print(msgHandlerDeclaration.getLine());
//                            System.out.print(":Redefinition of msghandler ");
//                            System.out.println(msgHandlerDeclaration.getName().getName());
                            String errorName = "Line:" + msgHandlerDeclaration.getLine() + ":Redefinition of msghandler " + msgHandlerDeclaration.getName().getName();
                            errors.put(errorName, msgHandlerDeclaration.getLine());
                            break;
                        }
                    }
                }

            }
        }
        symbolTable.pop();
    }

    @Override
    public void visit(HandlerDeclaration handlerDeclaration) {
        if (handlerDeclaration.getName() != null)
            handlerDeclaration.getName().accept(this);

        if (handlerDeclaration.getArgs() != null) {
            for (VarDeclaration varDeclaration : handlerDeclaration.getArgs()) {
                varDeclaration.accept(this);
            }
        }

        if (handlerDeclaration.getLocalVars() != null) {
            for (VarDeclaration varDeclaration : handlerDeclaration.getLocalVars()) {
                varDeclaration.accept(this);
            }
        }

        if (handlerDeclaration.getBody() != null) {
            for (Statement statement : handlerDeclaration.getBody()) {
                statement.accept(this);
            }
        }
    }

    @Override
    public void visit(VarDeclaration varDeclaration) {
//        if (varDeclaration.getType() instanceof ArrayType)
//            if (((ArrayType) varDeclaration.getType()).getSize() == 0) {
//                System.out.print("Line:");
//                System.out.print(varDeclaration.getLine());
//                System.out.println(":Array size must be positive");
//            }

        if (varDeclaration.getIdentifier() != null)
            varDeclaration.getIdentifier().accept(this);
    }

    @Override
    public void visit(Main mainActors) {
//        SymbolTable newSymbolTable = new SymbolTable(SymbolTable.top, mainActors.toString());
//        SymbolTableMainItem newSymbolMainItem = new SymbolTableMainItem(mainActors);

//        try {
//            SymbolTable.top.put(newSymbolMainItem);
//        }
//        catch (ItemAlreadyExistsException e) {
//            System.out.println("Error in main");
//        }
//
//        SymbolTable.push(newSymbolTable);

        if (mainActors.getMainActors() != null) {
            for (ActorInstantiation actorInstantiation : mainActors.getMainActors()) {
                actorInstantiation.accept(this);
            }
        }
//        newSymbolMainItem.setMainSymbolTable(SymbolTable.top);
//        SymbolTable.pop();
    }

    @Override
    public void visit(ActorInstantiation actorInstantiation) {

        if (actorInstantiation.getIdentifier() != null)
            actorInstantiation.getIdentifier().accept(this);
        if (actorInstantiation.getKnownActors() != null) {
            for (Identifier identifier : actorInstantiation.getKnownActors()) {
                identifier.accept(this);
            }
        }
        if (actorInstantiation.getInitArgs() != null) {
            for (Expression expression : actorInstantiation.getInitArgs()) {
                expression.accept(this);
            }
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {

//        if (unaryExpression.getUnaryOperator() != null)

        if (unaryExpression.getOperand() != null)
            unaryExpression.getOperand().accept(this);
    }

    @Override
    public void visit(BinaryExpression binaryExpression) {

        if(binaryExpression.getLeft() != null)
            binaryExpression.getLeft().accept(this);
//        if(binaryExpression.getBinaryOperator() != null)

        if(binaryExpression.getRight() != null)
            binaryExpression.getRight().accept(this);
    }

    @Override
    public void visit(ArrayCall arrayCall) {

        if (arrayCall.getArrayInstance() != null)
            arrayCall.getArrayInstance().accept(this);
        if (arrayCall.getIndex() != null)
            arrayCall.getIndex().accept(this);
    }

    @Override
    public void visit(ActorVarAccess actorVarAccess) {


        if (actorVarAccess.getSelf() != null)
            actorVarAccess.getSelf().accept(this);

        if (actorVarAccess.getVariable() != null)
            actorVarAccess.getVariable().accept(this);
    }

    @Override
    public void visit(Identifier identifier) {

    }

    @Override
    public void visit(Self self) {

    }

    @Override
    public void visit(Sender sender) {

    }

    @Override
    public void visit(BooleanValue value) {

    }

    @Override
    public void visit(IntValue value) {

    }

    @Override
    public void visit(StringValue value) {

    }

    @Override
    public void visit(Block block) {

        if (block.getStatements() != null) {
            for (Statement statement : block.getStatements()) {
                statement.accept(this);
            }
        }

    }

    @Override
    public void visit(Conditional conditional) {

        if (conditional.getExpression() != null)
            conditional.getExpression().accept(this);

        if (conditional.getThenBody() != null)
            conditional.getThenBody().accept(this);

        if (conditional.getElseBody() != null)
            conditional.getElseBody().accept(this);
    }

    @Override
    public void visit(For loop) {

        if (loop.getInitialize() != null)
            loop.getInitialize().accept(this);

        if (loop.getCondition() != null)
            loop.getCondition().accept(this);

        if (loop.getUpdate() != null)
            loop.getUpdate().accept(this);

        if (loop.getBody() != null)
            loop.getBody().accept(this);
    }

    @Override
    public void visit(Break breakLoop) {

    }

    @Override
    public void visit(Continue continueLoop) {

    }

    @Override
    public void visit(MsgHandlerCall msgHandlerCall) {

        if (msgHandlerCall.getInstance() != null)
            msgHandlerCall.getInstance().accept(this);

        if (msgHandlerCall.getMsgHandlerName() != null)
            msgHandlerCall.getMsgHandlerName().accept(this);

        if (msgHandlerCall.getArgs() != null) {
            for (Expression expression : msgHandlerCall.getArgs()) {
                expression.accept(this);
            }
        }
    }

    @Override
    public void visit(Print print) {

        if (print.getArg() != null)
            print.getArg().accept(this);
    }

    @Override
    public void visit(Assign assign) {

        if (assign.getlValue() != null)
            assign.getlValue().accept(this);

        if (assign.getrValue() != null)
            assign.getrValue().accept(this);
    }
}
