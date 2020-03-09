package dsp

import chisel3._
import chisel3.experimental.withClockAndReset
import multiclock_demo.ClockDivider

class DSPX2 extends Module {
  val io = IO(new Bundle {
    val clock_x2 = Input(Clock())
    val reset_x2 = Input(Bool())
    val a0 = Input(SInt(18.W))
    val a1 = Input(SInt(18.W))
    val b0 = Input(SInt(18.W))
    val b1 = Input(SInt(18.W))
    val c0 = Input(SInt(36.W))
    val c1 = Input(SInt(36.W))
    val rlt0 = Output(SInt(37.W))
    val rlt1 = Output(SInt(37.W))
  })
  val r0_dly1 = RegInit(0.S(37.W))
  val r1_dly1 = RegInit(0.S(37.W))

  withClockAndReset(io.clock_x2, io.reset_x2) {
    val msel = RegInit(false.B)
    val mul = RegInit(0.S(36.W))
    val r0 = RegInit(0.S(37.W))
    val r1 = RegInit(0.S(37.W))

    msel := !msel
    val a = Mux(msel, io.a0, io.a1)
    val b = Mux(msel, io.b0, io.b1)
    val c = Mux(msel, io.c0, io.c1)
    val c_dly1 = RegNext(c)
    mul := a * b
    val add = mul +& c_dly1
    //val rlt = RegNext(add)
    when (!msel) {
      r0 := add
    }
    when (msel) {
      r1 := add
    }
    r0_dly1 := r0
    r1_dly1 := r1
  }

  io.rlt0 := r0_dly1
  io.rlt1 := r1_dly1
}

class DSPX2Wrapper extends Module {
  val io = IO(new Bundle {
    //val clock_x2 = Input(Clock())
    //val reset_x2 = Input(Bool())
    val a0 = Input(SInt(18.W))
    val a1 = Input(SInt(18.W))
    val b0 = Input(SInt(18.W))
    val b1 = Input(SInt(18.W))
    val c0 = Input(SInt(36.W))
    val c1 = Input(SInt(36.W))
    val rlt0 = Output(SInt(37.W))
    val rlt1 = Output(SInt(37.W))
  })

  val div2_clock = ClockDivider(clock, 2)
  val inst = withClockAndReset(clock = div2_clock, reset = reset) {
    Module(new DSPX2)
  }
  inst.io.clock_x2 := clock
  inst.io.reset_x2 := reset
  inst.io.a0 := io.a0
  inst.io.a1 := io.a1
  inst.io.b0 := io.b0
  inst.io.b1 := io.b1
  inst.io.c0 := io.c0
  inst.io.c1 := io.c1
  io.rlt0 := inst.io.rlt0
  io.rlt1 := inst.io.rlt1
}


//object DSPX2 extends App {
//  chisel3.Driver.execute(args, () => new DSPX2)
//  // Alternate version if there are no args
//  // chisel3.Driver.execute(Array[String](), () => new HelloWorld)
//}
