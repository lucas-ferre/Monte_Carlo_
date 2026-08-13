using System;
using System.Collections.Generic;

namespace MonteCarlo.Tests;

internal static class Program
{
    private static int Main()
    {
        var tests = new (string Name, Action Run)[]
        {
            ("rejeita tamanho de amostra inválido", RejectsInvalidSampleSize),
            ("preserva invariantes do resultado", PreservesResultInvariants),
            ("repete o resultado com a mesma semente", RepeatsResultWithSameSeed),
            ("produz aproximação plausível de pi", ProducesPlausiblePiApproximation),
        };

        var failures = new List<string>();

        foreach ((string name, Action run) in tests)
        {
            try
            {
                run();
                Console.WriteLine($"[OK] {name}");
            }
            catch (Exception exception)
            {
                failures.Add(name);
                Console.Error.WriteLine($"[FALHOU] {name}: {exception.Message}");
            }
        }

        Console.WriteLine($"{tests.Length - failures.Count}/{tests.Length} testes aprovados.");
        return failures.Count == 0 ? 0 : 1;
    }

    private static void RejectsInvalidSampleSize()
    {
        var simulator = new PiApproximationSimulator(seed: 42);
        AssertThrows<ArgumentOutOfRangeException>(() => simulator.Simulate(0));
        AssertThrows<ArgumentOutOfRangeException>(() => simulator.Simulate(-1));
    }

    private static void PreservesResultInvariants()
    {
        const int sampleSize = 10_000;
        SimulationResult result = new PiApproximationSimulator(seed: 42).Simulate(sampleSize);

        Assert(result.TotalPoints == sampleSize, "o total de pontos não corresponde à amostra");
        Assert(result.PointsInside >= 0, "a quantidade de pontos internos é negativa");
        Assert(result.PointsInside <= sampleSize, "há mais pontos internos do que pontos gerados");
        Assert(result.PiEstimate is >= 0.0 and <= 4.0, "a estimativa de pi está fora de [0, 4]");
        Assert(result.EstimatedVariance >= 0.0, "a variância estimada é negativa");
        Assert(result.StandardError >= 0.0, "o erro padrão é negativo");
        Assert(
            Math.Abs((result.StandardError * result.StandardError) - result.EstimatedVariance) < 1e-15,
            "o erro padrão não é a raiz da variância estimada");
        Assert(
            result.ConfidenceInterval95Lower <= result.PiEstimate &&
            result.PiEstimate <= result.ConfidenceInterval95Upper,
            "o intervalo de confiança não contém a estimativa");
        Assert(result.ExecutionTimeMs >= 0.0, "o tempo de execução é negativo");
    }

    private static void RepeatsResultWithSameSeed()
    {
        SimulationResult first = new PiApproximationSimulator(seed: 1234).Simulate(20_000);
        SimulationResult second = new PiApproximationSimulator(seed: 1234).Simulate(20_000);

        Assert(first.PointsInside == second.PointsInside, "a mesma semente gerou contagens diferentes");
        Assert(first.PiEstimate == second.PiEstimate, "a mesma semente gerou estimativas diferentes");
        Assert(
            first.EstimatedVariance == second.EstimatedVariance,
            "a mesma semente gerou variâncias diferentes");
    }

    private static void ProducesPlausiblePiApproximation()
    {
        SimulationResult result = new PiApproximationSimulator(seed: 42).Simulate(200_000);
        Assert(
            Math.Abs(result.PiEstimate - Math.PI) < 0.03,
            $"a estimativa {result.PiEstimate:F6} ficou fora da tolerância esperada");
    }

    private static void Assert(bool condition, string message)
    {
        if (!condition)
        {
            throw new InvalidOperationException(message);
        }
    }

    private static void AssertThrows<TException>(Action action)
        where TException : Exception
    {
        try
        {
            action();
        }
        catch (TException)
        {
            return;
        }

        throw new InvalidOperationException($"a exceção esperada {typeof(TException).Name} não foi lançada");
    }
}
