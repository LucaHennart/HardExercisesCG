/**
 * Version 1 : Cost n²log n (or maybe n.a.log n, a being the max number of neighbours of a node, don't remember)
 * Can be optimized to n.log n easily
 *      - Add a successor array in order not to have to parse in all the predecessor map if the node is a
 *      predecessor of another node or something equivalent
 *      - Since it passes the tests and don't have any more time to spend on it, let's keep it that way
 * To be done in **Version 2**:
 * Code cleaning (no more red tape patches)
 * More function, less code inside
 * Go for n.log n complexity
 */

import java.util.*;

class Player {

    public static void main(String args[]) throws Exception {
        Scanner in = new Scanner(System.in);
        int N = in.nextInt(); // the total number of nodes in the level, including the gateways
        int L = in.nextInt(); // the number of links
        int E = in.nextInt(); // the number of exit gateways

        Parser parser = new Parser(in);
        Graph graph = parser.parseInput(L, E);
        while (true) {
            int SI = in.nextInt(); // The index of the node on which the Skynet agent is positioned this turn
            graph.setStartNode(SI);

            Node node = graph.BFSAlgorithm();
            Node gateway = graph.updateGraph(node);
            System.out.println(graph.outputResult(node, gateway));
        }
    }
}


class Node {

    private int name;
    private Set<Node> neighbours;
    private char type;

    public Node(int name) {
        this.name = name;
        this.neighbours = new HashSet<>();
        this.type = Constants.NORMAL_NODE;
    }


    public int getName() {
        return name;
    }

    public void addNeighbour(Node neighbour) {
        this.neighbours.add(neighbour);
    }

    public Set<Node> getNeighbours() {
        return neighbours;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(
                "Node " + this.name + " of " + "type " + this.type + " has neighbours :\n\t");

        for(Node neighbour : this.neighbours) {
            result.append("Node ").append(neighbour.name).append(" of ").append("type : ").
                    append(neighbour.type).append("\n\t");
        }
        return result.append("\n").toString();
    }

    public void removeNeighbour(Node neighbour) {
        this.neighbours.remove(neighbour);
    }

    public boolean isLinkedToGW() {
        for(Node neighbour : this.getNeighbours()) {
            if(neighbour.getType() == Constants.GATEWAY) {
                return true;
            }
        }
        return false;
    }
}


class Graph {

    private Map<Integer, Node> dict;
    private Node startNode;


    public Graph() {
        this.dict = new HashMap<>();
        this.startNode = null;
    }

    public Node addOrGetNode(int nodeName) {
        Node createdOrRetrieved;
        if(!this.containsNode(nodeName)) {
            createdOrRetrieved = new Node(nodeName);
            this.dict.put(nodeName, createdOrRetrieved);
        } else {
            createdOrRetrieved = this.getNode(nodeName);
        }

        return createdOrRetrieved;
    }


    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("startNode = " + startNode.getName() + "\n\n");
        Set<Integer> keys = dict.keySet();
        for(int key : keys) {
            output.append(dict.get(key).toString());
        }
        return output.toString();
    }


    public Node getNode(int name) {
        return this.dict.get(name);
    }

    public boolean containsNode(int name) {
        return this.dict.containsKey(name);
    }

    public void setStartNode(int startNode) {
        this.startNode = dict.get(startNode);
    }


    public Node BFSAlgorithm() throws Exception {

        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new ArrayDeque<>();
        Map<Integer, List<Integer>> predecessors = new HashMap<>();

        int currentNodeName = this.startNode.getName();
        visited.add(currentNodeName);
        queue.add(currentNodeName);
        this.addPredecessor(predecessors, currentNodeName, null, 0);

        while(!queue.isEmpty()) {
            currentNodeName = queue.remove();
            int distance = predecessors.get(currentNodeName).get(0);

            Set<Node> currentNodeNeighbours = this.dict.get(currentNodeName).getNeighbours();
            int nbGateways = 0;
            for(Node neighbour : currentNodeNeighbours) {
                int neighbourName = neighbour.getName();
                if(neighbour.getType() == Constants.GATEWAY) {
                    nbGateways++;
                } else if(!visited.contains(neighbourName)) {
                    visited.add(neighbourName );
                    queue.add(neighbourName);
                    this.addPredecessor(predecessors, neighbourName, currentNodeName, distance + 1);
                }

            }

            if(nbGateways > 0) {
                int updatedDistance = this.getPredecessorDistance(predecessors, currentNodeName) - nbGateways;
                this.updateDistance(predecessors, currentNodeName, updatedDistance, nbGateways);
            }

        }

        Set<Integer> keys = predecessors.keySet();
        int minValue = Integer.MAX_VALUE;
        List<Integer> indexes = new ArrayList<>();
        for(int key : keys) {
            if(this.dict.get(key).isLinkedToGW()) {
                if(predecessors.get(key).get(0) < minValue) {
                    if(predecessors.get(key).get(1) != null || predecessors.get(key).get(0) != 0) {
                        minValue = predecessors.get(key).get(0);
                        indexes.clear();
                        indexes.add(key);
                    }
                } else if(predecessors.get(key).get(0) == minValue) {
                    indexes.add(key);
                }
                if(predecessors.get(key).get(1) == null && predecessors.get(key).get(0) == -1) {
                    indexes.clear();
                    indexes.add(key);
                    break;
                }
            }
        }

        if(indexes.size() > 1) {
            int startNode = this.startNode.getName();
            int maxNbTurns = Integer.MAX_VALUE;
            int chosenNode = -1;
            for(int tmp : indexes) {
                int numberTurns = 1;
                int nodeName = tmp;
                while(predecessors.get(nodeName).get(1) != startNode) {
                    nodeName = predecessors.get(nodeName).get(1);
                    numberTurns++;
                }
                if(numberTurns < maxNbTurns) {
                    maxNbTurns = numberTurns;
                    chosenNode = tmp;
                }
            }
            return this.dict.get(chosenNode);
        } else {
            return this.dict.get(indexes.get(0));
        }
    }

    public void printPredecessors(Map<Integer, List<Integer>> predecessors) {
        Set<Integer> keys = predecessors.keySet();
        StringBuilder nodes = new StringBuilder("Nodes :        ");
        StringBuilder distances = new StringBuilder("Distances :    ");
        StringBuilder predecessorNodes = new StringBuilder("Predecessors : ");
        for(int key : keys) {
            nodes.append(key).append("  ");
            if(key <= 9) {
                nodes.append(" ");
            }

            if(predecessors.get(key).get(1) == null) {
                predecessorNodes.append("/").append("   ");
            } else {
                predecessorNodes.append(predecessors.get(key).get(1)).append("  ");
                if(predecessors.get(key).get(1) <= 9) {
                    predecessorNodes.append(" ");
                }
            }

            distances.append(predecessors.get(key).get(0));
            distances.append(predecessors.get(key).get(0) < 0 ? " " : "  ");
            distances.append(predecessors.get(key).get(0) <= 9 ? " " : "");
        }
        System.err.println(nodes);
        System.err.println(predecessorNodes);
        System.err.println(distances);
    }



    private void updateDistance(Map<Integer, List<Integer>> predecessors, int currentNodeName, int updatedDistance, int nbGW) {
        Set<Integer> keys = predecessors.keySet();
        for(int key : keys) {
            if(predecessors.get(key).get(1) != null && predecessors.get(key).get(1) == currentNodeName) {
                predecessors.get(key).set(0, predecessors.get(key).get(0) - nbGW);
            }
        }
        predecessors.get(currentNodeName).set(0, updatedDistance);
    }

    private int getPredecessorDistance(Map<Integer, List<Integer>> predecessors, int currentNodeName) {
        return predecessors.get(currentNodeName).get(0);
    }


    public Node updateGraph(Node currentNode) {
        Set<Node> neighbours = currentNode.getNeighbours();
        for(Node neighbour : neighbours) {
            if(neighbour.getType() == Constants.GATEWAY) {
                currentNode.removeNeighbour(neighbour);
                neighbour.removeNeighbour(currentNode);
                return neighbour;
            }
        }
        throw new Error("No Gateway found");
    }

    public String outputResult(Node currentNode, Node gatewayNode) {
        return currentNode.getName() + " " + gatewayNode.getName();
    }

    private void addPredecessor(Map<Integer, List<Integer>> predecessors, int currentNode,
                                Integer previousNode, int distance) throws Exception {
        if(!predecessors.containsKey(currentNode)) {
            predecessors.put(currentNode, new ArrayList<>());
        } else {
            throw new Exception("If a Node is visited and because you're doing a BFS, you shouldn't have " +
                    "to go twice in a Node");
        }

        predecessors.get(currentNode).add(distance);
        predecessors.get(currentNode).add(previousNode);

    }
}


class Parser {

    private Scanner sc;
    public Parser(Scanner sc) {
        this.sc = sc;
    }

    public Graph parseInput(int nbLinks, int nbExits) {
        Graph graph = new Graph();


        for (int i = 0; i < nbLinks; i++) {
            int n1Name = sc.nextInt(); // N1 and N2 defines a link between these nodes
            int n2Name = sc.nextInt();
            Node n1 = graph.addOrGetNode(n1Name);
            Node n2 = graph.addOrGetNode(n2Name);
            n1.addNeighbour(n2);
            n2.addNeighbour(n1);

        }
        for (int i = 0; i < nbExits; i++) {
            int EI = sc.nextInt(); // the index of a gateway node
            graph.getNode(EI).setType(Constants.GATEWAY);
        }

        return graph;
    }
}



class Constants {
    protected static final char NORMAL_NODE = 'N';
    protected static final char GATEWAY = 'G';
}