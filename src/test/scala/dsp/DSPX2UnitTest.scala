package dsp

import java.io.File

import chisel3.iotesters
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

class DSPX2UnitTester(c: DSPX2Wrapper) extends PeekPokeTester(c) {
    for (i <- 1 to 10 by 1) {
        for (j <- 1 to 10 by 1) {
            for (k <- 1 to 10 by 1) {
                poke(c.io.a0, i)
                poke(c.io.b0, j)
                poke(c.io.c0, k)
                poke(c.io.a1, i*2)
                poke(c.io.b1, j*3)
                poke(c.io.c1, k*5)
                step(4)
                val expected_q0 = i * j + k
                val expected_q1 = (i*2) * (j*3) + (k*5)
                expect(c.io.q0, expected_q0)
                expect(c.io.q1, expected_q1)
            }
        }
    }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly gcd.GCDTester
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly gcd.GCDTester'
  * }}}
  */
class DSPX2Tester extends ChiselFlatSpec {
  // Disable this until we fix isCommandAvailable to swallow stderr along with stdout
  private val backendNames = if(firrtl.FileUtils.isCommandAvailable(Seq("verilator", "--version"))) {
    Array("firrtl", "verilator")
  }
  else {
    Array("firrtl")
  }
  for ( backendName <- backendNames ) {
    "DSPX2" should s"calculate proper multiply-add (with $backendName)" in {
      Driver(() => new DSPX2Wrapper, backendName) {
        c => new DSPX2UnitTester(c)
      } should be (true)
    }
  }

  "Basic test using Driver.execute" should "be used as an alternative way to run specification" in {
    iotesters.Driver.execute(Array(), () => new DSPX2Wrapper) {
      c => new DSPX2UnitTester(c)
    } should be (true)
  }

  if(backendNames.contains("verilator")) {
    "using --backend-name verilator" should "be an alternative way to run using verilator" in {
      iotesters.Driver.execute(Array("--backend-name", "verilator"), () => new DSPX2Wrapper) {
        c => new DSPX2UnitTester(c)
      } should be(true)
    }
  }

  "running with --is-verbose" should "show more about what's going on in your tester" in {
    iotesters.Driver.execute(Array("--is-verbose"), () => new DSPX2Wrapper) {
      c => new DSPX2UnitTester(c)
    } should be(true)
  }

  /**
    * By default verilator backend produces vcd file, and firrtl and treadle backends do not.
    * Following examples show you how to turn on vcd for firrtl and treadle and how to turn it off for verilator
    */

  "running with --generate-vcd-output on" should "create a vcd file from your test" in {
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "on", "--target-dir", "test_run_dir/make_a_vcd", "--top-name", "make_a_vcd"),
      () => new DSPX2Wrapper
    ) {

      c => new DSPX2UnitTester(c)
    } should be(true)

    new File("test_run_dir/make_a_vcd/make_a_vcd.vcd").exists should be (true)
  }

  "running with --generate-vcd-output off" should "not create a vcd file from your test" in {
    iotesters.Driver.execute(
      Array("--generate-vcd-output", "off", "--target-dir", "test_run_dir/make_no_vcd", "--top-name", "make_no_vcd",
      "--backend-name", "verilator"),
      () => new DSPX2Wrapper
    ) {

      c => new DSPX2UnitTester(c)
    } should be(true)

    new File("test_run_dir/make_no_vcd/make_a_vcd.vcd").exists should be (false)

  }

}
