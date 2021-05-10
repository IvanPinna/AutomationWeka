echo off

java -cp weka.jar;. weka.experiment.PairedCorrectedTTester -t results.arff -G 1 -D 4,5,6 -R 2 -S 0.05 -c 40 -b 1

pause