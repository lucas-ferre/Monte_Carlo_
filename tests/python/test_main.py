import math
import unittest

from main import PiApproximationSimulator


class PiApproximationSimulatorTests(unittest.TestCase):
    def test_rejects_non_positive_sample_size(self) -> None:
        simulator = PiApproximationSimulator(seed=1)

        for invalid_sample_size in (0, -1, -100):
            with self.subTest(sample_size=invalid_sample_size):
                with self.assertRaises(ValueError):
                    simulator.simulate(invalid_sample_size)

    def test_result_respects_simulation_invariants(self) -> None:
        sample_size = 1_000
        result = PiApproximationSimulator(seed=42).simulate(sample_size)

        self.assertEqual(result.total_points, sample_size)
        self.assertGreaterEqual(result.points_inside, 0)
        self.assertLessEqual(result.points_inside, sample_size)
        self.assertGreaterEqual(result.pi_estimate, 0.0)
        self.assertLessEqual(result.pi_estimate, 4.0)
        self.assertGreaterEqual(result.estimated_variance, 0.0)
        self.assertGreaterEqual(result.standard_error, 0.0)
        self.assertLessEqual(
            result.confidence_interval_95_lower,
            result.pi_estimate,
        )
        self.assertGreaterEqual(
            result.confidence_interval_95_upper,
            result.pi_estimate,
        )

        p_hat = result.points_inside / result.total_points
        expected_variance = 16.0 * p_hat * (1.0 - p_hat) / sample_size

        self.assertAlmostEqual(result.estimated_variance, expected_variance)
        self.assertAlmostEqual(result.standard_error, math.sqrt(expected_variance))
        self.assertAlmostEqual(
            result.confidence_interval_95_lower,
            result.pi_estimate - 1.96 * result.standard_error,
        )
        self.assertAlmostEqual(
            result.confidence_interval_95_upper,
            result.pi_estimate + 1.96 * result.standard_error,
        )

    def test_same_seed_produces_same_simulation_result(self) -> None:
        first_result = PiApproximationSimulator(seed=2026).simulate(5_000)
        second_result = PiApproximationSimulator(seed=2026).simulate(5_000)

        self.assertEqual(first_result.points_inside, second_result.points_inside)
        self.assertEqual(first_result.pi_estimate, second_result.pi_estimate)
        self.assertEqual(
            first_result.estimated_variance,
            second_result.estimated_variance,
        )
        self.assertEqual(first_result.standard_error, second_result.standard_error)
        self.assertEqual(
            first_result.confidence_interval_95_lower,
            second_result.confidence_interval_95_lower,
        )
        self.assertEqual(
            first_result.confidence_interval_95_upper,
            second_result.confidence_interval_95_upper,
        )

    def test_estimate_is_plausibly_close_to_pi(self) -> None:
        result = PiApproximationSimulator(seed=42).simulate(100_000)

        self.assertAlmostEqual(result.pi_estimate, math.pi, delta=0.03)


if __name__ == "__main__":
    unittest.main()
