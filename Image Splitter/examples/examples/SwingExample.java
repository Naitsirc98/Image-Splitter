package examples;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import naitsirc98.imagesplitter.ImageSplitter;
import naitsirc98.imagesplitter.ImageBounds;

public class SwingExample extends JPanel {

	private static final long serialVersionUID = -2832358245196062877L;

	private static final String IMG = "https://archive-media-0.nyafuu.org/vp/image/1409/30/1409309301323.png";

	static SwingExample p;

	static int threshold = 0x30;

	public static void main(String[] args) {

		BufferedImage image = null;

		try {

			BufferedImage img = ImageIO.read(new URL(IMG));

			image = new BufferedImage(img.getWidth(), img.getHeight(),
					BufferedImage.TYPE_INT_ARGB);

			Graphics g = image.getGraphics();

			g.drawImage(img, 0, 0, null);

			g.dispose();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		ImageSplitter splitter = new ImageSplitter(pixels, image.getWidth(), image.getHeight());
		
		List<ImageBounds> sprites = splitter.split();

		// GUI

		JFrame frame = new JFrame();

		frame.setSize(new Dimension(image.getWidth()+100, image.getHeight()+100));

		p = new SwingExample(image, sprites);

		p.setPreferredSize(new Dimension(image.getWidth()+100, image.getHeight()+100));
		
		frame.add(p);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setLocationRelativeTo(null);

		frame.setVisible(true);

	}


	private BufferedImage image;
	private List<ImageBounds> sprites;

	public SwingExample(BufferedImage image, List<ImageBounds> sprites) {
		this.image = image;
		this.sprites = sprites;
	}

	public void paintComponent(Graphics gr) {

		Graphics2D g = (Graphics2D) gr;

		final int offset = 20;
		
		g.drawImage(image, offset, offset, null);

		g.setColor(Color.GREEN.darker());

		int i = 0;

		for(ImageBounds r : sprites) {

			g.drawRect(r.getX()+offset,r.getY()+offset,r.getWidth(),r.getHeight());
			g.drawString(String.valueOf(i), r.getX()+offset, r.getY()+offset);
			i++;
		}

	}



}
