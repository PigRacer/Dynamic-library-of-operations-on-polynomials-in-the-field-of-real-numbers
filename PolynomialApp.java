import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class PolynomialApp {
    private PolynomialLibrary library;
    private JTextField inputField1;
    private JTextField inputField2;
    private JTextField valueField; // Поле для ввода значения переменной
    private JLabel resultLabel;
    private JComboBox<String> operationComboBox; // Выпадающий список для выбора операции
    private static Logger logger;
    private String lastResult; // Переменная для хранения последнего результата

    public PolynomialApp() {
        library = new PolynomialLibrary();
        setupLogger();
        createUI();
    }

    private void setupLogger() {
        try {
            logger = Logger.getLogger("PolynomialAppLog");
            FileHandler fh = new FileHandler("PolynomialApp.log", true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createUI() {
        JFrame frame = new JFrame("Polynomial Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridLayout(6, 2));

        String[] operations = {"Select Operation", "Add", "Subtract", "Multiply", "Divide", "Evaluate"};
        operationComboBox = new JComboBox<>(operations);
        operationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleOperationSelection();
            }
        });

        inputField1 = new JTextField(20);
        inputField2 = new JTextField(20);
        valueField = new JTextField(10);
        resultLabel = new JLabel("Result: ");
        JButton calculateButton = new JButton("Calculate");
        JButton saveButton = new JButton("Save Result");

        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCalculate();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveResultToFile(lastResult);
            }
        });

        frame.add(new JLabel("Select Operation: "));
        frame.add(operationComboBox);
        frame.add(new JLabel("Polynomial 1: "));
        frame.add(inputField1);
        frame.add(new JLabel("Polynomial 2 / Value: "));
        frame.add(inputField2);
        frame.add(new JLabel("Value for evaluation: "));
        frame.add(valueField);
        frame.add(calculateButton);
        frame.add(resultLabel);
        frame.add(saveButton);

        inputField2.setVisible(false);
        valueField.setVisible(false);

        frame.setVisible(true);
    }

    private void handleOperationSelection() {
        String selectedOperation = (String) operationComboBox.getSelectedItem();
        inputField2.setVisible(true);
        valueField.setVisible(false);

        switch (selectedOperation) {
            case "Add":
            case "Subtract":
            case "Multiply":
            case "Divide":
                valueField.setVisible(false);
                break;
            case "Evaluate":
                inputField2.setVisible(false);
                valueField.setVisible(true);
                break;
            default:
                inputField2.setVisible(false);
                valueField.setVisible(false);
                break;
        }
        inputField1.revalidate();
        inputField2.revalidate();
        valueField.revalidate();
    }

    private Map<Integer, Double> parsePolynomial(String input) {
        try {
            return library.parse(input);
        } catch (Exception e) {
            logger.warning("Parsing error: " + e.getMessage());
            return null; // Возвращаем null в случае ошибки парсинга
        }
    }

    private void handleCalculate() {
        String selectedOperation = (String) operationComboBox.getSelectedItem();
        Map<Integer, Double> p1 = parsePolynomial(inputField1.getText());
        Map<Integer, Double> result = null;

        if (p1 == null) {
            resultLabel.setText("Invalid polynomial input for Polynomial 1.");
            logger.warning("Invalid polynomial input for Polynomial 1: " + inputField1.getText());
            return;
        }

        switch (selectedOperation) {
            case "Add":
                result = library.add(p1, parsePolynomial(inputField2.getText()));
                if (result == null) {
                    resultLabel.setText("Invalid polynomial input for Polynomial 2.");
                    logger.warning("Invalid polynomial input for Polynomial 2: " + inputField2.getText());
                    return;
                }
                break;
            case "Subtract":
                result = library.subtract(p1, parsePolynomial(inputField2.getText()));
                if (result == null) {
                    resultLabel.setText("Invalid polynomial input for Polynomial 2.");
                    logger.warning("Invalid polynomial input for Polynomial 2: " + inputField2.getText());
                    return;
                }
                break;
            case "Multiply":
                result = library.multiply(p1, parsePolynomial(inputField2.getText()));
                if (result == null) {
                    resultLabel.setText("Invalid polynomial input for Polynomial 2.");
                    logger.warning("Invalid polynomial input for Polynomial 2: " + inputField2.getText());
                    return;
                }
                break;
            case "Divide":
                PolynomialLibrary.DivisionResult divisionResult = library.divide(p1, parsePolynomial(inputField2.getText()));
                if (divisionResult == null) {
                    resultLabel.setText("Invalid polynomial input for Polynomial 2.");
                    logger.warning("Invalid polynomial input for Polynomial 2: " + inputField2.getText());
                    return;
                }
                if (!divisionResult.remainder.isEmpty()) {
                    resultLabel.setText("Polynomials do not divide evenly. Remainder: " + formatPolynomial(divisionResult.remainder));
                } else {
                    resultLabel.setText("Result: " + formatPolynomial(divisionResult.quotient));
                }
                lastResult = "Result: " + formatPolynomial(divisionResult.quotient);
                return;
            case "Evaluate":
                try {
                    double value = Double.parseDouble(valueField.getText());
                    double evaluationResult = library.evaluate(p1, value);
                    resultLabel.setText("Result: " + evaluationResult);
                    lastResult = "Result: " + evaluationResult;
                    return;
                } catch (NumberFormatException e) {
                    resultLabel.setText("Invalid input for value.");
                    logger.warning("Invalid input for value: " + valueField.getText());
                    return;
                }
            default:
                return;
        }

        resultLabel.setText("Result: " + formatPolynomial(result));
        lastResult = "Result: " + formatPolynomial(result);
    }

    private String formatPolynomial(Map<Integer, Double> polynomial) {
        StringBuilder sb = new StringBuilder();
        polynomial.entrySet().stream()
                .sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
                .forEach(entry -> {
                    int exponent = entry.getKey();
                    double coefficient = entry.getValue();

                    if (coefficient == 0) return;

                    if (sb.length() > 0) {
                        sb.append(coefficient > 0 ? " + " : " ");
                    }

                    if (exponent == 0) {
                        sb.append(coefficient);
                    } else if (exponent == 1) {
                        sb.append(coefficient == 1 ? "x" : coefficient + "x");
                    } else {
                        sb.append(coefficient == 1 ? "x^" + exponent : coefficient + "x^" + exponent);
                    }
                });
        return sb.toString();
    }

    private void saveResultToFile(String result) {
        if (result == null || result.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No result to save.");
            return;
        }
        try (FileWriter writer = new FileWriter("PolynomialResults.txt", true)) {
            writer.write(result + System.lineSeparator());
            JOptionPane.showMessageDialog(null, "Result saved to file.");
        } catch (IOException e) {
            logger.warning("Error writing to file: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error saving result to file.");
        }
    }

    public static void main(String[] args) {
        new PolynomialApp();
    }
}
