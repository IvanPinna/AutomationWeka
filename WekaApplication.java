
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.Vector;
import javax.swing.DefaultListModel;
import weka.classifiers.Classifier;
import weka.core.Utils;
import weka.experiment.ClassifierSplitEvaluator;
import weka.experiment.CrossValidationResultProducer;
import weka.experiment.Experiment;
import weka.experiment.InstancesResultListener;
import weka.experiment.PropertyNode;
import weka.experiment.RandomSplitResultProducer;
import weka.experiment.RegressionSplitEvaluator;
import weka.experiment.SplitEvaluator;
import weka.experiment.xml.XMLExperiment;

/**
 *
 * @author ivan
 */
public class WekaApplication {
	
    //Post: Escribe un fichero xml de nombre filename con la configuracion de exp. 
    public static void write(String filename, Experiment exp) throws Exception {

        // XML?
        if (filename.toLowerCase().endsWith(".xml")) {
            XMLExperiment xml = new XMLExperiment();
            xml.write(filename, exp);
        }
    }

    //Post: Devuelve el numero de ocurrencias de la cadena busqueda en args.
    public static Vector<Integer> ocurrencias(String[] cad, String busqueda) {
        //int res = 0;
		Vector<Integer> res = new Vector<Integer>();
		
        for (int i = 0; i < cad.length; ++i) {
            if (cad[i].equals(busqueda)) {
                //++res;
				res.add(res.size(), i); 
            }
        }
        return res;
    }

    //Post: Busca en args el nombre del xml a escribir, si no lo encuentra
    //devuelve default
    public static String nombre_xml(String[] cad) {
        String res = "default.xml"; //Nombre por defecto. 
        for (int i = 0; i < cad.length; ++i) {
            if (cad[i].equals("-xml")) {
                res = cad[i + 1];
            }
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(
                    "\nUsage: ExperimentDemo\n"
                    + "\t   -classifier <classifier incl. parameters> (can be supplied multiple times)\n"
                    + "\t   -exptype <classification|regression>\n"
                    + "\t   -splittype <crossvalidation|randomsplit>\n"
                    + "\t   -runs <# of runs>\n"
                    + "\t   -folds <folds for CV>\n"
                    + "\t   -percentage <percentage for randomsplit>\n"
                    + "\t   -result <ARFF file for storing the results>\n"
                    + "\t   -t dataset (can be supplied multiple times, p.e. iris.arff/breast.arff)\n"
                    + "\t   -xml name <file for store the experiment in xml>");
            System.exit(1);
        }

        // 1. setup the experiment
        System.out.println("Setting up...");
        Experiment exp = new Experiment();
        exp.setPropertyArray(new Classifier[0]);
        exp.setUsePropertyIterator(true);

        String option;

        // classification or regression
        option = Utils.getOption("exptype", args);
        if (option.length() == 0) {
            throw new IllegalArgumentException("No experiment type provided!");
        }

        SplitEvaluator se = null;
        Classifier sec = null;
        boolean classification = false;
        if (option.equals("classification")) {
            classification = true;
            se = new ClassifierSplitEvaluator();
            sec = ((ClassifierSplitEvaluator) se).getClassifier();
        } else if (option.equals("regression")) {
            se = new RegressionSplitEvaluator();
            sec = ((RegressionSplitEvaluator) se).getClassifier();
        } else {
            throw new IllegalArgumentException("Unknown experiment type '" + option + "'!");
        }

        // crossvalidation or randomsplit
        option = Utils.getOption("splittype", args);
        if (option.length() == 0) {
            throw new IllegalArgumentException("No split type provided!");
        }

        if (option.equals("crossvalidation")) {
            CrossValidationResultProducer cvrp = new CrossValidationResultProducer();
            option = Utils.getOption("folds", args);
            if (option.length() == 0) {
                throw new IllegalArgumentException("No folds provided!");
            }
            cvrp.setNumFolds(Integer.parseInt(option));
            cvrp.setSplitEvaluator(se);

            PropertyNode[] propertyPath = new PropertyNode[2];
            try {
                propertyPath[0] = new PropertyNode(
                        se,
                        new PropertyDescriptor("splitEvaluator",
                                CrossValidationResultProducer.class),
                        CrossValidationResultProducer.class);
                propertyPath[1] = new PropertyNode(
                        sec,
                        new PropertyDescriptor("classifier",
                                se.getClass()),
                        se.getClass());
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }

            exp.setResultProducer(cvrp);
            exp.setPropertyPath(propertyPath);

        } else if (option.equals("randomsplit")) {
            RandomSplitResultProducer rsrp = new RandomSplitResultProducer();
            rsrp.setRandomizeData(true);
            option = Utils.getOption("percentage", args);
            if (option.length() == 0) {
                throw new IllegalArgumentException("No percentage provided!");
            }
            rsrp.setTrainPercent(Double.parseDouble(option));
            rsrp.setSplitEvaluator(se);

            PropertyNode[] propertyPath = new PropertyNode[2];
            try {
                propertyPath[0] = new PropertyNode(
                        se,
                        new PropertyDescriptor("splitEvaluator",
                                RandomSplitResultProducer.class),
                        RandomSplitResultProducer.class);
                propertyPath[1] = new PropertyNode(
                        sec,
                        new PropertyDescriptor("classifier",
                                se.getClass()),
                        se.getClass());
            } catch (IntrospectionException e) {
                e.printStackTrace();
            }

            exp.setResultProducer(rsrp);
            exp.setPropertyPath(propertyPath);
        } else {
            throw new IllegalArgumentException("Unknown split type '" + option + "'!");
        }

        // runs
        option = Utils.getOption("runs", args);
        if (option.length() == 0) {
            throw new IllegalArgumentException("No runs provided!");
        }
        exp.setRunLower(1);
        exp.setRunUpper(Integer.parseInt(option));

        //Visionado de args
		for(int i = 0; i < args.length; ++i)
			System.out.println(i + "  " + args[i]);
        int j = 0;

        //Contar el numero de clasificadores;
        Vector<Integer> clasificadores = ocurrencias(args, "-classifier");
        
		/* *** TESTEO *** */
		for(int i = 0; i < clasificadores.size(); ++i)
			System.out.println(clasificadores.elementAt(i));
		/* *** FIN TESTEO *** */
		
		Classifier[] c = new Classifier[clasificadores.size()];

        for (int i = 0; i < clasificadores.size(); ++i) {
			//Asi si coge los parametros, es necesario pasarle la cadena de esta forma. 
            //option = "weka.classifiers.trees.J48 -M 1 -C 0.75"; //Utils.getOption("classifier", args); //No coge los parametros. 
			option = Utils.getOption("classifier", args);
			
			//Option solo tiene el nombre del clasificador, es necesario añadirle los demas parametros. 
			if(i == (clasificadores.size() -1)){  //Último clasficador, pivota sobre -result.
				Vector<Integer> v = ocurrencias(args, "-result");
				for(int cadena = clasificadores.elementAt(i)+2; cadena < v.elementAt(0); ++cadena)
					option = option + " " + args[cadena];
				System.out.println("Look -> " + option);
				
			}else{
				for(int cadena = clasificadores.elementAt(i)+2; cadena < clasificadores.elementAt(i+1); ++cadena)
					option = option + " " + args[cadena];
				System.out.println("Look -> " + option);
			}
			
			
            if (option.length() == 0) {
                throw new IllegalArgumentException("No classifier provided!");
            }
            String[] options = Utils.splitOptions(option); //Almacenarse las opciones del clasificador. 
            String classname = options[0];
            options[0] = ""; //Sino salta una excepcion
			
			//System.out.println(options[1]);
			/* Juraria que la lista de opciones deberia ser: 
			    options[0] = "-M";
				options[1] = "2";
				...
			*/	
			
            c[i] = (Classifier) Utils.forName(Classifier.class, classname, options);
            options[0] = "";
        }
        exp.setPropertyArray(c);
        
        //conjuntos de datos 
        //int datasets = ocurrencias(args, "-t");
		Vector<Integer> datasets = ocurrencias(args, "-t");
        boolean data = false;
        DefaultListModel model = new DefaultListModel();
        for(int i = 0; i < datasets.size(); ++i){
            data = true;  
            File file = new File(Utils.getOption("t", args));
            model.addElement(file);
        }

        if (!data) {
            throw new IllegalArgumentException("No data files provided!");
        }
        exp.setDatasets(model);

        //result
        option = Utils.getOption("result", args);
        if (option.length() == 0) {
            throw new IllegalArgumentException("No result file provided!");
        }
        InstancesResultListener irl = new InstancesResultListener();
        irl.setOutputFile(new File(option));
        exp.setResultListener(irl);

        // 2. run experiment
        System.out.println("Initializing...");
        exp.initialize();
        //Buscar en la cadena la opción -xml y escribir el fichero.
        write(nombre_xml(args), exp);
        //AQUI EMPIEZA EL CODIGO QUE LANZA EL EXPERIMENTO
        // 2. run experiment
    System.out.println("Initializing...");
    exp.initialize();
    System.out.println("Running...");
    exp.runExperiment();
    System.out.println("Finishing...");
    exp.postProcess();
    
    }
}
