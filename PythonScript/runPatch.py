import os
import json
import glob
from pathlib import Path
import time
import shutil
import subprocess, shlex
from threading import Timer
#---------------------------------------------------------------------
# Deploys the Project 
#---------------------------------------------------------------------
def deployProj(proj, version, rootPath_i, supportPath_i, projPath_i):
	# Fetch the defects4j config file
	defects4jConfig = os.path.join(rootPath_i, "defects4j.build.properties")
	
	configDict = dict()
	configDict["proj"] = proj
	configDict["version"] = version

	with open(defects4jConfig, 'r') as file:
		for line in file:
			if "d4j.dir.src.classes" in line:
				configDict["classes"] = line.rstrip("\n").split("=")[1]
			if "d4j.dir.src.tests" in line:
				configDict["tests"] = line.rstrip("\n").split("=")[1]

	if proj == "Chart":
		configDict["ant_folder"] = "ant"
		configDict["build_file"] = "ant/build.xml"
	else:
		configDict["ant_folder"] = ""
		configDict["build_file"] = "build.xml"

	# copy related files
	classesRootPath = configDict["classes"].split("/")[0]

	# copy src proj folder into results
	code_folder_src = os.path.join(rootPath_i)
	code_folder_dst = os.path.join(projPath_i)
	shutil.rmtree(code_folder_dst)
	shutil.copytree(code_folder_src, code_folder_dst)

	# copy Driver of the bug into tests
	testDriver_loc = projPath_i + "/src/test/java/sketchFix"
	testDriver_src = os.path.join(supportPath_i, "Sketch4JDriver.java")
	Path(testDriver_loc).mkdir(parents=True, exist_ok=True)
	testDriver_dst = testDriver_loc + "/Sketch4JDriver.java"
	shutil.copyfile(testDriver_src, testDriver_dst)

	# copy build file into the folder
	buildXML_src = os.path.join(supportPath_i, "build.xml")
	buildXML_dst = os.path.join(projPath_i, configDict["build_file"])
	os.remove(buildXML_dst)
	shutil.copyfile(buildXML_src, buildXML_dst)

	return configDict
#---------------------------------------------------------------------
# Replaces the jars in the build.xml
#---------------------------------------------------------------------
def repalceLibPath(projPath_i, configDict, jarRootPath):
	#The original Path of the libs
	libPath_org = "/Users/lisahua/projects/jpf/example/workspace/Sketch4J"
	libPath_new = jarRootPath
	buildFile = os.path.join(projPath_i, configDict["build_file"])
	replacementCMD = "sed -i 's#"+ libPath_org +"#"+ libPath_new +"#g' " + buildFile 
	os.system(replacementCMD)
	
	#All 1.4 must be replaced to 1.7
	if configDict["proj"] == "Chart":
		replacementCMD2 = "sed -i -e 's/%s/%s/g' %s" %('\"1.4\"', '\"1.7\"', buildFile)
		os.system(replacementCMD2)
		replacementCMD3 = "sed -i -e 's#${basedir}/source#${basedir}/src/main/java#g' "+ buildFile
		os.system(replacementCMD3)
		replacementCMD3 = "sed -i -e 's#${basedir}/tests#${basedir}/src/test#g' "+ buildFile
		os.system(replacementCMD3)
#---------------------------------------------------------------------
#If the java file was replaced, run the SketchFix4JDriver:
#---------------------------------------------------------------------
def runAnt(projPath_i, configDict):
	#-------------------------------------------------------------
	#Process of killing ANT
	#-------------------------------------------------------------
	def kill_proc(proc, timeout):
		timeout["value"] = True
		proc.kill()

	#-------------------------------------------------------------
	#Process of running ANT
	#-------------------------------------------------------------
	def run(cmd, timeout_sec):
		proc = subprocess.Popen(shlex.split(cmd), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		timeout = {"value": False}
		timer = Timer(timeout_sec, kill_proc, [proc, timeout])
		timer.start()
		stdout, stderr = proc.communicate()
		timer.cancel()
		return proc.returncode, stdout.decode("utf-8"), stderr.decode("utf-8"), timeout["value"]

	#Change the path to the ant folder of the project and run ANT.
	patchPath = os.path.join(projPath_i, configDict["ant_folder"])
	currentPath = os.getcwd()
	os.chdir(patchPath)
	print("Start compiling SketchFixDriver... ")
	print("         ")
	antCMD = "ant sketch4j"
	returncode, stdout_msg, stderr_msg, timeout_flag = run(antCMD, 180)
	os.chdir(currentPath)

	#If you want to see the javac and java messages displayed uncomment:
	#print(stdout_msg)
	#print(stderr_msg)
	
	#Decides with the javacompiler output wheather correct or false output.
	if timeout_flag:
		print("☹ Timeout - No Solution can be found."),
		return False, None

	elif "[java] Found solution:" in stdout_msg:
		print("✓ Success! Found solution:")
		print(stdout_msg[stdout_msg.find('[java] Found solution:',0)+22:stdout_msg.find('BUILD SUCCESSFUL',0)])
		msgList = stdout_msg.split("\n")
		returnList = []
		flag = False
		for msg_i in msgList:
			if " candidates for the" in msg_i:
				flag = True
			if flag and (("Hole" in msg_i) or ("Space:" in msg_i) or (" candidates for the" in msg_i)):
				returnList.append(msg_i)
			if "BUILD SUCCESSFUL" in msg_i:
				break
		return True, returnList

	elif "[java] No solution!" in stdout_msg:
		print("☹ After all no Solution can be found.")
		msgList = stdout_msg.split("\n")
		returnList = []
		flag = False
		for msg_i in msgList:
			if " candidates for the" in msg_i:
				flag = True
			if flag and (("Hole" in msg_i) or ("Space:" in msg_i) or (" candidates for the" in msg_i)):
				returnList.append(msg_i)
			if "BUILD SUCCESSFUL" in msg_i:
				break
		return False, returnList

	else:
		print("☹ No output at all.")
		return False, None
#---------------------------------------------------------------------
# Test all Patches with SketchFix
#---------------------------------------------------------------------
def testAllPatches(projPath_i, configDict, sketchesPath):
	#Get to the SketchList:	
	patchPath = sketchesPath
	print("Sketches will be searchd in:", patchPath)
	patchList = glob.glob(os.path.join(patchPath, "*.java*"))
	if len(patchList) == 0: 
		print("✖ No patches found Did you put the sketches into the right folder?") 
	else:
		print("Number of possible sketches found:", len(patchList))

	#Some lists and counter for information
	validPatchList = []
	validPatchDict = dict()
	validTimeList = []
	nonValidPatchList = []
	nonValidPatchDict = dict()
	nonValidTimeList = []
	counter = 1

	for patch_i in patchList:
		#Print the Name of the patch and the number of the patch which will be edited.
		patchName = patch_i.split("/")[-1]
		print("..................................................")
		print("#", counter)
		javaFileFullName = patch_i.split(".java")[0] + ".java"
		className = javaFileFullName.split("/")[-1]
		sketchNumber = patch_i.split(".java")[1]
		print("SketchNumber:", sketchNumber[1:])
		print("Class:", className)
		print("..................................................")

		#Find the Java File path in the project:
		javaFilePath = os.path.join(projPath_i, "src")
		findJavaCMD = 'find %s -wholename "*\\/%s"' %(javaFilePath, className)
		process = os.popen(findJavaCMD)
		javaFileList = process.read().rstrip("\n").split("\n")
		process.close()

		#If the Java file was found then print success 
		if len(javaFileList) == 0: 
			print("✖ The class could not been found.") 
		elif len(javaFileList) > 1:
			print("✖ More then one class found?:", len(javaFileList))
		
		#For every java file in the javaFileList start sketchFix and replace the sketch
		#with the original java file.
		for javaFile in javaFileList:
			#Make a Backup of the changed class
			backupfolder = projPath_i + "/backup"
			Path(backupfolder).mkdir(parents=True, exist_ok=True)
			if javaFile not in backupfolder:
				shutil.copyfile(javaFile, backupfolder + "/" + className)

			#Replace the old file to the sketches file.
			shutil.copyfile(patch_i, javaFile)

			#Run Sketch4JDriver and take the time.
			startingTime = time.time()
			flag, returnList = runAnt(projPath_i, configDict)
			deltaTime = time.time() - startingTime
			print("☐ Time needed for this:",deltaTime, "seconds.")
			print("☐ Time needed for this:",deltaTime/60, "minutes.")
			
			#If the flag which was set is valid then the patchName with the deltaTime will be 
			#stored in validList, else in invalidList.
			if flag:
				validPatchList.append(patchName)
				validTimeList.append(deltaTime)
				validPatchDict[patchName] = returnList
			else:
				nonValidPatchList.append(patchName)
				nonValidTimeList.append(deltaTime)
				if returnList is not None:
					nonValidPatchDict[patchName] = returnList

			#Recover the file from backup.
			shutil.copyfile(backupfolder + "/" + className, javaFile)
			counter += 1
	# If every sketch has passed then the list will be stored in an array.
	patchDict = dict()
	patchDict["vpList"] = validPatchList
	patchDict["vpDict"] = validPatchDict
	patchDict["vtList"] = validTimeList
	patchDict["nvpList"] = nonValidPatchList
	patchDict["nvtList"] = nonValidTimeList
	patchDict["nvpDict"] = nonValidPatchDict
	return patchDict
#---------------------------------------------------------------------
#Save the result to this evaluation:
#---------------------------------------------------------------------
def saveResult(configDict, patchDict, resultPath_i, sketchesPath_i):
	currentProj = configDict["proj"] + "_" + configDict["version"]
	#Create a .txt file were all valid patches are stored.
	total_delta_time_valid = 0
	total_sketches_valid = 0
	with open(os.path.join(resultPath_i, "validPatches.txt"), 'w+') as file:	
		total_sketches_valid = len(patchDict["vpList"])
		for i in range(len(patchDict["vpList"])):
			file.write("##################################################################### \n")
			patch_i = patchDict["vpList"][i]
			delta_time_i = patchDict["vtList"][i]
			total_delta_time_valid = total_delta_time_valid + float(delta_time_i)
			file.write("PatchName: " + patch_i + "\n")
			file.write("PatchTime: " + str(delta_time_i) + "\n")
			for j in patchDict["vpDict"][patch_i]:
				file.write(j + "\n")
			file.write("\n")
			
	total_delta_time_nonvalid = 0
	#Create a .txt file were all invalid patches are stored.
	with open(os.path.join(resultPath_i, "invalidPatches.txt"), 'w+') as file:
		total_sketches_nonvalid = len(patchDict["nvpList"])
		for i in range(len(patchDict["nvpList"])):
			file.write("##################################################################### \n")
			patch_i = patchDict["nvpList"][i]
			delta_time_i = patchDict["nvtList"][i]
			total_delta_time_nonvalid = total_delta_time_nonvalid + float(delta_time_i) 
			file.write("PatchName: " + patch_i + "\n")
			file.write("PatchTime: " + str(delta_time_i) + "\n")
			if patch_i in patchDict["nvpDict"]:
				for j in patchDict["nvpDict"][patch_i]:
					file.write(j + "\n")
			file.write("\n")

	#Create a .txt file were the information about time and number of patches is stored.
	with open(os.path.join(resultPath_i, "results.txt"), 'w+') as file:
		file.write("##################################################################### \n")
		file.write("				RESULTS					 \n")
		file.write("                       FOR " + currentProj)
		file.write("\n #####################################################################")
		file.write("\n Total valid patches: " + str(total_sketches_valid))
		file.write("\n Total time for valid patches in seconds: " + str(total_delta_time_valid))
		file.write("\n Total time for valid patches in seconds: " + str(total_delta_time_valid/60))
		file.write("\n Total nonvalid patches: " + str(total_sketches_nonvalid))
		file.write("\n Total time for nonvalid patches in seconds: " + str(total_delta_time_nonvalid))
		file.write("\n Total time for nonvalid patches in seconds: " + str(total_delta_time_nonvalid/60))
		file.write("\n")
		file.write("\nTotal patches: " + str(total_sketches_valid + total_sketches_nonvalid))
		file.write("\nTotal time in seconds: " + str(total_delta_time_nonvalid + total_delta_time_valid))
		file.write("\nTotal time in minutess: " + str((total_delta_time_nonvalid + total_delta_time_valid)/60))
	
	#Copy all valid patches into the results folder:
	Path(resultPath_i + "/patches").mkdir(parents=True, exist_ok=True)
	for patch_i in patchDict["nvpList"]:
		srcFileName = os.path.join(sketchesPath_i, patch_i)
		dstFileName = os.path.join(resultPath_i, "patches", patch_i)
		shutil.copyfile(srcFileName, dstFileName)
	#Return the time 
	return total_sketches_valid, total_delta_time_valid, total_delta_time_nonvalid
#--------------------------------------------------------------------------
#				SketchFix®
#		
#--------------------------------------------------------------------------
#Here a list is created were all patches which should be evaluated are listed.
projDict = dict()
#Plausible
#projDict["Chart"] = [13, 26]
#projDict["Closure"] = [70, 73]
#projDict["Lang"] = [51]
#projDict["Math"] = [73]
#projDict["Time"] = [4]
#projList = ["Chart", "Lang", "Math", "Time", "Closure"]

#Correct
projDict["Chart"] = [1, 8, 9, 11, 20, 24]
projDict["Closure"] = [14, 62, 126]
#projDict["Lang"] = [6, 55, 59]
projDict["Math"] = [5, 33, 50, 59, 70, 82, 85]
projList = ["Chart", "Math"]

#Test
#projDict["Chart"] = [8]
#projDict["Math"] = [59, 50]
#projList = ["Chart"]

#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#These paths must be set:
#Here the results of SketchFix will be inserted.
resultPath = "/home/daniel/SketchFix_Evaluation/SketchFix-Results"
#The folder where the SketchFix master lies.
supportPath = "/home/daniel/SketchFix_Evaluation/SketchFix-master/support"
#The folder where Defects4J project are.
projRootPath = "/home/daniel/SketchFix_Evaluation/Defects4J"
#The folder where the Jars for SketchFix are listed. 
jarRootPath = "/home/daniel/SketchFix_Evaluation/SketchFix_Jars"
#The folder where the Sketches are located.
sketchesPath = "/home/daniel/SketchFix_Evaluation/SketchFix-Sketches"
#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

#Start:
print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
print("	             SketchFix® 			 ")
print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
print("✽ TODO: These defaults paths are to set before execution: ")
print("✽ Here the results of SketchFix will be inserted:")
print(resultPath)
print("✽ Here the SketchFix master is located:")
print(supportPath)
print("✽ Here the Defects4j folder with all versions is deployed:")
print(projRootPath)
print("✽ Here the SketchFix Jars are located:")
print(jarRootPath)
print("✽ Here generated Sketches are located (need to be manual created):")
print(sketchesPath)
print("   ")
my_time = time.strftime("%Y_%m_%d_%H_%M_%S")
sketches_All = 0
time_valid_All = 0
time_nonvalid_All = 0
for proj_i in projList:
	# Start project
	print("##################################################")
	projectname = str(proj_i)
	print("project:", projectname)
	print("##################################################")
	sketches_proj = 0
	time_valid_proj = 0
	time_nonvalid_proj = 0
	for ver_j in projDict[proj_i]:
                # Fetch version
		version = str(ver_j)

		#Set Directory (current Project with version):
		currentProjFolder =  "/" + projectname + "_" + version
		
		#Set rootPath (Where the Project folder src is located of Defects4j)
		rootPath_i = projRootPath + "/" + projectname  + currentProjFolder
		print("➳ Defects4J Projects (rootPath_i):")
		print(rootPath_i)	

		# Set the projectPath_i (Where the source is in it)
		projPath_i = resultPath + "/" + my_time + "/" + projectname + currentProjFolder
		Path(projPath_i).mkdir(parents=True, exist_ok=True)
		print("➳ SketchFix Result Folder Src (projPath_i):")
		print(projPath_i)

		#Set resultPath (Where the results are stored in)
		resultPath_i = resultPath + "/" + my_time + "/" + projectname + "/results"
		Path(resultPath_i + currentProjFolder).mkdir(parents=True, exist_ok=True)
		print("➳ SketchFix Valid/Invalid Result (resultPath_i):")
		print(resultPath_i)

		#Set supportPath (Where the SketchFix github directory is)
		supportPath_i = supportPath + currentProjFolder	
		print("➳ SketchFix Master From Git (supportPath_i):")
		print(supportPath_i)

		#Set sketchesPath (Where the before generated sketches are located)
		sketchesPath_i = sketchesPath + currentProjFolder
		print("➳ SketchFix Sketches from EdSketch (sketchesPath):")
		print(supportPath_i)

		# Deploy Project 
		print("**************************************************")
		print("✎ Starting to deploy", projectname, version + ":")  
		configDict = deployProj(proj_i, version, rootPath_i, supportPath_i, projPath_i)
		print("✓ Success! Everything has been deployed in: " + projectname + "_" + version)

		#ReplaceLibPath started with the array where information is stored.
		print("✎ Starting to deploy the jars for SketchFix:")
		repalceLibPath(projPath_i, configDict, jarRootPath)
		print("✓ Success! The jars are set in: " + projectname + "_" + version)

		#Test all Patches
		print("✎ Starting to test all patches:")
		patchDict = testAllPatches(projPath_i, configDict, sketchesPath_i)
		print("✓ Done: All test have been executed: " + projectname + "_" + version)

		#Save the Result
		print("✎ Starting to save the results:")
		total_sketches_valid, total_time_valid, total_time_nonvalid = saveResult(configDict, patchDict, resultPath_i + currentProjFolder, sketchesPath_i)
		print("✓ Done: The results are stored in: " + resultPath_i + currentProjFolder)
		print("**************************************************")
		print("✓ " + projectname + "_" + version + " done.")
		print("☐ ☐ Time needed for :", projectname + "_" + version,total_time_valid + total_time_nonvalid, "seconds.")
		print("☐ ☐ Time needed for :",  projectname + "_" + version, (total_time_valid + total_time_nonvalid)/60, "minutes.")
		sketches_proj = sketches_proj + total_sketches_valid
		time_valid_proj = time_valid_proj + total_time_valid
		time_nonvalid_proj = time_nonvalid_proj + total_time_nonvalid
	#END of Project:
	print("**************************************************")
	print("++++++++++++++++++++++++++++++++++++++++++++++++++")
	print("                                                  ")
	print("✓ " + projectname + " done.")
	print("☐ ☐ ☐ Time needed", time_valid_proj  + time_nonvalid_proj, "seconds.")
	print("☐ ☐ ☐ Time needed", (time_valid_proj  + time_nonvalid_proj)/60, "minutes.")
	print("☐ ☐ ☐ Sketches for:", sketches_proj)
	print("                                                  ")
	print("++++++++++++++++++++++++++++++++++++++++++++++++++")
	print("                                                  ")
	sketches_All = sketches_All + sketches_proj
	time_valid_All = time_valid_All + time_valid_proj
	time_nonvalid_All = time_nonvalid_All + time_nonvalid_proj

#END of Program:
print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
print("☑ All Patches are tested with SketchFix.")
print("☑ Time needed in total:",time_valid_All  + time_nonvalid_All, "seconds.")
print("☑ Time needed in total:", (time_valid_All  + time_nonvalid_All)/60, "minutes.")
print("☑ Sketches in total:", sketches_All)
print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
#--------------------------------------------------------------------------	
#--------------------------------------------------------------------------			
