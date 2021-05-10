echo off

javac -cp weka.jar WekaApplication.java
java -cp weka.jar;. WekaApplication -classifier weka.classifiers.trees.J48 -M 3 -C 0.5  -exptype classification -splittype crossvalidation -runs 3 -folds 10 -result results.arff -t .\get-drink-set.arff -t .\go-to-bed-set.arff -t .\leave-house-set.arff -xml experimento.xml   

pause