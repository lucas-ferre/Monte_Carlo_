import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public final class MonteCarloTests {
    private static int testsRun = 0;

    private MonteCarloTests() {
    }

    public static void main(String[] args) {
        runTest("rejeita tamanhos de amostra inválidos", MonteCarloTests::rejectsInvalidSampleSizes);
        runTest("preserva as invariantes da simulação", MonteCarloTests::preservesSimulationInvariants);
        runTest("repete o resultado com a mesma semente", MonteCarloTests::isReproducibleWithSeed);
        runTest("produz uma aproximação plausível de pi", MonteCarloTests::producesPlausibleApproximation);
        runTest("rejeita argumentos de linha de comando inválidos", MonteCarloTests::rejectsInvalidCliArguments);

        System.out.printf("Todos os %d testes passaram.%n", testsRun);
    }

    private static void rejectsInvalidSampleSizes() {
        PiApproximationSimulator simulator = new PiApproximationSimulator(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> simulator.simulate(0),
                "Amostra igual a zero deveria ser rejeitada.");
        assertThrows(
                IllegalArgumentException.class,
                () -> simulator.simulate(-1),
                "Amostra negativa deveria ser rejeitada.");
    }

    private static void preservesSimulationInvariants() {
        int sampleSize = 10_000;
        SimulationResult result = new PiApproximationSimulator(12345L).simulate(sampleSize);

        assertEquals(sampleSize, result.totalPoints, "O total de pontos deve preservar a entrada.");
        assertTrue(result.pointsInside >= 0, "O número de pontos internos não pode ser negativo.");
        assertTrue(result.pointsInside <= sampleSize, "Pontos internos não podem exceder o total.");
        assertTrue(result.piEstimate >= 0.0 && result.piEstimate <= 4.0,
                "A estimativa de pi deve estar entre zero e quatro.");
        assertTrue(result.estimatedVariance >= 0.0, "A variância estimada não pode ser negativa.");
        assertDoubleEquals(Math.sqrt(result.estimatedVariance), result.standardError, 0.0,
                "O erro padrão deve ser a raiz da variância estimada.");
        assertDoubleEquals(
                result.piEstimate - 1.96 * result.standardError,
                result.confidenceInterval95Lower,
                1.0e-15,
                "O limite inferior do intervalo de confiança está incorreto.");
        assertDoubleEquals(
                result.piEstimate + 1.96 * result.standardError,
                result.confidenceInterval95Upper,
                1.0e-15,
                "O limite superior do intervalo de confiança está incorreto.");
        assertTrue(result.confidenceInterval95Lower <= result.piEstimate,
                "O intervalo deve conter a estimativa pontual.");
        assertTrue(result.piEstimate <= result.confidenceInterval95Upper,
                "O intervalo deve conter a estimativa pontual.");
        assertTrue(result.executionTimeMs >= 0.0, "O tempo de execução não pode ser negativo.");
    }

    private static void isReproducibleWithSeed() {
        int sampleSize = 50_000;
        SimulationResult first = new PiApproximationSimulator(987654321L).simulate(sampleSize);
        SimulationResult second = new PiApproximationSimulator(987654321L).simulate(sampleSize);

        assertEquals(first.pointsInside, second.pointsInside,
                "A mesma semente deve gerar a mesma contagem de pontos.");
        assertDoubleEquals(first.piEstimate, second.piEstimate, 0.0,
                "A mesma semente deve gerar a mesma estimativa.");
        assertDoubleEquals(first.estimatedVariance, second.estimatedVariance, 0.0,
                "A mesma semente deve gerar a mesma variância estimada.");
        assertDoubleEquals(first.standardError, second.standardError, 0.0,
                "A mesma semente deve gerar o mesmo erro padrão.");
        assertDoubleEquals(first.confidenceInterval95Lower, second.confidenceInterval95Lower, 0.0,
                "A mesma semente deve gerar o mesmo limite inferior.");
        assertDoubleEquals(first.confidenceInterval95Upper, second.confidenceInterval95Upper, 0.0,
                "A mesma semente deve gerar o mesmo limite superior.");
    }

    private static void producesPlausibleApproximation() {
        SimulationResult result = new PiApproximationSimulator(20260812L).simulate(500_000);
        double absoluteError = Math.abs(result.piEstimate - Math.PI);

        assertTrue(absoluteError < 0.02,
                "A aproximação deveria ficar a menos de 0,02 de Math.PI; erro obtido: " + absoluteError);
    }

    private static void rejectsInvalidCliArguments() {
        assertCliError(new String[]{"abc"}, "tamanho da amostra");
        assertCliError(new String[]{"0"}, "inteiro positivo");
        assertCliError(new String[]{"100", "semente-inválida"}, "semente");
        assertCliError(new String[]{"100", "1", "extra"}, "argumentos demais");
    }

    private static void assertCliError(String[] args, String expectedMessagePart) {
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();

        try (PrintStream out = new PrintStream(standardOutput, true, StandardCharsets.UTF_8);
             PrintStream err = new PrintStream(errorOutput, true, StandardCharsets.UTF_8)) {
            int exitCode = Main.run(args, out, err);

            assertEquals(2, exitCode, "Argumentos inválidos devem retornar o código de saída 2.");
        }

        String errorMessage = errorOutput.toString(StandardCharsets.UTF_8);
        assertTrue(errorMessage.contains(expectedMessagePart),
                "A mensagem de erro deveria conter: " + expectedMessagePart);
        assertTrue(errorMessage.contains("Uso: java Main"),
                "Argumentos inválidos devem exibir instruções de uso.");
    }

    private static void runTest(String name, Runnable test) {
        try {
            test.run();
            testsRun++;
            System.out.println("[OK] " + name);
        } catch (Throwable failure) {
            System.err.println("[FALHOU] " + name);
            failure.printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " Esperado: " + expected + "; obtido: " + actual + ".");
        }
    }

    private static void assertDoubleEquals(double expected, double actual, double tolerance, String message) {
        if (Double.isNaN(actual) || Math.abs(expected - actual) > tolerance) {
            throw new AssertionError(message + " Esperado: " + expected + "; obtido: " + actual + ".");
        }
    }

    private static <T extends Throwable> void assertThrows(
            Class<T> expectedType,
            Runnable action,
            String message
    ) {
        try {
            action.run();
        } catch (Throwable failure) {
            if (expectedType.isInstance(failure)) {
                return;
            }

            throw new AssertionError(
                    message + " Exceção esperada: " + expectedType.getName()
                            + "; obtida: " + failure.getClass().getName() + ".",
                    failure);
        }

        throw new AssertionError(message + " Nenhuma exceção foi lançada.");
    }
}
