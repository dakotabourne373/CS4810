.PHONEY: build, run

build: Program.class

run: Program.class
	java ImageGenerator $(file)

Program.class: ImageGenerator.java
	javac ImageGenerator.java Point.java Matrix.java sortByXRounded.java