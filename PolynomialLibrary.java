import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolynomialLibrary {

    public Map<Integer, Double> parse(String input) throws Exception {
        // Удаляем лишние пробелы
        input = input.replaceAll("\\s+", "");

        // Проверка на наличие некорректных символов
        if (!input.matches("^[+-]?((\\d*\\.?\\d*)x\\^?\\d*|[+-]?\\d+\\.?\\d*|x|[+-]?x\\^?\\d*)*$")) {
            throw new Exception("Invalid polynomial format: contains invalid characters.");
        }

        Map<Integer, Double> coefficients = new HashMap<>();
        String regex = "([+-]?\\d*\\.?\\d*)x\\^?(\\d*)|([+-]?\\d+\\.?\\d*)|([+-]?x\\^?\\d*)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            // Обработка членов с x
            if (matcher.group(1) != null) {
                String coefficientStr = matcher.group(1);
                String exponentStr = matcher.group(2);
                double coefficient = coefficientStr.isEmpty() || coefficientStr.equals("+") ? 1 :
                        coefficientStr.equals("-") ? -1 : Double.parseDouble(coefficientStr);
                int exponent = exponentStr.isEmpty() ? 1 : Integer.parseInt(exponentStr);
                coefficients.merge(exponent, coefficient, Double::sum);
            }
            // Обработка свободных членов
            else if (matcher.group(3) != null) {
                String freeTermStr = matcher.group(3);
                double freeTermCoefficient = Double.parseDouble(freeTermStr);
                coefficients.merge(0, freeTermCoefficient, Double::sum);
            }
            // Обработка случаев с x без коэффициента
            else if (matcher.group(4) != null) {
                String xTermStr = matcher.group(4);
                double coefficient = xTermStr.equals("x") || xTermStr.equals("+x") ? 1 : -1;
                int exponent = xTermStr.contains("^") ? Integer.parseInt(xTermStr.split("\\^")[1]) : 1;
                coefficients.merge(exponent, coefficient, Double::sum);
            }
        }

        // Проверка на наличие хотя бы одного коэффициента
        if (coefficients.isEmpty()) {
            throw new Exception("Invalid polynomial format: no valid terms found.");
        }

        return coefficients;
    }

    public Map<Integer, Double> add(Map<Integer, Double> p1, Map<Integer, Double> p2) {
        if (p2 == null) return null; // Проверка на null
        Map<Integer, Double> result = new HashMap<>(p1);
        p2.forEach((k, v) -> result.merge(k, v, Double::sum));
        result.entrySet().removeIf(entry -> entry.getValue() == 0);
        return result;
    }

    public Map<Integer, Double> subtract(Map<Integer, Double> p1, Map<Integer, Double> p2) {
        if (p2 == null) return null; // Проверка на null
        Map<Integer, Double> result = new HashMap<>(p1);
        p2.forEach((k, v) -> result.merge(k, -v, Double::sum));
        result.entrySet().removeIf(entry -> entry.getValue() == 0);
        return result;
    }

    public Map<Integer, Double> multiply(Map<Integer, Double> p1, Map<Integer, Double> p2) {
        if (p2 == null) return null; // Проверка на null
        Map<Integer, Double> result = new HashMap<>();
        for (Map.Entry<Integer, Double> entry1 : p1.entrySet()) {
            for (Map.Entry<Integer, Double> entry2 : p2.entrySet()) {
                int newKey = entry1.getKey() + entry2.getKey();
                double newValue = entry1.getValue() * entry2.getValue();
                result.merge(newKey, newValue, Double::sum);
            }
        }
        return result;
    }
    public class DivisionResult {
        public Map<Integer, Double> quotient;
        public Map<Integer, Double> remainder;

        public DivisionResult(Map<Integer, Double> quotient, Map<Integer, Double> remainder) {
            this.quotient = quotient;
            this.remainder = remainder;
        }
    }

    public DivisionResult divide(Map<Integer, Double> p1, Map<Integer, Double> p2) {
        // Проверка на деление на ноль
        if (p2 == null || p2.isEmpty() || !p2.containsKey(getDegree(p2)) || getDegree(p2) == 0 && p2.get(0) == 0) {
            return null; // Возвращаем null, если p2 является нулевым полином
        }

        Map<Integer, Double> quotient = new HashMap<>();
        Map<Integer, Double> remainder = new HashMap<>(p1);
        int degreeD = getDegree(p2);
        double leadingCoefficientD = p2.get(degreeD);

        while (!remainder.isEmpty() && getDegree(remainder) >= degreeD) {
            int degreeR = getDegree(remainder);
            double leadingCoefficientR = remainder.get(degreeR);
            int newDegree = degreeR - degreeD;
            double newCoefficient = leadingCoefficientR / leadingCoefficientD;

            quotient.put(newDegree, quotient.getOrDefault(newDegree, 0.0) + newCoefficient);
            Map<Integer, Double> term = new HashMap<>();
            term.put(newDegree, newCoefficient);
            Map<Integer, Double> product = multiply(term, p2);
            remainder = subtract(remainder, product);
        }

        return new DivisionResult(quotient, remainder);
    }



    private int getDegree(Map<Integer, Double> polynomial) {
        return polynomial.keySet().stream().max(Integer::compareTo).orElse(0);
    }

    public double evaluate(Map<Integer, Double> polynomial, double value) {
        double result = 0.0;
        for (Map.Entry<Integer, Double> entry : polynomial.entrySet()) {
            result += entry.getValue() * Math.pow(value, entry.getKey());
        }
        return result;
    }
}