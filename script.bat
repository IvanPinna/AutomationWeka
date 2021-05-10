echo off

javac -cp weka.jar WekaApplication.java
java -cp weka.jar;. WekaApplication -classifier weka.classifiers.trees.J48 -M 2 -C 0.75 -classifier weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K "weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007" -calibrator "weka.classifiers.functions.Logistic -R 1.0E-8 -M -1 -num-decimal-places 4"             -classifier weka.classifiers.functions.VotedPerceptron -I 1 -E 1.0 -S 1 -M 10000           -classifier weka.classifiers.rules.DecisionTable -X 1 -S "weka.attributeSelection.BestFirst -D 1 -N 5" -exptype classification -splittype crossvalidation -runs 3 -folds 10 -result results.arff -t .\get-drink-set.arff -t .\go-to-bed-set.arff -t .\leave-house-set.arff -xml experimento.xml   

pause