package examples;


import java.net.URL;
import java.nio.IntBuffer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import naitsirc98.imagesplitter.ImageSplitter;
import naitsirc98.imagesplitter.ImageSplitter.BackgroundType;
import naitsirc98.imagesplitter.ImageBounds;
import naitsirc98.imagesplitter.SplittedImageList;

public class JavaFXExample extends Application {
	
	private static final String IMG = "http://commondatastorage.googleapis.com/codeskulptor-assets/explosion.hasgraphics.png";
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		
		Image image = new Image(new URL(IMG).openStream());
		
		Canvas canvas = new Canvas(image.getWidth(), image.getHeight());
		
		stage.setScene(new Scene(new Group(canvas)));
		
		stage.sizeToScene();
		
		stage.show();
		
		PixelReader reader = image.getPixelReader();
		
		int[] buffer = new int[(int) (image.getWidth()*image.getHeight())];
		
		reader.getPixels(0, 0, (int)image.getWidth(), (int)image.getHeight(),
				(WritablePixelFormat<IntBuffer>) PixelFormat.getIntArgbInstance(), buffer, 0, (int)image.getWidth());
		
		ImageSplitter splitter = new ImageSplitter(buffer, (int)image.getWidth(), (int)image.getHeight());
		
		splitter.setBackground(BackgroundType.TRANSPARENT_0x33);
		
		SplittedImageList sprites = splitter.split();
		
		GraphicsContext g = canvas.getGraphicsContext2D();
		
		g.drawImage(image, 0, 0);
		
		g.setStroke(Color.GREEN);
		
		for(int i = 0;i < sprites.size();i++) {
			
			ImageBounds b = sprites.get(i);
			
			g.strokeRect(b.getX(), b.getY(), b.getWidth(), b.getHeight());
			
			g.strokeText(String.valueOf(i), b.getX(), b.getY());
			
		}
		
	}



}
