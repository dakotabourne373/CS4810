.PHONEY: build, run

build: Program.class

bones: Program.class
	java ImageGenerator $(file)

Program.class: ImageGenerator.java
	javac ImageGenerator.java Point.java Matrix.java sortByXRounded.java ImageObj.java