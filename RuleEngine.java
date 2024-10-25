package com.ruleengine;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleEngine {

    // Method to create an AST from a rule string
    public Node createRule(String rule) {
        Stack<Node> stack = new Stack<>();
        Stack<String> operators = new Stack<>();

        // Define regex patterns to match conditions and operators separately
        Pattern conditionPattern = Pattern.compile("\\b\\w+\\s*[><=]\\s*\\S+");
        Pattern operatorPattern = Pattern.compile("\\b(AND|OR)\\b");

        int currentIndex = 0;
        while (currentIndex < rule.length()) {
            if (rule.charAt(currentIndex) == '(') {
                operators.push("(");
                currentIndex++;
            } else if (rule.charAt(currentIndex) == ')') {
                // Process operators until the last left parenthesis
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    if (stack.size() < 2) {
                        System.err.println("Error: Not enough operands for operator " + operators.peek());
                        return null;
                    }
                    Node right = stack.pop();
                    Node left = stack.pop();
                    stack.push(new Node("operator", operators.pop(), left, right));
                }
                operators.pop(); // Pop the left parenthesis
                currentIndex++;
            } else {
                Matcher conditionMatcher = conditionPattern.matcher(rule);
                Matcher operatorMatcher = operatorPattern.matcher(rule);

                if (conditionMatcher.find(currentIndex) && conditionMatcher.start() == currentIndex) {
                    // If a condition is found, push it as a single operand
                    String condition = conditionMatcher.group();
                    stack.push(new Node("operand", condition));
                    currentIndex = conditionMatcher.end();
                } else if (operatorMatcher.find(currentIndex) && operatorMatcher.start() == currentIndex) {
                    // If an operator is found, process it based on precedence
                    String operator = operatorMatcher.group();
                    while (!operators.isEmpty() && !operators.peek().equals("(")) {
                        Node right = stack.pop();
                        Node left = stack.pop();
                        stack.push(new Node("operator", operators.pop(), left, right));
                    }
                    operators.push(operator);
                    currentIndex = operatorMatcher.end();
                } else {
                    currentIndex++;
                }
            }
        }

        // Process remaining operators in the stack
        while (!operators.isEmpty()) {
            if (stack.size() < 2) {
                System.err.println("Error: Not enough operands for operator " + operators.peek());
                return null;
            }
            Node right = stack.pop();
            Node left = stack.pop();
            stack.push(new Node("operator", operators.pop(), left, right));
        }

        return stack.isEmpty() ? null : stack.pop();
    }

    // Method to combine multiple rules into a single AST
    public Node combineRules(List<Node> rules) {
        Node combined = rules.get(0);
        for (int i = 1; i < rules.size(); i++) {
            combined = new Node("operator", "AND", combined, rules.get(i));
        }
        return combined;
    }

    // Method to evaluate a rule against input data
    public boolean evaluateRule(Node node, Map<String, Object> data) {
        if (node == null) return false;

        if (node.type.equals("operand")) {
            // Operand node, e.g., "age > 30"
            String[] parts = node.value.trim().split(" ", 3); // Limit split to 3 parts: field, operator, value

            // Check if the operand has exactly three parts: field, operator, and value
            if (parts.length != 3) {
                System.err.println("Invalid operand format: " + node.value);
                return false;
            }

            String field = parts[0];
            String operator = parts[1];
            String value = parts[2].replace("'", ""); // Remove any single quotes around strings

            if (data.containsKey(field)) {
                Object fieldValue = data.get(field);
                return compareValues(fieldValue, operator, value);
            }
            return false;
        } else if (node.type.equals("operator")) {
            // Process AND/OR operators
            if (node.value.equals("AND")) {
                return evaluateRule(node.left, data) && evaluateRule(node.right, data);
            } else if (node.value.equals("OR")) {
                return evaluateRule(node.left, data) || evaluateRule(node.right, data);
            }
        }
        return false;
    }

    // Helper method to compare values based on the operator
    private boolean compareValues(Object fieldValue, String operator, String value) {
        if (fieldValue instanceof Integer) {
            int intValue = Integer.parseInt(value);
            if (operator.equals(">")) return (int) fieldValue > intValue;
            if (operator.equals("<")) return (int) fieldValue < intValue;
            if (operator.equals("=")) return (int) fieldValue == intValue;
        } else if (fieldValue instanceof String) {
            if (operator.equals("=")) return fieldValue.equals(value);
        }
        return false;
    }

    // Method to print AST for debugging
    public void printAST(Node node, int depth) {
        if (node == null) return;

        for (int i = 0; i < depth; i++) System.out.print("  ");
        System.out.println(node.type + ": " + node.value);

        printAST(node.left, depth + 1);
        printAST(node.right, depth + 1);
    }
}
