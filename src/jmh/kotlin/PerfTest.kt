package de.earley.simplyTyped.perftest

import de.earley.simplyTyped.NoDiagnostics
import de.earley.simplyTyped.processSource
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class PerfTest {
    @Benchmark
    fun fulltest(hole: Blackhole) {

        val source = """
            letrec add : Nat -> Nat -> Nat = λ n : Nat . λ m : Nat .
                if iszero n then m else succ (add (pred n) m)
            in
            letrec times : Nat -> Nat -> Nat = λ n : Nat . λ m : Nat .
                if iszero n then 0 else add m (times (pred n) m)
            in
            letrec factorial : Nat -> Nat = λ n : Nat .
             if iszero n then 1 else times n (factorial (pred n))
            in factorial 4
    """

        hole.consume(processSource(source, NoDiagnostics))
    }

}