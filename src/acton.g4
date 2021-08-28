grammar acton;

@header {
    package main.parsers;
    import main.* ;
    import main.ast.node.* ;
    import main.ast.node.declaration.* ;
    import main.ast.node.declaration.handler.* ;
    import main.ast.node.expression.* ;
    import main.ast.node.expression.operators.* ;
    import main.ast.node.expression.values.* ;
    import main.ast.node.statement.* ;
    import main.ast.type.* ;
    import main.ast.type.actorType.* ;
    import main.ast.type.arrayType.* ;
    import main.ast.type.primitiveType.* ;
    import java.util.ArrayList ;
}

program returns [Program p]
    :
    {
        $p = new Program();
    }
     (actorDeclaration { $p.addActor($actorDeclaration.actDec); } )+ mainDeclaration { $p.setMain($mainDeclaration.mainDec); }
    ;

actorDeclaration returns [ActorDeclaration actDec]
    :
        ACTOR identifier
        {
            $actDec = new ActorDeclaration($identifier.id);
            $actDec.setLine($identifier.id.getLine());

        }
        (EXTENDS identifier
        {
            $actDec.setParentName($identifier.id);
        }
        ) ? LPAREN intval = INTVAL
        {
            $actDec.setQueueSize($intval.int);
        }
        RPAREN
        LBRACE

        (KNOWNACTORS
        LBRACE
            (actorT = identifier actorN = identifier SEMICOLON
            {
                ActorType newActorType = new ActorType($actorT.id);
                newActorType.setLine($actorT.id.getLine());
                VarDeclaration varDeclare = new VarDeclaration($actorN.id, newActorType);
                varDeclare.setLine($actorT.id.getLine());
                $actDec.addKnownActor(varDeclare);
            }
            )*
        RBRACE)

        (ACTORVARS
        LBRACE
            varDeclarations { $actDec.setActorVars($varDeclarations.varDecs); }
        RBRACE)

        (initHandlerDeclaration { $actDec.setInitHandler($initHandlerDeclaration.initHandlerDec); } )?
        (msgHandlerDeclaration { $actDec.addMsgHandler($msgHandlerDeclaration.msgHandlerDec); } )*

        RBRACE
    ;

mainDeclaration returns [Main mainDec]
    :
        mainVar = MAIN
        {
            $mainDec = new Main();
            $mainDec.setLine($mainVar.getLine());
        }
    	LBRACE
        (actorInstantiation
        {
            $mainDec.addActorInstantiation($actorInstantiation.actInst);
        } )*
    	RBRACE
    ;

actorInstantiation returns [ActorInstantiation actInst]
    :	instType = identifier instId = identifier
    {
        ActorType newActorType = new ActorType($instType.id);
        newActorType.setLine($instType.id.getLine());
        $actInst = new ActorInstantiation(newActorType, $instId.id);
        $actInst.setLine($instType.id.getLine());
    }
     	LPAREN (identifier { $actInst.addKnownActor($identifier.id); } (COMMA identifier { $actInst.addKnownActor($identifier.id); })* | ) RPAREN
     	COLON LPAREN expressionList { $actInst.setInitArgs($expressionList.list); } RPAREN SEMICOLON
    ;

initHandlerDeclaration returns [InitHandlerDeclaration initHandlerDec]
    :	msgHndlr = MSGHANDLER INITIAL
    {
        Identifier identifierObj = new Identifier($INITIAL.getText());
        identifierObj.setLine($msgHndlr.getLine());
        $initHandlerDec = new InitHandlerDeclaration(identifierObj);
        $initHandlerDec.setLine($msgHndlr.getLine());
    }
        LPAREN
        argDeclarations
    {
        $initHandlerDec.setArgs($argDeclarations.argDecs);
    }
        RPAREN
     	LBRACE
     	varDeclarations
    {
        $initHandlerDec.setLocalVars($varDeclarations.varDecs);
    }
     	(statement
    {
        $initHandlerDec.addStatement($statement.statementCont);
    }
     	)*
     	RBRACE
    ;

msgHandlerDeclaration returns [MsgHandlerDeclaration msgHandlerDec]
    :	msgHndlr = MSGHANDLER identifier
     {
        $msgHandlerDec = new MsgHandlerDeclaration($identifier.id);
        $msgHandlerDec.setLine($msgHndlr.getLine());
     }
     LPAREN argDeclarations
     {
        $msgHandlerDec.setArgs($argDeclarations.argDecs);
     }
     RPAREN
       	LBRACE
       	varDeclarations
     {
         $msgHandlerDec.setLocalVars($varDeclarations.varDecs);
     }
       	(statement
     {
         $msgHandlerDec.addStatement($statement.statementCont);
     }
       	)*
       	RBRACE
    ;

argDeclarations returns [ArrayList<VarDeclaration> argDecs]
    :
    {
        $argDecs = new ArrayList<>();
    } varDeclaration
    {
        $argDecs.add($varDeclaration.varDec);
    }
    (COMMA varDeclaration
    {
        $argDecs.add($varDeclaration.varDec);
    }
    )* | { $argDecs = new ArrayList<>(); }
    ;

varDeclarations returns [ArrayList<VarDeclaration> varDecs]
    :
    {
        $varDecs = new ArrayList<>();
    }
    	(varDeclaration SEMICOLON
    {
        $varDecs.add($varDeclaration.varDec);
    }
    	)*
    ;

varDeclaration returns [VarDeclaration varDec]
    :	INT identifier
    {
        IntType typeObj = new IntType();
        typeObj.setLine($identifier.id.getLine());
        $varDec = new VarDeclaration($identifier.id, typeObj);
        $varDec.setLine($identifier.id.getLine());
    }
    |   STRING identifier
    {
        StringType typeObj = new StringType();
        typeObj.setLine($identifier.id.getLine());
        $varDec = new VarDeclaration($identifier.id, typeObj);
        $varDec.setLine($identifier.id.getLine());
    }
    |   BOOLEAN identifier
    {
        BooleanType typeObj = new BooleanType();
        typeObj.setLine($identifier.id.getLine());
        $varDec = new VarDeclaration($identifier.id, typeObj);
        $varDec.setLine($identifier.id.getLine());
    }
    |   INT identifier LBRACKET intval = INTVAL RBRACKET
    {
        ArrayType typeObj = new ArrayType($intval.int);
        $varDec = new VarDeclaration($identifier.id, typeObj);
        $varDec.setLine($identifier.id.getLine());
    }
    ;

statement returns [Statement statementCont]
    :	blockStmt
    {
        $statementCont = $blockStmt.stmts;
    }
    | 	printStmt
    {
        $statementCont = $printStmt.stmt;
    }
    |  	assignStmt
    {
        $statementCont = $assignStmt.stmt;
    }
    |  	forStmt
    {
        $statementCont = $forStmt.stmt;
    }
    |  	ifStmt
    {
        $statementCont = $ifStmt.stmt;
    }
    |  	continueStmt
    {
        $statementCont = $continueStmt.stmt;
    }
    |  	breakStmt
    {
        $statementCont = $breakStmt.stmt;
    }
    |  	msgHandlerCall
    {
        $statementCont = $msgHandlerCall.msgHandlerCont;
    }
    ;

blockStmt returns [Block stmts]
    :

    lbraceLine = LBRACE
    {
        $stmts = new Block();
        $stmts.setLine($lbraceLine.getLine());
    }
    (statement
    {
        $stmts.addStatement($statement.statementCont);
    }
    )*
    RBRACE
    ;

printStmt returns [Print stmt]
    :
    PRINT LPAREN expression
    {
        $stmt = new Print($expression.expCont);
        $stmt.setLine($expression.expCont.getLine());
    }
    RPAREN SEMICOLON
    ;

assignStmt returns [Assign stmt]
    :
    assignment
    {
        $stmt = $assignment.stmt;
    }
    SEMICOLON
    ;

assignment returns [Assign stmt]
    :   orExpression ASSIGN expression
    {
        $stmt = new Assign($orExpression.expCont, $expression.expCont);
        $stmt.setLine($orExpression.expCont.getLine());
    }
    ;

forStmt returns [For stmt]
    :
    forLine = FOR
    {
        $stmt = new For();
        $stmt.setLine($forLine.getLine());
    }
    LPAREN (assignment
    {
        $stmt.setInitialize($assignment.stmt);
    }
    )? SEMICOLON (expression
    {
        $stmt.setCondition($expression.expCont);
    }
    )? SEMICOLON (assignment
    {
        $stmt.setUpdate($assignment.stmt);
    }
    )? RPAREN statement
    {
        $stmt.setBody($statement.statementCont);
    }
    ;

ifStmt returns [Conditional stmt]
    :   ifLine = IF LPAREN expression RPAREN statement elseStmt
    {
        $stmt = new Conditional($expression.expCont, $statement.statementCont);
        $stmt.setLine($ifLine.getLine());
        $stmt.setElseBody($elseStmt.elseCont);
    }
    ;

elseStmt returns [Statement elseCont] //does empty new need setLine function?
    : ELSE statement
    {
        $elseCont = $statement.statementCont;
    }
    |
    {
        $elseCont = null;
    }
    ;

continueStmt returns [Continue stmt]
    : 	continueLine = CONTINUE
    {
        $stmt = new Continue();
        $stmt.setLine($continueLine.getLine());
    }
    SEMICOLON
    ;

breakStmt returns [Break stmt]
    : 	breakLine = BREAK
    {
        $stmt = new Break();
        $stmt.setLine($breakLine.getLine());
    }
    SEMICOLON
    ;

msgHandlerCall returns [MsgHandlerCall msgHandlerCont] locals [Expression name]
    :
    (identifier
    {
        $name = $identifier.id;
    }
    | selfLine = SELF
    {
        $name = new Self();
        $name.setLine($selfLine.getLine());
    }
    | senderLine = SENDER
    {
        $name = new Identifier($senderLine.text);
        $name.setLine($senderLine.getLine());
    }
    ) DOT
        identifier LPAREN expressionList
        {
            $msgHandlerCont = new MsgHandlerCall($name, $identifier.id);
            $msgHandlerCont.setLine($identifier.id.getLine());
            $msgHandlerCont.setArgs($expressionList.list);
        }
        RPAREN SEMICOLON
    ;

expression returns [Expression expCont] locals [Expression rValue]
    :	orExpression (ASSIGN expression
    {
        $rValue = $expression.expCont;
    })?
    {
        if ($rValue == null) {
            $expCont = $orExpression.expCont;
        }
        else {
            BinaryOperator operator = BinaryOperator.assign;
            $expCont = new BinaryExpression($orExpression.expCont, $rValue, operator);
            $expCont.setLine($orExpression.expCont.getLine());
        }
    }
    ;

orExpression returns [Expression expCont] locals [int counter, BinaryExpression root, BinaryExpression current]
    :	expA = andExpression
    {
        $counter = 0;
    }
    (OR andExpression
    {
        BinaryOperator operator = BinaryOperator.or;
        if ($counter == 0) {
            $root = new BinaryExpression($expA.expCont, $andExpression.expCont, operator);
            $root.setLine($andExpression.expCont.getLine());
            $current = $root;
            $counter = $counter + 1;
        }
        else {
            BinaryExpression newBinaryExpression = new BinaryExpression($current.getRight(), $andExpression.expCont, operator);
            newBinaryExpression.setLine($andExpression.expCont.getLine());
            $counter = $counter + 1;
            $current.setRight(newBinaryExpression);
            $current = newBinaryExpression;
        }
    })*
    {
        if ($counter == 0) {
            $expCont = $expA.expCont;
        } else {
            $expCont = $root;
        }
    }
    ;

andExpression returns [Expression expCont] locals [int counter, BinaryExpression root, BinaryExpression current]
    :	expA = equalityExpression
    {
        $counter = 0;
    }
    (AND equalityExpression
    {
        BinaryOperator operator = BinaryOperator.and;
        if ($counter == 0) {
            $root = new BinaryExpression($expA.expCont, $equalityExpression.expCont, operator);
            $root.setLine($equalityExpression.expCont.getLine());
            $current = $root;
            $counter = $counter + 1;
        }
        else {
            BinaryExpression newBinaryExpression = new BinaryExpression($current.getRight(), $equalityExpression.expCont, operator);
            newBinaryExpression.setLine($equalityExpression.expCont.getLine());
            $counter = $counter + 1;
            $current.setRight(newBinaryExpression);
            $current = newBinaryExpression;
        }
    })*
    {
        if ($counter == 0) {
            $expCont = $expA.expCont;
        } else {
            $expCont = $root;
        }
    }
    ;

equalityExpression returns [Expression expCont] locals [int counter, BinaryExpression root, BinaryExpression current, BinaryOperator operator]
    :	expA = relationalExpression
    {
        $counter = 0;
    }
    ( (EQ
    {
        $operator = BinaryOperator.eq;
    }
    |
    NEQ
    {
        $operator = BinaryOperator.neq;
    }
    ) relationalExpression
    {
        if ($counter == 0) {
            $root = new BinaryExpression($expA.expCont, $relationalExpression.expCont, $operator);
            $root.setLine($relationalExpression.expCont.getLine());
            $current = $root;
            $counter = $counter + 1;
        }
        else {
            BinaryExpression newBinaryExpression = new BinaryExpression($current.getRight(), $relationalExpression.expCont, $operator);
            newBinaryExpression.setLine($relationalExpression.expCont.getLine());
            $counter = $counter + 1;
            $current.setRight(newBinaryExpression);
            $current = newBinaryExpression;
        }
    })*
    {
        if ($counter == 0) {
            $expCont = $expA.expCont;
        } else {
            $expCont = $root;
        }
    }
    ;

relationalExpression returns [Expression expCont] locals [int counter, BinaryExpression root, BinaryExpression current, BinaryOperator operator]
    : expA = additiveExpression
    {
        $counter = 0;
    }
    ((LT
    {
        $operator = BinaryOperator.lt;
    }
    |
    GT
    {
        $operator = BinaryOperator.gt;
    }
    ) additiveExpression{
        if ($counter == 0) {
            $root = new BinaryExpression($expA.expCont, $additiveExpression.expCont, $operator);
            $root.setLine($additiveExpression.expCont.getLine());
            $current = $root;
            $counter = $counter + 1;
        }
        else {
            BinaryExpression newBinaryExpression = new BinaryExpression($current.getRight(), $additiveExpression.expCont, $operator);
            newBinaryExpression.setLine($additiveExpression.expCont.getLine());
            $counter = $counter + 1;
            $current.setRight(newBinaryExpression);
            $current = newBinaryExpression;
        }
    })*
    {
        if ($counter == 0) {
            $expCont = $expA.expCont;
        } else {
            $expCont = $root;
        }
    }
    ;

additiveExpression returns [Expression expCont] locals [int counter, BinaryExpression root, BinaryExpression current, BinaryOperator operator]
    : expA = multiplicativeExpression
    {
        $counter = 0;
    }
    ((PLUS
    {
        $operator = BinaryOperator.add;
    }
    |
    MINUS
    {
        $operator = BinaryOperator.sub;
    }) multiplicativeExpression{
       if ($counter == 0) {
           $root = new BinaryExpression($expA.expCont, $multiplicativeExpression.expCont, $operator);
           $root.setLine($multiplicativeExpression.expCont.getLine());
           $current = $root;
           $counter = $counter + 1;
       }
       else {
           BinaryExpression newBinaryExpression = new BinaryExpression($current.getRight(), $multiplicativeExpression.expCont, $operator);
           newBinaryExpression.setLine($multiplicativeExpression.expCont.getLine());
           $counter = $counter + 1;
           $current.setRight(newBinaryExpression);
           $current = newBinaryExpression;
       }
    })*
    {
       if ($counter == 0) {
           $expCont = $expA.expCont;
       } else {
           $expCont = $root;
       }
    }
    ;

multiplicativeExpression returns [Expression expCont] locals [int counter, BinaryExpression root, BinaryExpression current, BinaryOperator operator]
    : expA = preUnaryExpression
     {
         $counter = 0;
     }
     ((MULT
     {
        $operator = BinaryOperator.mult;
     }
     |
     DIV
     {
        $operator = BinaryOperator.div;
     }
     |
     PERCENT
     {
        $operator = BinaryOperator.mod;
     }
     ) preUnaryExpression{
        if ($counter == 0) {
            $root = new BinaryExpression($expA.expCont, $preUnaryExpression.expCont, $operator);
            $root.setLine($preUnaryExpression.expCont.getLine());
            $current = $root;
            $counter = $counter + 1;
        }
        else {
            BinaryExpression newBinaryExpression = new BinaryExpression($current.getRight(), $preUnaryExpression.expCont, $operator);
            newBinaryExpression.setLine($preUnaryExpression.expCont.getLine());
            $counter = $counter + 1;
            $current.setRight(newBinaryExpression);
            $current = newBinaryExpression;
        }
     })*
     {
        if ($counter == 0) {
            $expCont = $expA.expCont;
        } else {
            $expCont = $root;
        }
     }
    ;

preUnaryExpression returns [Expression expCont]
    :   NOT preUnaryExpression
    {
        UnaryOperator operator = UnaryOperator.not;
        $expCont = new UnaryExpression(operator, $preUnaryExpression.expCont);
        $expCont.setLine($preUnaryExpression.expCont.getLine());
    }
    |   MINUS preUnaryExpression
    {
        UnaryOperator operator = UnaryOperator.minus;
        $expCont = new UnaryExpression(operator, $preUnaryExpression.expCont);
        $expCont.setLine($preUnaryExpression.expCont.getLine());
    }
    |   PLUSPLUS preUnaryExpression
    {
        UnaryOperator operator = UnaryOperator.preinc;
        $expCont = new UnaryExpression(operator, $preUnaryExpression.expCont);
        $expCont.setLine($preUnaryExpression.expCont.getLine());
    }
    |   MINUSMINUS preUnaryExpression
    {
        UnaryOperator operator = UnaryOperator.predec;
        $expCont = new UnaryExpression(operator, $preUnaryExpression.expCont);
        $expCont.setLine($preUnaryExpression.expCont.getLine());
    }
    |   postUnaryExpression
    {
        $expCont = $postUnaryExpression.expCont;
        $expCont.setLine($postUnaryExpression.expCont.getLine());
    }
    ;

postUnaryExpression returns [Expression expCont] locals [UnaryOperator operator]
    :   otherExpression (postUnaryOp
    {
        $operator = $postUnaryOp.operator;
    }
    )?
    {
        if ($operator == null) {
            $expCont = $otherExpression.expCont;
        }
        else {
            $expCont = new UnaryExpression($operator, $otherExpression.expCont);
            $expCont.setLine($otherExpression.expCont.getLine());
        }
    }
    ;

postUnaryOp returns [UnaryOperator operator]
    :	PLUSPLUS
    {
        $operator = UnaryOperator.postinc;
    }
    | MINUSMINUS
    {
        $operator = UnaryOperator.postdec;
    }
    ;

otherExpression returns [Expression expCont]
    :    LPAREN expression RPAREN
    {
        $expCont = $expression.expCont;
    }
    |    identifier
    {
        $expCont = $identifier.id;
    }
    |    arrayCall
    {
        $expCont = $arrayCall.arrayCallCont;
    }
    |    actorVarAccess
    {
        $expCont = $actorVarAccess.actorVar;
    }
    |    value
    {
        $expCont = $value.val;
    }
    |    senderLine = SENDER
    {
        Sender s = new Sender();
        $expCont = s;
        $expCont.setLine($senderLine.getLine());
    }
    ;

arrayCall returns [ArrayCall arrayCallCont] locals [Expression instance]
    :   (identifier
    {
        $instance = $identifier.id;
    }
     | actorVarAccess
    {
        $instance = $actorVarAccess.actorVar;
    }) LBRACKET expression
    {
        $arrayCallCont = new ArrayCall($instance, $expression.expCont);
    } RBRACKET
    ;

actorVarAccess returns [ActorVarAccess actorVar]
    :   selfLine = SELF DOT identifier
    {
        $actorVar = new ActorVarAccess($identifier.id);
        $actorVar.setLine($selfLine.getLine());

    }
    ;

expressionList returns [ArrayList<Expression> list]
    :
    {
        $list = new ArrayList<>();
    }
    (expression
    {
        $list.add($expression.expCont);
    }
    (COMMA expression
    {
        $list.add($expression.expCont);
    }
    )* |
    {
        $list = new ArrayList<>();
    }
    )
    ;

identifier returns [Identifier id]
    :   identifierLine = IDENTIFIER
    {
        $id = new Identifier($identifierLine.text);
        $id.setLine($identifierLine.getLine());
    }
    ;

value returns [Value val]
    :   intLine = INTVAL
    {
        IntType _intType = new IntType();
        _intType.setLine($intLine.getLine());
        $val = new IntValue($intLine.int, _intType);
        $val.setLine($intLine.getLine());
    }
    | stringLine = STRINGVAL
    {
        StringType _stringType = new StringType();
        _stringType.setLine($stringLine.getLine());
        $val = new StringValue($stringLine.text, _stringType);
        $val.setLine($stringLine.getLine());
    } | trueLine = TRUE
    {
        BooleanType _boolType = new BooleanType();
        _boolType.setLine($trueLine.getLine());
        $val = new BooleanValue(true, _boolType);
        $val.setLine($trueLine.getLine());
    } | falseLine = FALSE
    {
        BooleanType _boolType = new BooleanType();
        _boolType.setLine($falseLine.getLine());
        $val = new BooleanValue(false, _boolType);
        $val.setLine($falseLine.getLine());
    }
    ;

// values
INTVAL
    : [1-9][0-9]* | [0]
    ;

STRINGVAL
    : '"'~["]*'"'
    ;

TRUE
    :   'true'
    ;

FALSE
    :   'false'
    ;

//types
INT
    : 'int'
    ;

BOOLEAN
    : 'boolean'
    ;

STRING
    : 'string'
    ;

//keywords
ACTOR
	:	'actor'
	;

EXTENDS
	:	'extends'
	;

ACTORVARS
	:	'actorvars'
	;

KNOWNACTORS
	:	'knownactors'
	;

INITIAL
    :   'initial'
    ;

MSGHANDLER
	: 	'msghandler'
	;

SENDER
    :   'sender'
    ;

SELF
    :   'self'
    ;

MAIN
	:	'main'
	;

FOR
    :   'for'
    ;

CONTINUE
    :   'continue'
    ;

BREAK
    :   'break'
    ;

IF
    :   'if'
    ;

ELSE
    :   'else'
    ;

PRINT
    :   'print'
    ;

//symbols
LPAREN
    :   '('
    ;

RPAREN
    :   ')'
    ;

LBRACE
    :   '{'
    ;

RBRACE
    :   '}'
    ;

LBRACKET
    :   '['
    ;

RBRACKET
    :   ']'
    ;

COLON
    :   ':'
    ;

SEMICOLON
    :   ';'
    ;

COMMA
    :   ','
    ;

DOT
    :   '.'
    ;

//operators
ASSIGN
    :   '='
    ;

EQ
    :   '=='
    ;

NEQ
    :   '!='
    ;

GT
    :   '>'
    ;

LT
    :   '<'
    ;

PLUSPLUS
    :   '++'
    ;

MINUSMINUS
    :   '--'
    ;

PLUS
    :   '+'
    ;

MINUS
    :   '-'
    ;

MULT
    :   '*'
    ;

DIV
    :   '/'
    ;

PERCENT
    :   '%'
    ;

NOT
    :   '!'
    ;

AND
    :   '&&'
    ;

OR
    :   '||'
    ;

QUES
    :   '?'
    ;

IDENTIFIER
    :   [a-zA-Z_][a-zA-Z0-9_]*
    ;

COMMENT
    :   '//' ~[\n\r]* -> skip
    ;

WHITESPACE
    :   [ \t\r\n] -> skip
    ;