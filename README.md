SketchFix Evaluation on Defects4J

This is only an evulation of SketchFix. All rights
belong to the developers of SketchFix.

https://github.com/SketchFix/SketchFix.git

Test of my Python script to evaluate SketchFix on Defects4J:

1. Setup Defects4J:
First of all you have to config the Defects4J folder.
Therefore please download the folders and unpack them into 
the Defects4J folder of this repository:
Original (Time > Time_4 > 4 > src...)
Please make the following folder structure:
This repository (Defects4J > Time > Time_4 > src..)
The folder "4" must be deleted, unless the python script doesn't work.

Time: https://github.com/ali-ghanbari/d4j-time.git

Closure: https://github.com/ali-ghanbari/d4j-closure.git

Math: https://github.com/ali-ghanbari/d4j-math.git

Chart: https://github.com/ali-ghanbari/d4j-chart.git

Lang: https://github.com/ali-ghanbari/d4j-lang.git

2. Setup SketchFix-master:
Now you have to setup SketchFix.
Therefore please download the SketchFix-master and put the content it in the 
SketchFix-master folder of this repository.

SketchFix: https://github.com/SketchFix/SketchFix.git

3. Setup SketchFix-Jars:
Now you have to unzip the HelloWorld project.
Please put the content of the HelloWorld> HelloWorld > lib folder:
- javaparser-core-3.6.7.jar
- Sketch4J.jar
- SketchFix.jar
into the SketchFix_Jars folder of this project.
You also need to download for this folder:
- junit.jar
- hamcrest-core-1.3.jar

The PatchGenerator folder contains the PatchGenerator and his config, which 
I renewed. With this you can create patches (but it is not automated).

4. Setup SketchFix-Sketches:
Here the generated sketches are listed. Please therefore download the following 
project:

https://github.com/utlisahua/sketchFix-evaluation.git

In there all Java file sketches are put in.


5. My results:
In the results folder you can see my evaluation.
The results.txt contains the time and the number of plausible sketches created.
In the seperate folders the valid patches and invalid patches of the project with the bug 
id is listed. In the patches folder the created patches are listed. The output
also contains the (actual) Java code which fixes the bug. 

6. Run Python script
Please at first configurate the Python paths (descirbed in the Python script). 
Then you can run the Python script with:
- Python 3
- Java 1.8.0_241
- Ant 1.10.5
This scirpt tests the sketches in the sketchesFolder by running the tests and filling
the holes, while testing. If the sketch is valid then an output is created where the 
fix of the Java bug is listed. I tested this on a platform containing an 
I3 CPU 2.0 GHZ with 8GB Ram an Ubunto 18.04.

7. TODO/ Future Work:
- Automate process of creating patches. Therefore please at first automate EdSketch (which 
  creates the sketches for SketchFix4J)
- Automate process of transforming holes back to basic Java code with my created output. 

