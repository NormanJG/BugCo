package com.cab302.bugco;

import java.util.*;

public class QuestionRepository {

    public static Map<String, List<Question>> loadQuestions() {
        Map<String, List<Question>> questionsByDifficulty = new HashMap<>();

        // ---------------- Easy Questions (placeholders) ----------------
        List<Question> easySet = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            easySet.add(new Question(
                    i,
                    "Easy Question " + i,
                    "Buggy code for Easy question " + i,
                    "Hint for Easy question " + i,
                    "Easy",
                    String.valueOf(i) // expected answer = just the number
            ));
        }
        questionsByDifficulty.put("Easy", easySet);

        // ---------------- Medium Questions (real) ----------------
        List<Question> mediumSet = new ArrayList<>();

        mediumSet.add(new Question(
                1,
                "Missing Semicolon",
                "System.out.println(\"Hello World\")",
                "Don't forget that every Java statement must end with a semicolon!",
                "Medium",
                "System.out.println(\"Hello World\");"
        ));

        mediumSet.add(new Question(
                2,
                "Wrong Variable Name",
                "int num = 5;\nSystem.out.println(number);",
                "Make sure the variable name you print matches the one you declared.",
                "Medium",
                "int num = 5;\nSystem.out.println(num);"
        ));

        mediumSet.add(new Question(
                3,
                "Missing Colon",
                "if (x == 10)\n    System.out.println(\"Found!\");",
                "Some languages use a colon (:) after if-statements.",
                "Medium",
                "if (x == 10):\n    System.out.println(\"Found!\");"
        ));

        mediumSet.add(new Question(
                4,
                "Choose the Loop Type",
                "Which loop runs a specific number of times?\nA) while loop\nB) for loop\nC) do-while loop",
                "Think about which loop is designed for a fixed number of iterations.",
                "Medium",
                "B"
        ));

        mediumSet.add(new Question(
                5,
                "What Prints Here?",
                "int x = 3;\nint y = x * 2;\nSystem.out.println(y);\n\nWhat does this print?",
                "x = 3, y = x * 2 → 6",
                "Medium",
                "6"
        ));

        mediumSet.add(new Question(
                6,
                "Which is Correct?",
                "Which declares an integer variable?\nA) int x = 5;\nB) string x = 5;\nC) float x = 5;",
                "In Java, 'int' is the correct type for integers.",
                "Medium",
                "A"
        ));

        questionsByDifficulty.put("Medium", mediumSet);

        // ---------------- Hard Questions (real) ----------------
        List<Question> hardSet = new ArrayList<>();

        hardSet.add(new Question(
                1,
                "Fix off-by-one error in loop printing 1 to 10",
                "for (int i = 0; i < 10; i++) {\n    System.out.println(i);\n}",
                "Expected output: numbers 1 through 10 (inclusive).",
                "Hard",
                "for (int i = 1; i <= 10; i++) {\n    System.out.println(i);\n}"
        ));

        hardSet.add(new Question(
                2,
                "Fix NullPointerException when accessing string length",
                "String s = null;\nSystem.out.println(s.length());",
                "Hint: `s` must reference a valid String before calling `length()`.",
                "Hard",
                "String s = \"\";\nSystem.out.println(s.length());"
        ));

        hardSet.add(new Question(
                3,
                "Fix string comparison to check equality correctly",
                "String s = \"hello\";\nif (s == \"hello\") {\n    System.out.println(\"Match!\");\n}",
                "Hint: Use .equals() for string comparison in Java.",
                "Hard",
                "String s = \"hello\";\nif (s.equals(\"hello\")) {\n    System.out.println(\"Match!\");\n}"
        ));

        hardSet.add(new Question(
                4,
                "Fix integer division so result is 2.5",
                "int a = 5, b = 2;\nSystem.out.println(a / b);",
                "Hint: Cast one operand to double before dividing.",
                "Hard",
                "int a = 5, b = 2;\nSystem.out.println((double) a / b);"
        ));

        hardSet.add(new Question(
                5,
                "Fix array loop to avoid ArrayIndexOutOfBoundsException",
                "int[] nums = {1,2,3};\nfor (int i = 0; i <= nums.length; i++) {\n    System.out.println(nums[i]);\n}",
                "Hint: Use i < nums.length, not <=.",
                "Hard",
                "int[] nums = {1,2,3};\nfor (int i = 0; i < nums.length; i++) {\n    System.out.println(nums[i]);\n}"
        ));

        hardSet.add(new Question(
                6,
                "Fix immutable string bug so it prints HELLO",
                "String s = \"hello\";\ns.toUpperCase();\nSystem.out.println(s);",
                "Hint: Strings are immutable; assign the result back to s.",
                "Hard",
                "String s = \"hello\";\ns = s.toUpperCase();\nSystem.out.println(s);"
        ));

        hardSet.add(new Question(
                7,
                "Fix floating point equality check for 0.1 + 0.2",
                "if (0.1 + 0.2 == 0.3) {\n    System.out.println(\"Equal\");\n}",
                "Hint: Compare doubles using a tolerance (Math.abs difference).",
                "Hard",
                "if (Math.abs((0.1 + 0.2) - 0.3) < 1e-9) {\n    System.out.println(\"Equal\");\n}"
        ));

        hardSet.add(new Question(
                8,
                "Fix concurrency bug so counter increments are thread-safe",
                "class Counter {\n    private int count = 0;\n    public void increment() {\n        count++;\n    }\n}",
                "Hint: Use synchronized methods or AtomicInteger.",
                "Hard",
                "class Counter {\n    private int count = 0;\n    public synchronized void increment() {\n        count++;\n    }\n}"
        ));

        hardSet.add(new Question(
                9,
                "Fix file handling so file closes properly after reading",
                "BufferedReader br = new BufferedReader(new FileReader(\"data.txt\"));\nString line = br.readLine();\nSystem.out.println(line);\n// missing close",
                "Hint: Use try-with-resources to auto-close the reader.",
                "Hard",
                "try (BufferedReader br = new BufferedReader(new FileReader(\"data.txt\"))) {\n    String line = br.readLine();\n    System.out.println(line);\n} catch (IOException e) {\n    e.printStackTrace();\n}"
        ));

        questionsByDifficulty.put("Hard", hardSet);

        return questionsByDifficulty;
    }
}
