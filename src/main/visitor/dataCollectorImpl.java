package main.visitor;

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
import main.symbolTable.SymbolTable;
import main.symbolTable.SymbolTableActorItem;
import main.symbolTable.SymbolTableHandlerItem;
import main.symbolTable.SymbolTableMainItem;
import main.symbolTable.itemException.ItemAlreadyExistsException;
import main.symbolTable.symbolTableVariableItem.SymbolTableActorVariableItem;
import main.symbolTable.symbolTableVariableItem.SymbolTableHandlerArgumentItem;
import main.symbolTable.symbolTableVariableItem.SymbolTableKnownActorItem;
import main.symbolTable.symbolTableVariableItem.SymbolTableLocalVariableItem;

public class dataCollectorImpl implements Visitor {


    @Override
    public void visit(Program program) {
        SymbolTable.root = new SymbolTable();
        SymbolTable.push(SymbolTable.root);

        if (program.getActors() != null) {
            for (ActorDeclaration actorDeclaration : program.getActors()) {
                actorDeclaration.accept(this);
            }
        }

        if (program.getMain() != null)
            program.getMain().accept(this);
    }

    @Override
    public void visit(ActorDeclaration actorDeclaration) {
        SymbolTable newSymbolTable = new SymbolTable(SymbolTable.top, actorDeclaration.getName().getName());
        SymbolTableActorItem newSymbolActorItem = new SymbolTableActorItem(actorDeclaration);
        try {
            SymbolTable.top.put(newSymbolActorItem);
        }
        catch (ItemAlreadyExistsException e) {
            String errorName = "Line:" + actorDeclaration.getLine() + ":Redefinition of actor " + actorDeclaration.getName().getName();
            errors.put(errorName, actorDeclaration.getLine());

            String name = actorDeclaration.getName().getName();
            while(true) {
                name = name + "*";
                actorDeclaration.getName().setName(name);
                SymbolTableActorItem newSTI = new SymbolTableActorItem(actorDeclaration);
                try {
                    SymbolTable.top.put(newSTI);
                }
                catch (ItemAlreadyExistsException e2) {
                    continue;
                }
                break;
            }
//            System.out.print("Line:");
//            System.out.print(actorDeclaration.getLine());
//            System.out.print(":Redefinition of actor ");
//            System.out.println(actorDeclaration.getName().getName());
        }
        if (actorDeclaration.getName() != null && actorDeclaration.getQueueSize() == 0) {
//            System.out.print("Line:");
//            System.out.print(actorDeclaration.getLine());
//            System.out.println(":Queue size must be positive");
            String errorName = "Line:" + actorDeclaration.getLine() + ":Queue size must be positive";
            errors.put(errorName, actorDeclaration.getLine());
        }

        SymbolTable.push(newSymbolTable);
        newSymbolActorItem.setActorSymbolTable(SymbolTable.top);

        if (actorDeclaration.getName() != null)
            actorDeclaration.getName().accept(this);

        if (actorDeclaration.getParentName() != null)
            actorDeclaration.getParentName().accept(this);

        if (actorDeclaration.getKnownActors() != null) {
            for (VarDeclaration varDeclaration : actorDeclaration.getKnownActors()) {
                SymbolTableKnownActorItem newKnownActor = new SymbolTableKnownActorItem(varDeclaration);
                try {
                    SymbolTable.top.put(newKnownActor);
                }
                catch (ItemAlreadyExistsException e) {
//                    System.out.print("Line:");
//                    System.out.print(varDeclaration.getLine());
//                    System.out.print(":Redefinition of variable ");
//                    System.out.println(varDeclaration.getIdentifier().getName());

                }
                varDeclaration.accept(this);
            }
        }

        if (actorDeclaration.getActorVars() != null) {
            for (VarDeclaration varDeclaration : actorDeclaration.getActorVars()) {
                SymbolTableActorVariableItem newActorVar = new SymbolTableActorVariableItem(varDeclaration);
                try {
                    SymbolTable.top.put(newActorVar);
                }
                catch (ItemAlreadyExistsException e) {
//                    System.out.print("Line:");
//                    System.out.print(varDeclaration.getLine());
//                    System.out.print(":Redefinition of variable ");
//                    System.out.println(varDeclaration.getIdentifier().getName());
                }

                varDeclaration.accept(this);
            }
        }

        if (actorDeclaration.getInitHandler() != null) {
            actorDeclaration.getInitHandler().accept(this);
        }

        if (actorDeclaration.getMsgHandlers() != null) {
            for (MsgHandlerDeclaration msgHandlerDeclaration : actorDeclaration.getMsgHandlers()) {
                msgHandlerDeclaration.accept(this);
            }
        }
        newSymbolActorItem.setActorSymbolTable(SymbolTable.top);
        SymbolTable.pop();
    }

    @Override
    public void visit(HandlerDeclaration handlerDeclaration) {
        SymbolTable newSymbolTable = new SymbolTable(SymbolTable.top, handlerDeclaration.getName().getName());
        SymbolTableHandlerItem newSymbolHandlerItem = new SymbolTableHandlerItem(handlerDeclaration);

        try {
            SymbolTable.top.put(newSymbolHandlerItem);
        }
        catch (ItemAlreadyExistsException e) {
//            System.out.print("Line:");
//            System.out.print(handlerDeclaration.getLine());
//            System.out.print(":Redefinition of msghandler ");
//            System.out.println(handlerDeclaration.getName().getName());
        }
        SymbolTable.push(newSymbolTable);
        newSymbolHandlerItem.setHandlerSymbolTable(SymbolTable.top);

        if (handlerDeclaration.getName() != null)
            handlerDeclaration.getName().accept(this);

        if (handlerDeclaration.getArgs() != null) {
            for (VarDeclaration varDeclaration : handlerDeclaration.getArgs()) {
                SymbolTableHandlerArgumentItem newHandlerArg = new SymbolTableHandlerArgumentItem(varDeclaration);
                try {
                    SymbolTable.top.put(newHandlerArg);
                }
                catch (ItemAlreadyExistsException e) {
//                    System.out.print("Line:");
//                    System.out.print(varDeclaration.getLine());
//                    System.out.print(":Redefinition of variable ");
//                    System.out.println(varDeclaration.getIdentifier().getName());
                    String errorName = "Line:" + varDeclaration.getLine() + ":Redefinition of variable " + varDeclaration.getIdentifier().getName();
                    errors.put(errorName, varDeclaration.getLine());

                }
                varDeclaration.accept(this);
            }
        }

        if (handlerDeclaration.getLocalVars() != null) {
            for (VarDeclaration varDeclaration : handlerDeclaration.getLocalVars()) {
                SymbolTableLocalVariableItem newLocalVar = new SymbolTableLocalVariableItem(varDeclaration);
                try {
                    SymbolTable.top.put(newLocalVar);
                }
                catch (ItemAlreadyExistsException e) {
//                    System.out.print("Line:");
//                    System.out.print(varDeclaration.getLine());
//                    System.out.print(":Redefinition of variable ");
//                    System.out.println(varDeclaration.getIdentifier().getName());
                    String errorName = "Line:" + varDeclaration.getLine() + ":Redefinition of variable " + varDeclaration.getIdentifier().getName();
                    errors.put(errorName, varDeclaration.getLine());
                }
                varDeclaration.accept(this);
            }
        }

        if (handlerDeclaration.getBody() != null) {
            for (Statement statement : handlerDeclaration.getBody()) {
                statement.accept(this);
            }
        }
        newSymbolHandlerItem.setHandlerSymbolTable(SymbolTable.top);
        SymbolTable.pop();
    }

    @Override
    public void visit(VarDeclaration varDeclaration) {
        if (varDeclaration.getType() instanceof ArrayType)
            if (((ArrayType) varDeclaration.getType()).getSize() == 0) {
//                System.out.print("Line:");
//                System.out.print(varDeclaration.getLine());
//                System.out.println(":Array size must be positive");
                String errorName = "Line:" + varDeclaration.getLine() + ":Array size must be positive";
                errors.put(errorName, varDeclaration.getLine());
            }

        if (varDeclaration.getIdentifier() != null)
            varDeclaration.getIdentifier().accept(this);
    }

    @Override
    public void visit(Main mainActors) {
        SymbolTable newSymbolTable = new SymbolTable(SymbolTable.top, mainActors.toString());
        SymbolTableMainItem newSymbolMainItem = new SymbolTableMainItem(mainActors);

        try {
            SymbolTable.top.put(newSymbolMainItem);
        }
        catch (ItemAlreadyExistsException e) {
//            System.out.println("Error in main");
        }

        SymbolTable.push(newSymbolTable);
        newSymbolMainItem.setMainSymbolTable(SymbolTable.top);
        if (mainActors.getMainActors() != null) {
            for (ActorInstantiation actorInstantiation : mainActors.getMainActors()) {
                actorInstantiation.accept(this);
            }
        }
        newSymbolMainItem.setMainSymbolTable(SymbolTable.top);
        SymbolTable.pop();
    }

    @Override
    public void visit(ActorInstantiation actorInstantiation) {
        try {
            SymbolTableLocalVariableItem newLocalVar = new SymbolTableLocalVariableItem(actorInstantiation);
            SymbolTable.top.put(newLocalVar);
        }
        catch (ItemAlreadyExistsException e) {
//            System.out.println("hey");
            String errorName = "Line:" + actorInstantiation.getLine() + ":Redefinition of variable " + actorInstantiation.getIdentifier().getName();
            errors.put(errorName, actorInstantiation.getLine());
        }
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
