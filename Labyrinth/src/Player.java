import java.util.*;

/**
 * Version 1 : n.log n complexity
 * - The maze is rebuilt at every iteration
 * To be done in **Version 2** :
 * Update the maze and not rebuild it
 * Code cleaning
 * More function, less code inside
 */
class Player {


    private static final int NEXT_STATE = -1;
    private static final int MAZE_DISCOVERY = 0;
    private static final int SWITCH_PATHING = 1;
    private static final int ENTRANCE_PATHING = 2;


    public static void main(String args[]) throws Exception {
        Scanner in = new Scanner(System.in);
        int R = in.nextInt(); // number of rows.
        int C = in.nextInt(); // number of columns.
        in.nextInt(); // number of rounds between the time the alarm countdown is activated and the time the alarm goes off.
        Player player = new Player();
        int state = MAZE_DISCOVERY;

        while (true) {
            int Y = in.nextInt(); // row where Kirk is located.
            int X = in.nextInt(); // column where Kirk is located.
            List<String> inputLabyrinth = new ArrayList<>(R);

            for (int i = 0; i < R; i++) {
                String ROW = in.next(); // C of the characters in '#.TC?' (i.e. one line of the ASCII maze).
                inputLabyrinth.add(ROW);
            }

            char groundType = player.getTypeGround(inputLabyrinth, X, Y);
            Node startNode = new Node(X, Y, groundType);
            Graph graph = new Graph(startNode);
            player.parseLabyrinth(graph, inputLabyrinth, R, C);


            if(state == MAZE_DISCOVERY) {
                if(graph.getStartNode().getType() != Constants.SWITCH) {
                    Integer nextUnknownNodeName = player.BFSAlgorithm(graph, Constants.UNKNOWN);
                    if(nextUnknownNodeName == NEXT_STATE) {
                        state = SWITCH_PATHING;
                    } else {
                        System.out.println(player.convertIntoOutput(
                                graph.getStartNode().getNeighbourDirection(nextUnknownNodeName)));
                    }
                } else {
                    state = ENTRANCE_PATHING;
                }
            }

            if(state == SWITCH_PATHING) {
                if(graph.getStartNode().getType() != Constants.SWITCH) {
                    Integer nextUnknownNodeName = player.BFSAlgorithm(graph, Constants.SWITCH);
                    System.out.println(player.convertIntoOutput(
                            graph.getStartNode().getNeighbourDirection(nextUnknownNodeName)));
                } else {
                    state = ENTRANCE_PATHING;
                }
            }

            if(state == ENTRANCE_PATHING) {
                Integer nextUnknownNodeName = player.BFSAlgorithm(graph, Constants.START_MAZE);
                System.out.println(player.convertIntoOutput(
                        graph.getStartNode().getNeighbourDirection(nextUnknownNodeName)));
            }


        }

    }



    private void parseLabyrinth(Graph graph, List<String> inputLabyrinth, int R, int C) throws Exception {
        for (int y = 0; y < R; y++) {
            String row = inputLabyrinth.get(y);
            for (int x = 0; x < C; x++) {
                char type = row.charAt(x);
                Coordinate c = new Coordinate(x, y);
                Node current;

                if (type != Constants.WALL) {
                    if (!graph.containsNode(c)) {
                        current = new Node(c, type);
                        graph.addNode(current);
                    } else {
                        current = graph.getNode(c);
                    }

                    boolean specialCase = false;
                    boolean corner = false;

                    if (x == 0 && y == 0) {
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.BOTTOM);
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.RIGHT);
                        corner = true;
                    } else if (x == 0 && y == (R - 1)) {
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.TOP);
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.RIGHT);
                        corner = true;
                    } else if (x == (C - 1) && y == 0) {
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.BOTTOM);
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.LEFT);
                        corner = true;
                    } else if (x == (C - 1) && y == (R - 1)) {
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.TOP);
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.LEFT);
                        corner = true;
                    }

                    if (!corner) {
                        if (x == 0) {
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.BOTTOM);
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.TOP);
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.RIGHT);
                            specialCase = true;
                        }
                        if (y == 0) {
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.BOTTOM);
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.LEFT);
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.RIGHT);
                            specialCase = true;
                        }
                        if (x == (C - 1)) {
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.BOTTOM);
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.TOP);
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.LEFT);
                            specialCase = true;
                        }
                        if (y == (R - 1)) {
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.TOP);
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.LEFT);
                            current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.RIGHT);
                            specialCase = true;
                        }
                    }

                    if (!corner && !specialCase) {
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.TOP);
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.BOTTOM);
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.RIGHT);
                        current.addRelativeNeighbour(graph, inputLabyrinth, x, y, Constants.LEFT);
                    }

                }
            }
        }

    }

    public char getTypeGround(List<String> inputLabyrinth, int x, int y) {
        return  inputLabyrinth.get(y).charAt(x);
    }

    public Integer BFSAlgorithm(Graph graph, char objective) {

        Set<Integer> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        Map<Integer, List<Integer>> predecessors = new HashMap<>();
        boolean mazeDiscovered = true;

        Node currentNode = graph.getStartNode();
        queue.add(currentNode);
        this.initializePredecessorNode(predecessors, currentNode, null, 0);
        visited.add(currentNode.getName());

        while(!queue.isEmpty()) {
            currentNode = queue.remove();
            int distance = predecessors.get(currentNode.getName()).get(0);
            if(currentNode.getType() != objective) {
                Map<Character, Node> neighbours = currentNode.getNeighbours();
                Set<Character> keys = neighbours.keySet();
                for(char key : keys) {
                    Node neighbour = neighbours.get(key);
                    Integer nodeName = neighbour.getName();
                    if(!visited.contains(nodeName)) {
                        visited.add(nodeName);
                        this.initializePredecessorNode(predecessors, neighbour, currentNode, distance + 1);
                        queue.add(neighbour);
                    }
                }
            } else {
                mazeDiscovered = false;
                break;
            }
        }

        if(mazeDiscovered) {
            return NEXT_STATE;
        }

        Integer nextNodeName = currentNode.getName();
        while(predecessors.get(nextNodeName).get(1) != graph.getStartNode().getName()) {
            nextNodeName = predecessors.get(nextNodeName).get(1);
        }

        return nextNodeName;
    }

    private void initializePredecessorNode(Map<Integer, List<Integer>> predecessors, Node node, Node predecessorNode, int distance) {
        List<Integer> newEntry = new ArrayList<>();
        newEntry.add(distance);
        if(predecessorNode != null) {
            newEntry.add(predecessorNode.getName());
        } else {
            newEntry.add(null);
        }

        predecessors.put(node.getName(), newEntry);
    }


    private String convertIntoOutput(Character direction) throws Exception {
        String output;
        if(direction == Constants.RIGHT) {
            output = "RIGHT";
        } else if(direction == Constants.LEFT) {
            output = "LEFT";
        } else if(direction == Constants.TOP) {
            output = "UP";
        } else if(direction == Constants.BOTTOM) {
            output = "DOWN";
        } else {
            throw new Exception("[ERROR] Unknown direction. Cannot be converted");
        }
        return output;
    }

}


class Graph {

    private Map<Integer, Node> dict;
    private Node startNode;

    public Graph(Node node) {
        this.dict = new HashMap<>();
        this.addNode(node);
        this.startNode = node;
    }

    public Node getStartNode() {
        return startNode;
    }

    public Node getNode(Coordinate c) {
        return dict.get(c.hashCode());
    }

    public void addNode(Node node) {
        this.dict.put(node.getName(), node);
    }

    public boolean containsNode (Coordinate coordinate) {
        return this.dict.containsKey(coordinate.hashCode());
    }


    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("startNode = " + startNode.toString() + "\n");
        Set<Integer> keys = dict.keySet();
        for(int key : keys) {
            output.append(dict.get(key).toString());
        }
        return output.toString();
    }


}


class Node {

    private int name;
    private Coordinate coordinate;
    private Map<Character, Node> neighbours;
    private char type;

    public Node() { }

    public Node(int x, int y, char type) {
        this.coordinate = new Coordinate(x, y);
        this.name = coordinate.hashCode();
        this.neighbours = new HashMap<>();
        this.type = type;
    }

    public Node(Coordinate coordinate, char type) {
        this.coordinate = coordinate;
        this.name = coordinate.hashCode();
        this.neighbours = new HashMap<>();
        this.type = type;
    }

    private void addNeighbour(Graph graph, char direction, Coordinate crdNeighbour, char type) {
        Node neighbour;
        if(type != Constants.WALL) {
            if(!graph.containsNode(crdNeighbour)) {
                neighbour = new Node(crdNeighbour, type);
                graph.addNode(neighbour);
            } else {
                neighbour = graph.getNode(crdNeighbour);
            }
            this.neighbours.put(direction, neighbour);
        }
    }

    public void addRelativeNeighbour(Graph graph, List<String> inputLabyrinth,
                                        int x, int y, char position) throws Exception {

        if(position == Constants.TOP) {
            this.addNeighbour(graph, Constants.TOP, new Coordinate(x, y - 1), inputLabyrinth.get(y - 1).charAt(x));
        } else if(position == Constants.BOTTOM) {
            this.addNeighbour(graph, Constants.BOTTOM, new Coordinate(x, y + 1), inputLabyrinth.get(y + 1).charAt(x));
        } else if(position == Constants.RIGHT) {
            this.addNeighbour(graph, Constants.RIGHT, new Coordinate(x + 1, y), inputLabyrinth.get(y).charAt(x + 1));
        } else if(position == Constants.LEFT) {
            this.addNeighbour(graph, Constants.LEFT, new Coordinate(x - 1, y), inputLabyrinth.get(y).charAt(x - 1));
        } else {
            throw new Exception("Unknown relative position");
        }

    }




    public char getNeighbourDirection(Integer nodeName) throws Exception {
        Set<Character> keys = this.neighbours.keySet();
        for(char key : keys) {
            if(this.neighbours.get(key).getName() == nodeName) {
                return key;
            }
        }
        throw new Exception("No neighbour with such name");
    }

    public Map<Character, Node> getNeighbours() {
        return this.neighbours;
    }

    public int getName() {
        return name;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public char getType() {
        return type;
    }

    @Override
    public String toString() {
        return coordinate.toString() + " : " + type + "\n\t" +
                (neighbours.containsKey('T') ? "TOP : "     + neighbours.get('T').type  + "\n\t" : "") +
                (neighbours.containsKey('B') ? "BOTTOM : "  + neighbours.get('B').type + "\n\t" : "") +
                (neighbours.containsKey('L') ? "LEFT : "    + neighbours.get('L').type + "\n\t" : "") +
                (neighbours.containsKey('R') ? "RIGHT : "   + neighbours.get('R').type + "\n\t" : "");
    }
}



class Coordinate {

    private int x;
    private int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "( " + x + " ; " + y + " )";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Coordinate) {
            Coordinate coordinate = (Coordinate) obj;
            return this.x == coordinate.x && this.y == coordinate.y;
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + this.x;
        hash = 31 * hash + this.y;
        return hash;
    }
}


class Constants {
    protected static final char TOP = 'T';
    protected static final char BOTTOM = 'B';
    protected static final char LEFT = 'L';
    protected static final char RIGHT = 'R';

    protected static final char UNKNOWN = '?';
    protected static final char SWITCH = 'C';
    protected static final char START_MAZE = 'T';
    protected static final char WALL = '#';

}