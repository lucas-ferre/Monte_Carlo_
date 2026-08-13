import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Random;

// Modelagem de dados (estado)
final class SimulationResult {
    public final double piEstimate;
    public final double estimatedVariance;
    public final double standardError;
    public final double confidenceInterval95Lower;
    public final double confidenceInterval95Upper;
    public final int pointsInside;
    public final int totalPoints;
    public final double executionTimeMs;

    public SimulationResult(
            double piEstimate,
            double estimatedVariance,
            double standardError,
            double confidenceInterval95Lower,
            double confidenceInterval95Upper,
            int pointsInside,
            int totalPoints,
            double executionTimeMs
    ) {
        this.piEstimate = piEstimate;
        this.estimatedVariance = estimatedVariance;
        this.standardError = standardError;
        this.confidenceInterval95Lower = confidenceInterval95Lower;
        this.confidenceInterval95Upper = confidenceInterval95Upper;
        this.pointsInside = pointsInside;
        this.totalPoints = totalPoints;
        this.executionTimeMs = executionTimeMs;
    }
}

// Contrato da simulação
interface MonteCarloSimulator {
    SimulationResult simulate(int sampleSize);
}

// Implementação do método de Monte Carlo para aproximar pi
class PiApproximationSimulator implements MonteCarloSimulator {
    private static final double NORMAL_95_PERCENT_CRITICAL_VALUE = 1.96;

    private final Random random;

    public PiApproximationSimulator() {
        this.random = new Random();
    }

    public PiApproximationSimulator(long seed) {
        this.random = new Random(seed);
    }

    @Override
    public SimulationResult simulate(int sampleSize) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("O tamanho da amostra deve ser um inteiro positivo.");
        }

        long startTime = System.nanoTime();
        int pointsInside = 0;

        for (int i = 0; i < sampleSize; i++) {
            double x = (random.nextDouble() * 2.0) - 1.0;
            double y = (random.nextDouble() * 2.0) - 1.0;

            if (x * x + y * y <= 1.0) {
                pointsInside++;
            }
        }

        double executionTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;
        double pHat = (double) pointsInside / sampleSize;
        double piEstimate = 4.0 * pHat;
        double estimatedVariance = 16.0 * pHat * (1.0 - pHat) / sampleSize;
        double standardError = Math.sqrt(estimatedVariance);
        double marginOfError95 = NORMAL_95_PERCENT_CRITICAL_VALUE * standardError;

        return new SimulationResult(
                piEstimate,
                estimatedVariance,
                standardError,
                piEstimate - marginOfError95,
                piEstimate + marginOfError95,
                pointsInside,
                sampleSize,
                executionTimeMs
        );
    }
}

public class Main {
    private static final int DEFAULT_SAMPLE_SIZE = 10_000_000;
    private static final int INVALID_ARGUMENT_EXIT_CODE = 2;

    public static void main(String[] args) {
        configureUtf8Console();
        int exitCode = run(args, System.out, System.err);

        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length > 2) {
            printArgumentError(err, "foram informados argumentos demais.");
            return INVALID_ARGUMENT_EXIT_CODE;
        }

        int sampleSize = DEFAULT_SAMPLE_SIZE;
        Long seed = null;

        if (args.length >= 1) {
            try {
                sampleSize = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                printArgumentError(err, "o tamanho da amostra deve ser um inteiro positivo válido.");
                return INVALID_ARGUMENT_EXIT_CODE;
            }

            if (sampleSize <= 0) {
                printArgumentError(err, "o tamanho da amostra deve ser um inteiro positivo.");
                return INVALID_ARGUMENT_EXIT_CODE;
            }
        }

        if (args.length == 2) {
            try {
                seed = Long.parseLong(args[1]);
            } catch (NumberFormatException exception) {
                printArgumentError(err, "a semente deve ser um inteiro válido de 64 bits.");
                return INVALID_ARGUMENT_EXIT_CODE;
            }
        }

        MonteCarloSimulator simulator = seed == null
                ? new PiApproximationSimulator()
                : new PiApproximationSimulator(seed);

        if (seed == null) {
            out.printf(Locale.ROOT,
                    "Iniciando simulação de Monte Carlo com %,d amostras...%n",
                    sampleSize);
        } else {
            out.printf(Locale.ROOT,
                    "Iniciando simulação de Monte Carlo com %,d amostras (semente: %d)...%n",
                    sampleSize,
                    seed);
        }

        SimulationResult result = simulator.simulate(sampleSize);

        out.printf(Locale.ROOT, "Estimativa de pi: %.6f%n", result.piEstimate);
        out.printf(Locale.ROOT, "Variância estimada: %.6e%n", result.estimatedVariance);
        out.printf(Locale.ROOT, "Erro padrão: %.6e%n", result.standardError);
        out.printf(Locale.ROOT,
                "Intervalo de confiança de 95%%: [%.6f, %.6f]%n",
                result.confidenceInterval95Lower,
                result.confidenceInterval95Upper);
        out.printf(Locale.ROOT,
                "Pontos dentro do círculo: %,d de %,d%n",
                result.pointsInside,
                result.totalPoints);
        out.printf(Locale.ROOT, "Tempo de execução: %.2f ms%n", result.executionTimeMs);
        return 0;
    }

    private static void printArgumentError(PrintStream err, String message) {
        err.println("Erro: " + message);
        err.println("Uso: java Main [tamanho-da-amostra] [semente]");
        err.println("  tamanho-da-amostra: inteiro positivo (padrão: 10000000)");
        err.println("  semente: inteiro opcional de 64 bits para execuções reproduzíveis");
    }

    private static void configureUtf8Console() {
        System.setOut(new PrintStream(
                new FileOutputStream(FileDescriptor.out),
                true,
                StandardCharsets.UTF_8));
        System.setErr(new PrintStream(
                new FileOutputStream(FileDescriptor.err),
                true,
                StandardCharsets.UTF_8));
    }
}
