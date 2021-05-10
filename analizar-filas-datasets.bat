echo off

java -cp weka.jar;. weka.experiment.PairedCorrectedTTester -t results.arff -G 4,5,6 -D 1 -R 2 -S 0.05 -c 40 -b 1

pause