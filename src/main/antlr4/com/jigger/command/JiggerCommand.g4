grammar JiggerCommand;

// Parser rules
command
    : createCommand
    | createPartCommand
    | deleteCommand
    | moveCommand
    | resizeCommand
    | rotateCommand
    | joinCommand
    | displayCommand
    | hideCommand
    | listCommand
    | showCommand
    | setCommand
    | undoCommand
    | redoCommand
    | helpCommand
    | exitCommand
    ;

createCommand
    : CREATE shape objectName (AT position)? sizeSpec? (COLOR color)?
    ;

createPartCommand
    : CREATE PART objectName (MATERIAL materialName)? SIZE partSize (AT position)? (GRAIN grainReq)?
    ;

deleteCommand
    : DELETE (objectName | ALL)
    ;

moveCommand
    : MOVE objectName TO position
    ;

resizeCommand
    : RESIZE objectName sizeSpec
    ;

rotateCommand
    : ROTATE objectName rotation
    ;

rotation
    : NUMBER COMMA NUMBER COMMA NUMBER
    ;

objectName
    : STRING
    | ID
    ;

materialName
    : STRING
    | ID
    ;

partSize
    : NUMBER COMMA NUMBER
    ;

grainReq
    : VERTICAL
    | HORIZONTAL
    | ANY_KW
    ;

joinCommand
    : JOIN objectName TO objectName WITH jointType (DEPTH NUMBER)? (SCREWS NUMBER)? (SPACING NUMBER)?
    ;

jointType
    : BUTT_JT
    | DADO_JT
    | RABBET_JT
    | POCKET_SCREW_JT
    ;

displayCommand
    : DISPLAY NAMES              // all objects
    | DISPLAY NAME objectName    // single object
    ;

hideCommand
    : HIDE NAMES                 // all objects
    | HIDE NAME objectName       // single object
    ;

listCommand
    : LIST
    ;

showCommand
    : SHOW showTarget
    | SHOW INFO objectName
    | SHOW TEMPLATE objectName
    ;

showTarget
    : UNITS
    | OBJECTS
    | MATERIALS
    | TEMPLATES
    | JOINTS
    | CUTLIST
    | BOM
    ;

setCommand
    : SET UNITS unitName
    | SET MATERIAL materialName
    ;

undoCommand
    : UNDO
    ;

redoCommand
    : REDO
    ;

helpCommand
    : HELP
    ;

exitCommand
    : EXIT
    ;

shape
    : BOX
    | SPHERE
    | CYLINDER
    ;

position
    : NUMBER COMMA NUMBER COMMA NUMBER
    ;

// Size can be specified as:
//   size 2               — uniform
//   size 1,2,3           — w,h,d as a tuple
//   width 1 height 2 depth 3  — named (any order, each optional, default 1)
sizeSpec
    : SIZE dimensions                          # sizeByDimensions
    | (widthSpec | heightSpec | depthSpec)+     # sizeByComponents
    ;

dimensions
    : NUMBER (COMMA NUMBER COMMA NUMBER)?
    ;

widthSpec
    : WIDTH NUMBER
    ;

heightSpec
    : HEIGHT NUMBER
    ;

depthSpec
    : DEPTH NUMBER
    ;

color
    : RED | GREEN | BLUE | YELLOW | WHITE | HEX_COLOR
    ;

unitName
    : ID
    ;

// Lexer rules
CREATE     : 'create' | 'cr' ;
DELETE     : 'delete' | 'del' ;
MOVE       : 'move' | 'mv' ;
RESIZE     : 'resize' ;
ROTATE     : 'rotate' | 'rot' ;
JOIN       : 'join' ;
WITH       : 'with' ;
BUTT_JT    : 'butt' ;
DADO_JT    : 'dado' ;
RABBET_JT  : 'rabbet' ;
POCKET_SCREW_JT : 'pocket_screw' | 'pocket-screw' | 'pocketscrew' | 'pocket' ;
SCREWS     : 'screws' ;
SPACING    : 'spacing' ;
JOINTS     : 'joints' ;
CUTLIST    : 'cutlist' | 'cut-list' ;
BOM        : 'bom' ;
TO         : 'to' ;
DISPLAY    : 'display' ;
HIDE       : 'hide' ;
NAME       : 'name' ;
NAMES      : 'names' ;
PART       : 'part' | 'p' ;
MATERIAL   : 'material' | 'mat' ;
GRAIN      : 'grain' | 'gr' ;
VERTICAL   : 'vertical' | 'vert' | 'v' ;
HORIZONTAL : 'horizontal' | 'horiz' | 'hz' ;
ANY_KW     : 'any' ;
MATERIALS  : 'materials' ;
TEMPLATE   : 'template' ;
TEMPLATES  : 'templates' ;
INFO       : 'info' ;
LIST       : 'list' | 'ls' ;
SHOW       : 'show' ;
SET        : 'set' ;
OBJECTS    : 'objects' ;
UNITS      : 'units' ;
UNDO       : 'undo' ;
REDO       : 'redo' ;
HELP       : 'help' | '?' ;
EXIT       : 'exit' | 'quit' | 'q' ;
AT         : 'at' | '@' ;
SIZE       : 'size' | 'sz' ;
WIDTH      : 'width' | 'w' ;
HEIGHT     : 'height' | 'h' ;
DEPTH      : 'depth' | 'd' ;
COLOR      : 'color' | 'col' ;
ALL        : 'all' ;

BOX        : 'box' ;
SPHERE     : 'sphere' ;
CYLINDER   : 'cylinder' ;

RED        : 'red' ;
GREEN      : 'green' ;
BLUE       : 'blue' ;
YELLOW     : 'yellow' ;
WHITE      : 'white' ;

COMMA      : ',' ;
NUMBER     : '-'? ( [0-9]+ ('.' [0-9]*)? | '.' [0-9]+ ) ;
HEX_COLOR  : '#' [0-9a-fA-F]+ ;
STRING     : '"' ~["]* '"' ;
ID         : [a-zA-Z_] [a-zA-Z0-9_]* ;
WS         : [ \t\r\n]+ -> skip ;
