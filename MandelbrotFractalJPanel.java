import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MandelbrotFractalJPanel extends JuliaFractalJPanel{
    protected FractalCoordinates fc = new FractalCoordinates();
    private int screenWidth;
    private int screenHeight;

    public MandelbrotFractalJPanel(int s_x, int s_y) {
        super(s_x, s_y);
        screenWidth = s_x;
        screenHeight = s_y;
        offset.x = -2.0;
        offset.y = -1.2;
        startPan.x = 0.0;
        startPan.y = 0.0;

        frac_tl.x = -2.0;
        frac_tl.y = -1.0;
        frac_br.x = 1.0;
        frac_br.y = 1.0;
        vScale = s_x;
        JFrame JuliaSetFrame = new JFrame("Julia set");
        var JuliaJPanel = new JuliaFractalJPanel(2*screenWidth, 2*screenHeight);
        JuliaSetFrame.add(JuliaJPanel);
        JuliaSetFrame.setSize( 2*screenWidth, 2*screenHeight);
        JuliaSetFrame.setLocation(0,0);
        JuliaSetFrame.setVisible(true);
        JuliaSetFrame.setResizable(false);

        JFrame SmallJuliaSetFrame = new JFrame("Mandelbrot - Julia set");
        var SmallJuliaJPanel = new JuliaFractalJPanel(screenWidth / 2, screenHeight / 2);
        SmallJuliaSetFrame.add(SmallJuliaJPanel);
        SmallJuliaSetFrame.setSize(screenWidth/2, screenHeight / 2);
        SmallJuliaSetFrame.setLocation(2*screenWidth + 10,0);
        SmallJuliaSetFrame.setVisible(true);
        SmallJuliaSetFrame.setResizable(false);

        JuliaSetFrame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                String filename;
                Date date = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_dd_ss");
                filename=dateFormat.format(date)+".png";
                File outputFile = new File(filename);

                if(keyEvent.getKeyChar()=='s') {
                    System.out.println("Saving fractal to file:" + filename);
                    try {
                        ImageIO.write(JuliaJPanel.screenshot, "png", outputFile);
                    } catch (IOException evt) {
                        System.out.println("" + evt);
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });

        addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        mouseCoordinates.x = e.getX();
                        mouseCoordinates.y = e.getY();
                        screenToFractal(mouseCoordinates, fc);
                        JuliaJPanel.cr = fc.x;
                        JuliaJPanel.ci = fc.y;
                        JuliaJPanel.resetCoordinates();
                        JuliaJPanel.repaint();
                    }
                }
        );

        addMouseMotionListener(
                new MouseMotionAdapter() {
                    public void mouseMoved(MouseEvent e) {
                        mouseCoordinates.x = e.getX();
                        mouseCoordinates.y = e.getY();
                        screenToFractal(mouseCoordinates, fc);
                        SmallJuliaJPanel.cr = fc.x;
                        SmallJuliaJPanel.ci = fc.y;
                        SmallJuliaJPanel.repaint();
                        System.out.println(fc.x + "+i" + fc.y);
                    }
                }
        );
    }

    @Override
    protected void CreateFractal(final PixelCoordinates lPix_tl,final PixelCoordinates lPix_br,final FractalCoordinates lFrac_tl, final FractalCoordinates lFrac_br, final int lIterations ){
        final double x_scale = (lFrac_br.x - lFrac_tl.x) / ((double) lPix_br.x - (double) lPix_tl.x);
        final double y_scale = (lFrac_br.y - lFrac_tl.y) / ((double) lPix_br.y - (double) lPix_tl.y);
        double x_pos;
        double y_pos = lFrac_tl.y;
        int y_offset = lPix_tl.y*screenWidth;
        int n;
        double zr, zi, re, im, cr, ci;

        for (int y = lPix_tl.y; y < lPix_br.y; y++)
        {
            x_pos = lFrac_tl.x;
            ci = y_pos;
            for (int x = lPix_tl.x; x < lPix_br.x; x++)
            {
                cr = x_pos;
                zr = 0.0;
                zi = 0.0;
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
}
