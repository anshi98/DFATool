# DFATool

![alt text](https://raw.githubusercontent.com/anshi98/DFATool/master/FSAUtility/res/example1.PNG)

Tool used to simulate deterministic automaton. Swing used for GUI.

## Bugs
- [ ] Serialization only works in some cases. Throws a NullPointerException for hashCode() in Node.java

## Potential To-dos
- [ ] Change how two-way connections appear
- [ ] Add application settings feature

## Instructions
#### Controls
- *DEL* deletes a selected node
- *Left click* used to select nodes and connections
- *Middle click* used to create a connection (Use MMB to select starting node of connection and LMB to select ending node of connection)
- *Right click* used to create new nodes

#### Miscellaneous
- Starting nodes of to-be-created connections are colored green
- The current node the program is on during a query string test is colored red
- User-selected nodes and connections are colored blue
- All testing will start from the **Beginning** node, indicated by an inwards-pointing arrow from the left
- Terminal nodes are indicated by a 2-layered circle
- Connections which start and end on the same node are indicated with an inwards-pointing arrow from the bottom
