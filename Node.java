

package com.ruleengine;

public class Node {
    public String type; // "operator" or "operand"
    public Node left;
    public Node right;
    public String value; // For operator nodes, value will be "AND" or "OR"

    // Constructor for operand nodes
    public Node(String type, String value) {
        this.type = type;
        this.value = value;
    }

    // Constructor for operator nodes
    public Node(String type, String operator, Node left, Node right) {
        this.type = type;
        this.value = operator; // Set value to operator (AND/OR)
        this.left = left;
        this.right = right;
    }
}
