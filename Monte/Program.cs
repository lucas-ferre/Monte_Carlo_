using System;
using System.Diagnostics;
using System.Globalization;
using System.Text;

// Modelagem de dados (estado imutável do resultado).
public readonly struct SimulationResult
{
    public double PiEstimate { get; }
    public double EstimatedVariance { get; }
    public double StandardError { get; }
    public double ConfidenceInterval95Lower { get; }
    public double ConfidenceInterval95Upper { get; }
    public int PointsInside { get; }
    public int TotalPoints { get; }
    public double ExecutionTimeMs { get; }

    public SimulationResult(
        double piEstimate,
        double estimatedVariance,
        double standardError,
        double confidenceInterval95Lower,
        double confidenceInterval95Upper,
        int pointsInside,
        int totalPoints,
        double executionTimeMs)
    {
        PiEstimate = piEstimate;
        EstimatedVariance = estimatedVariance;
        StandardError = standardError;
        ConfidenceInterval95Lower = confidenceInterval95Lower;
        ConfidenceInterval95Upper = confidenceInterval95Upper;
        PointsInside = pointsInside;
        TotalPoints = totalPoints;
        ExecutionTimeMs = executionTimeMs;
    }
}

// Contrato para implementações de simuladores de Monte Carlo.
public interface IMonteCarloSimulator
{
    SimulationResult Simulate(int sampleSize);
}

// Implementação da aproximação de pi por amostragem no quadrado [-1, 1]².
public sealed class PiApproximationSimulator : IMonteCarloSimulator
{
    private const double ZScore95 = 1.96;
    private readonly Random _random;

    public PiApproximationSimulator()
        : this(new Random())
    {
    }

    public PiApproximationSimulator(int seed)
        : this(new Random(seed))
    {
    }

    private PiApproximationSimulator(Random random)
    {
        _random = random;
    }

    public SimulationResult Simulate(int sampleSize)
    {
        if (sampleSize <= 0)
        {
            throw new ArgumentOutOfRangeException(
                nameof(sampleSize),
                sampleSize,
                "O tamanho da amostra deve ser um inteiro positivo.");
        }

        Stopwatch stopwatch = Stopwatch.StartNew();
        int pointsInside = 0;

        for (int i = 0; i < sampleSize; i++)
        {
            double x = (_random.NextDouble() * 2.0) - 1.0;
            double y = (_random.NextDouble() * 2.0) - 1.0;

            if ((x * x) + (y * y) <= 1.0)
            {
                pointsInside++;
            }
        }

        stopwatch.Stop();

        double pHat = (double)pointsInside / sampleSize;
        double piEstimate = pHat * 4.0;
        double estimatedVariance = 16.0 * pHat * (1.0 - pHat) / sampleSize;
        double standardError = Math.Sqrt(estimatedVariance);
        double marginOfError95 = ZScore95 * standardError;

        return new SimulationResult(
            piEstimate,
            estimatedVariance,
            standardError,
            piEstimate - marginOfError95,
            piEstimate + marginOfError95,
            pointsInside,
            sampleSize,
            stopwatch.Elapsed.TotalMilliseconds);
    }
}

internal static class Program
{
    private const int DefaultSampleSize = 10_000_000;

    private static int Main(string[] args)
    {
        Console.OutputEncoding = Encoding.UTF8;

        if (!TryParseArguments(args, out int sampleSize, out int? seed, out string? errorMessage))
        {
            Console.Error.WriteLine($"Erro: {errorMessage}");
            PrintUsage();
            return 2;
        }

        IMonteCarloSimulator simulator = seed.HasValue
            ? new PiApproximationSimulator(seed.Value)
            : new PiApproximationSimulator();

        Console.WriteLine($"Iniciando simulação de Monte Carlo com {sampleSize:E1} amostras...");
        if (seed.HasValue)
        {
            Console.WriteLine($"Semente: {seed.Value}");
        }

        SimulationResult result = simulator.Simulate(sampleSize);

        Console.WriteLine($"Pontos dentro do círculo: {result.PointsInside:N0}/{result.TotalPoints:N0}");
        Console.WriteLine($"Estimativa de pi: {result.PiEstimate:F6}");
        Console.WriteLine($"Variância estimada: {result.EstimatedVariance:E6}");
        Console.WriteLine($"Erro padrão: {result.StandardError:E6}");
        Console.WriteLine(
            $"Intervalo de confiança de 95%: " +
            $"[{result.ConfidenceInterval95Lower:F6}, {result.ConfidenceInterval95Upper:F6}]");
        Console.WriteLine($"Tempo de execução: {result.ExecutionTimeMs:F2} ms");

        return 0;
    }

    private static bool TryParseArguments(
        string[] args,
        out int sampleSize,
        out int? seed,
        out string? errorMessage)
    {
        sampleSize = DefaultSampleSize;
        seed = null;
        errorMessage = null;

        if (args.Length > 2)
        {
            errorMessage = "foram informados mais de dois argumentos.";
            return false;
        }

        if (args.Length >= 1 &&
            (!int.TryParse(args[0], NumberStyles.Integer, CultureInfo.InvariantCulture, out sampleSize) ||
             sampleSize <= 0))
        {
            errorMessage = "o tamanho da amostra deve ser um inteiro positivo.";
            return false;
        }

        if (args.Length == 2)
        {
            if (!int.TryParse(args[1], NumberStyles.Integer, CultureInfo.InvariantCulture, out int parsedSeed))
            {
                errorMessage = "a semente deve ser um inteiro de 32 bits.";
                return false;
            }

            seed = parsedSeed;
        }

        return true;
    }

    private static void PrintUsage()
    {
        Console.Error.WriteLine("Uso: dotnet run --project Monte -- [tamanho-da-amostra] [semente]");
        Console.Error.WriteLine($"Exemplo: dotnet run --project Monte -- 1000000 42");
        Console.Error.WriteLine($"Padrão: {DefaultSampleSize} amostras e semente aleatória.");
    }
}
