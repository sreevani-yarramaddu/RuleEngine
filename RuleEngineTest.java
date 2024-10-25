package com.ruleengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleEngineTest {

    public static void main(String[] args) {
        RuleEngine engine = new RuleEngine();

        // Test 1: Create individual rule
        Node rule1 = engine.createRule("( age > 30 AND department = 'Sales' )");
        System.out.println("AST for Rule 1:");
        engine.printAST(rule1, 0);

        // Test 2: Combine rules
        Node rule2 = engine.createRule("( salary > 50000 OR experience > 5 )");
        List<Node> rules = new ArrayList<>();
        rules.add(rule1);
        rules.add(rule2);
        Node combinedRule = engine.combineRules(rules);
        System.out.println("\nAST for Combined Rule:");
        engine.printAST(combinedRule, 0);

        // Test 3: Evaluate rule
        Map<String, Object> userData = new HashMap<>();
        userData.put("age", 35);
        userData.put("department", "Sales");
        userData.put("salary", 60000);
        userData.put("experience", 3);

        boolean result = engine.evaluateRule(combinedRule, userData);
        System.out.println("\nEvaluation result: " + result); // Should return true
    }
}
