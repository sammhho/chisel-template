.PHONY: all v test

all:

v:
	sbt 'runMain dsp.DSPX2'

test:
	sbt 'testOnly dsp.DSPX2Tester -- -z Basic'
	#sbt 'test:runMain dsp.GCDMain --generate-vcd-output on'
