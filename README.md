# Fractal-Explorer

Explore Julia set fractals through a Mandelbrot set.

![Screenshot_Fractal-Explorer](https://user-images.githubusercontent.com/20295285/126981180-c6f859d3-a5e4-43b3-accb-ac3df2d15b18.png)

These fractals can be considered as maps of complex polynomials. The colors designate the number of polynomial terms for which the polynomial converge (|z|<2). For more mathematical details on fractals see the excellent exposition by John Milnor:

https://arxiv.org/abs/math/9201272

Each point within the Mandelbrot set represent a corresponding Julia Set fractal that is shown in the small window termed "Mandelbrot - Julia set". Pressing the left mouse button enlarges the Julia set fractal to the largest window termed "Julia Set". You can zoom in and out using the mouse wheel in all windows. Holding down the control key while moving the mouse wheel increases or decreases the number of terms - more terms require substantial more computing power. Hitting the key 's' in the large "Julia set" window will save a .png file in the directory Fractal-Explorer was started from.

Fractal-Explorer is written in Java version 11 (should probably run in Java version >=8) and you can compile as follows:

javac Explorer.java FractalExplorer.java JuliaFractalJPanel.java MandelbrotFractalJPanel.java

and run it as follows:

java FractalExplorer

A JAR file can be created as follows:

jar cfe FractalExplorer.jar FractalExplorer ./*.class

It has been tested on Linux (Mint), Windows and macos (M1).
