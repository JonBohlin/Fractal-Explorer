import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.Math;

class FractalCoordinates{
	double x,y;
}

class PixelCoordinates{
	int x,y;
}

public class JuliaFractalJPanel extends JPanel{//} implements MouseWheelListener{
	protected int screenWidth;
	protected int screenHeight;
	protected FractalCoordinates offset = new FractalCoordinates();
	protected FractalCoordinates startPan = new FractalCoordinates();
	protected FractalCoordinates mouseCoordinatesBeforeZoom = new FractalCoordinates();
	protected FractalCoordinates mouseCoordinatesAfterZoom = new FractalCoordinates();
	protected FractalCoordinates frac_tl = new FractalCoordinates();
	protected FractalCoordinates frac_br = new FractalCoordinates();
	protected PixelCoordinates mouseCoordinates = new PixelCoordinates();
	protected PixelCoordinates pix_tl = new PixelCoordinates();
	protected PixelCoordinates pix_br = new PixelCoordinates();
	protected BufferedImage screenshot;

	protected double vScale;
 	protected int maxThreads = Runtime.getRuntime().availableProcessors();
	protected final int defaultInter = 128;
	protected int nIterations = defaultInter;
	protected int[] fractalColors;
	protected final int numThreads = 10;
	protected double cr = -0.4;
 	protected double ci = 0.6;
		
	public JuliaFractalJPanel(int s_x, int s_y){
		resetCoordinates();
		pix_tl.x=0; pix_tl.y=0;
		screenWidth = s_x;
		screenHeight = s_y;
		vScale = s_x/2;
		pix_br.x=screenWidth; pix_br.y=screenHeight;
		System.out.println("Number of physical cores:"+maxThreads);
		fractalColors= new int[ screenWidth * screenHeight ];

		addMouseMotionListener(
        	new MouseMotionAdapter(){
        		public void mouseMoved( MouseEvent e ){
        			mouseCoordinates.x = e.getX();
					mouseCoordinates.y = e.getY();
       			}
					
				public void mouseDragged( MouseEvent e ){
						startPan.x = e.getX();
						startPan.y = e.getY();
						offset.x -= (mouseCoordinates.x - startPan.x )/vScale;
						offset.y -= (mouseCoordinates.y - startPan.y )/vScale;
						startPan.x = mouseCoordinates.x;
						startPan.y = mouseCoordinates.y;
					}
				}
			);

		addMouseWheelListener(
				e -> {

					screenToFractal(mouseCoordinates ,mouseCoordinatesBeforeZoom);
					startPan.x = mouseCoordinates.x;
					startPan.y = mouseCoordinates.y;

       				if( e.isControlDown() && e.getWheelRotation() < 0 ){
       					nIterations+=64;
       					System.out.println("Iterations :"+nIterations);
       				}

       				if( e.isControlDown() && e.getWheelRotation() > 0 && nIterations>64 ){
       					nIterations-=64;
	       				System.out.println("Iterations :"+nIterations);
	       			}

					if (e.getWheelRotation() < 0 && !e.isControlDown() && !e.isShiftDown() && !e.isAltDown())
        				vScale*=1.1;

					if (e.getWheelRotation() > 0 && !e.isControlDown() && !e.isShiftDown() && !e.isAltDown())
        				vScale*=0.9;

        			screenToFractal(mouseCoordinates ,mouseCoordinatesAfterZoom);
        			offset.x += (mouseCoordinatesBeforeZoom.x - mouseCoordinatesAfterZoom.x);
        			offset.y += (mouseCoordinatesBeforeZoom.y - mouseCoordinatesAfterZoom.y);
        		}
		);
	}

	protected FractalCoordinates screenToFractal(PixelCoordinates p ,FractalCoordinates f){
		f.x = ((double) p.x /vScale*2.5)+offset.x;
		f.y = ((double) p.y /vScale*2.5)+offset.y;
		return f;
	}

	public void resetCoordinates(){
		offset.x= -2.5;
		offset.y= -2.5;
		startPan.x = 0.0;
		startPan.y = 0.0;

		frac_tl.x = -2.0; frac_tl.y = -1.0;
		frac_br.x = 1.0; frac_br.y = 1.0;
		vScale = screenWidth/2;
		nIterations = defaultInter;
	}

	@Override
	public void update( Graphics g ){
		Color myColor;
		BufferedImage br= new BufferedImage(screenWidth,screenHeight,BufferedImage.TYPE_INT_RGB);
		frac_tl = screenToFractal( pix_tl, frac_tl);
		frac_br = screenToFractal( pix_br, frac_br);
		MultiThreadFractal(pix_tl, pix_br, frac_tl, frac_br, cr, ci, nIterations);
		for(int y=0; y<screenHeight; y++){
			for(int x=0; x<screenWidth; x++){
				int col = fractalColors[y*screenWidth + x];
				final double a = 0.1;
				int red = (int)((1.0-(0.5*Math.sin(a* (double) col)+0.5))*256);
				int green = (int)((1.0-(0.5*Math.sin(a* (double) col + 2.094)+0.5))*256);
				int blue = (int)((1.0-(0.5*Math.sin(a* (double) col +4.188)+0.5))*256);
				myColor = new Color(red, green, blue);
				br.setRGB( x,y, myColor.getRGB() );
			}
		}
		g.drawImage(br, 0 ,0 ,null);
	}

	public void paintComponent( Graphics g ){
		BufferedImage bufferedImage = new BufferedImage(screenWidth,screenHeight,BufferedImage.TYPE_INT_RGB);
		Graphics gg = bufferedImage.getGraphics();

		update( gg );
		g.drawImage( bufferedImage, 0, 0, null);
		screenshot = bufferedImage;
	}

	protected void CreateFractal(final PixelCoordinates lPix_tl,final PixelCoordinates lPix_br,final FractalCoordinates lFrac_tl, final FractalCoordinates lFrac_br, final int lIterations ){
		final double x_scale = (lFrac_br.x - lFrac_tl.x) / ((double) lPix_br.x - (double) lPix_tl.x);
		final double y_scale = (lFrac_br.y - lFrac_tl.y) / ((double) lPix_br.y - (double) lPix_tl.y);
		double x_pos, zr, zi, re, im;
		double y_pos = lFrac_tl.y;
		int y_offset = lPix_tl.y*screenWidth;
		int n;

		for (int y = lPix_tl.y; y < lPix_br.y; y++)
		{
			x_pos = lFrac_tl.x;

			for (int x = lPix_tl.x; x < lPix_br.x; x++)
			{
				zr = x_pos;
				zi = y_pos;
				n = 0;
				while ((zr * zr + zi * zi) < 4.0 && n < lIterations)
				{
					re = zr * zr - zi * zi + cr;
					im = zr * zi * 2.0 + ci;
					zr = re;
					zi = im;
					n++;
				}
				fractalColors[y_offset + x] = n;
				x_pos += x_scale;
			}
			y_pos += y_scale;
			y_offset += screenWidth;
		}
	}

	protected void MultiThreadFractal(final PixelCoordinates lpix_tl,final PixelCoordinates lpix_br,final FractalCoordinates lfrac_tl, final FractalCoordinates lfrac_br, final double cr, final double ci, final int iterations ){
		final int sectionHeight = (lpix_br.y - lpix_tl.y)/numThreads;
		final double fractalHeight = (lfrac_br.y - lfrac_tl.y)/ (double) numThreads;
		FractalCoordinates tFrac_tl = new FractalCoordinates();
		FractalCoordinates tFrac_br = new FractalCoordinates();
		PixelCoordinates tPix_tl = new PixelCoordinates();
		PixelCoordinates tPix_br = new PixelCoordinates();

		final Thread[] myThreads = new Thread[ numThreads ];
		tPix_tl.x = lpix_tl.x;
		tPix_br.x = lpix_br.x;			
		tFrac_tl.x = lfrac_tl.x;
		tFrac_br.x = lfrac_br.x;

		for(int i = 0; i < numThreads; i++){
			tPix_tl.y = lpix_tl.y+sectionHeight*(i);
			tPix_br.y = lpix_tl.y+sectionHeight*(i+1);
			tFrac_tl.y = lfrac_tl.y+fractalHeight* (double) i;
			tFrac_br.y = lfrac_tl.y+fractalHeight* (i + 1.0);
			myThreads[i] = new Thread(() -> CreateFractal( tPix_tl, tPix_br, tFrac_tl, tFrac_br, iterations));
			myThreads[i].start();

        	try{
        		myThreads[i].join();

        	} catch (InterruptedException e){
        		System.out.println("Thread interrupted");
       		}
       	}
       	repaint();
   	}
}