import argparse
import math
import random
import sys
import time
from abc import ABC, abstractmethod
from dataclasses import dataclass


DEFAULT_SAMPLE_SIZE = 10_000_000
CONFIDENCE_LEVEL_95_Z_SCORE = 1.96


@dataclass(frozen=True)
class SimulationResult:
    """Resultado imutável de uma simulação de Monte Carlo."""

    pi_estimate: float
    estimated_variance: float
    standard_error: float
    confidence_interval_95_lower: float
    confidence_interval_95_upper: float
    points_inside: int
    total_points: int
    execution_time_ms: float


class MonteCarloSimulator(ABC):
    """Contrato para simuladores de Monte Carlo."""

    @abstractmethod
    def simulate(self, sample_size: int) -> SimulationResult:
        """Executa a simulação para a quantidade de amostras informada."""
        raise NotImplementedError


class PiApproximationSimulator(MonteCarloSimulator):
    """Aproxima Pi pela proporção de pontos dentro do círculo unitário."""

    def __init__(self, seed: int | None = None) -> None:
        self._random = random.Random(seed)

    def simulate(self, sample_size: int) -> SimulationResult:
        if sample_size <= 0:
            raise ValueError("O tamanho da amostra deve ser um inteiro positivo.")

        start_time = time.perf_counter()
        points_inside = 0

        for _ in range(sample_size):
            x = self._random.uniform(-1.0, 1.0)
            y = self._random.uniform(-1.0, 1.0)

            if x * x + y * y <= 1.0:
                points_inside += 1

        execution_time_ms = (time.perf_counter() - start_time) * 1000

        p_hat = points_inside / sample_size
        pi_estimate = 4.0 * p_hat
        estimated_variance = 16.0 * p_hat * (1.0 - p_hat) / sample_size
        standard_error = math.sqrt(estimated_variance)
        confidence_margin = CONFIDENCE_LEVEL_95_Z_SCORE * standard_error

        return SimulationResult(
            pi_estimate=pi_estimate,
            estimated_variance=estimated_variance,
            standard_error=standard_error,
            confidence_interval_95_lower=pi_estimate - confidence_margin,
            confidence_interval_95_upper=pi_estimate + confidence_margin,
            points_inside=points_inside,
            total_points=sample_size,
            execution_time_ms=execution_time_ms,
        )


def positive_integer(value: str) -> int:
    """Converte um argumento da CLI em um inteiro estritamente positivo."""
    try:
        parsed_value = int(value)
    except ValueError as error:
        raise argparse.ArgumentTypeError("informe um número inteiro") from error

    if parsed_value <= 0:
        raise argparse.ArgumentTypeError("o valor deve ser maior que zero")

    return parsed_value


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Aproxima Pi com uma simulação pelo método de Monte Carlo."
    )
    parser.add_argument(
        "sample_size",
        nargs="?",
        type=positive_integer,
        default=DEFAULT_SAMPLE_SIZE,
        help=f"quantidade de pontos da simulação (padrão: {DEFAULT_SAMPLE_SIZE})",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=None,
        help="semente opcional para tornar a execução reprodutível",
    )
    return parser.parse_args()


def main() -> None:
    # Mantém os textos em português legíveis também quando a saída é redirecionada
    # em ambientes Windows cuja codificação padrão não é UTF-8.
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(encoding="utf-8")

    arguments = parse_arguments()
    simulator = PiApproximationSimulator(seed=arguments.seed)

    print(
        "Iniciando simulação de Monte Carlo com "
        f"{arguments.sample_size:,} amostras..."
    )
    if arguments.seed is not None:
        print(f"Semente aleatória: {arguments.seed}")

    result = simulator.simulate(sample_size=arguments.sample_size)

    print(
        "Pontos dentro do círculo: "
        f"{result.points_inside:,}/{result.total_points:,}"
    )
    print(f"Estimativa de Pi: {result.pi_estimate:.6f}")
    print(f"Variância estimada: {result.estimated_variance:.6e}")
    print(f"Erro padrão: {result.standard_error:.6e}")
    print(
        "Intervalo de confiança de 95%: "
        f"[{result.confidence_interval_95_lower:.6f}, "
        f"{result.confidence_interval_95_upper:.6f}]"
    )
    print(f"Tempo de execução: {result.execution_time_ms:.2f} ms")


if __name__ == "__main__":
    main()
