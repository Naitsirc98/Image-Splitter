package naitsirc98.imagesplitter;

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Objects of this class can split an image into multiple ones.
 * 
 * It can split the image in 3 different ways:
 * 
 * <ul>
 * 
 * <p>Split by grid: if your image contains subimages in a grid layout, then you should use this method</p>
 * <p>Split by fixed width and height: if the subimages have the same width and height, you should use this method.</p>
 * <p>Split automatically: the algorithm find and sort the subimages based on the colors of the pixels.</p>
 * </ul>
 * 
 * <p>All of the above methods return a {@link SplittedImageList} object, which is an extended ArrayList of {@link ImageBounds}</p>
 * 
 * <p>An ImageSplitter works with 1 dimensional {@code int} arrays. The array represents the image, where each value is the 
 * color of a pixel in ARGB format (1 byte for alpha, 1 byte for red, 1 byte for green and 1 byte for blue).</p>
 * 
 * <p>When {@code split} method is called, it makes a copy of the image array so the <b>original image is never modified</b> within this
 * class.<p>
 * 
 * <p>However, when set, the image is not copied, so <b>if the array is modified outside this class, 
 * it will modified here as well</b>.</p>
 * 
 * <p>Since it uses 1 dimensional arrays, you must specify the width and height of the image. You may choose a certain region of the image
 * to be splitted by setting a smaller width and/or height. Bounds are checked at start of each <i>split</i> method</p>
 * 
 * <p>Usually, and specially when you are working with spritesheets, there might be subimages that have particles or something similar.
 * This is not a problem if you split the image with the 2 first split methods, but it could cause strange results when
 * using the automatic split. Due to that, there is a {@code particleSize} and {@code particleDistance} attributes you can
 * set to help the algorithm to interpret those images as only one:</p>
 * 
 * <ul>
 * 
 * <p>{@code particleDistance} is the maximum distance between a particle an its parent.</p>
 * 
 * <p>{@code particleSize} is the maximum size of a particle. Sprites with a smaller or equal size are interpreted 
 * as particles. Remember that <i>size = width * height</i>.</p>
 * 
 * </ul>
 * <p>Both are 0 as default (so they are not activated). Please be aware that higher those attributes are, higher the
 * possibility of interpret normal sprites as particles, so be careful. A safer but manual way of handle particles is 
 * using the {@code blend} method of the {@link SplittedImageList} class</p>
 * 
 * <p>You may tell the ImageSplitter what kind of background the image has. By default it is a transparent background, but 
 * you can set whatever background you need with the {@code setBackground} method. See {@link BackgroundType} for more information</p>
 * 
 * 
 * */
public class ImageSplitter {

	private int[] image;
	private int width, height;
	private int particleSize;
	private int particleDistance;
	private BackgroundType background = BackgroundType.TRANSPARENT_0x33;

	/**
	 * Default constructor.
	 **/
	public ImageSplitter() {
		
	}
	
	/**
	 * Constructs a new ImageSplitter object.
	 * 
	 * @param image the array image
	 * @param width the width of the region to split
	 * @param height the height of the region to split
	 * 
	 * 
	 **/
	public ImageSplitter(int[] image, int width, int height) {
		this.image = image;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Splits the image by fixed width and height.
	 * 
	 * @param w the width of a subimage
	 * @param h the height of a subimage
	 * @param hPadding the horizontal padding
	 * @param vPadding the vertical padding 
	 * 
	 * @return the list of the subimage bounds
	 * 
	 **/
	public SplittedImageList split(int w, int h, int hPadding, int vPadding) {
		
		check();
		
		final int columns = width / (w+hPadding);
		final int rows = height / (h+vPadding);
		
		SplittedImageList sprites = new SplittedImageList();
		
		for(int y = 0;y < rows;y++) {
			
			for(int x = 0;x < columns;x++) {
				
				ImageBounds img = new ImageBounds(x*(w+hPadding), y*(h+vPadding), w, h);
				
				img.row = y;
				img.column = x;
				
				sprites.add(img);
				
			}
			
		}
		
		return sprites;
	}

	/**
	 * Splits the image by a given number of rows and columns
	 * 
	 * @param rows the number of rows
	 * @param columns the number of columns
	 * 
	 * @return the list of the subimage bounds
	 * 
	 * */
	public SplittedImageList split(int rows, int columns) {
		return split(width/columns, height/rows, 0, 0);
	}

	/**
	 * Splits the image automatically, based on pixel colors. It uses the background attribute
	 * to determine if a pixel is a background pixel or not. Attributes particleSize and particleDistance 
	 * are used here to. For more information, see {@link ImageSplitter}.
	 * 
	 * @return the list of the subimage bounds
	 * 
	 **/
	public SplittedImageList split() {
		
		check();

		SplittedImageList sprites = new SplittedImageList();

		final int[] pixels = image.clone();

		for(int y = 0;y < height;y++) {
			for(int x = 0;x < width;x++) {

				if(!background.contains(pixels[x+y*width])) {

					ImageBounds r = floodFill(pixels,x,y,width,height);
					
					addIfNotParticle(r, sprites);

				}

			}
		}
		
		return sort(sprites);
	}
	
	private void addIfNotParticle(ImageBounds r, SplittedImageList sprites) {
		
		Iterator<ImageBounds> it = sprites.iterator();
		
		while(it.hasNext()) {
			
			ImageBounds b = it.next();
			
			// Si se cortan, vamos a comprobar si podemos unirlos o no
			if(b.intersects(r) || b.distance(r) <= particleDistance) {
				
				ImageBounds min = ImageBounds.min(b,r);
				ImageBounds max = ImageBounds.max(b,r);
				
				if(min.equals(max)) {
					continue; // Son iguales
				}
				
				final int dx = Math.abs(r.x-b.x);
				final int dy = Math.abs(r.y-b.y);
				
				if(min.getSize() <= particleSize) {
					
					// Ajustamos la x y el ancho si es necesario
					if(min.x < max.x) {
						max.x = min.x;
						max.width += dx;
					} else if(min.x + min.width > max.x + max.width) {
						max.width += Math.abs(max.x + max.width - min.x + min.width);
					}
					
					// Ajustamos la y y el alto si es necesario
					if(min.y < max.y) {
						max.y = min.y;
						max.height += dy;
					} else if(min.y + min.height > max.y + max.height) {
						max.height += Math.abs(max.y + max.height - min.y + min.height);
					}
					
					if(min.equals(b)) {
						it.remove();
					} else {
						// No se añade
						return;
					}
					
				}
					
				
			}
			
		}
		
		sprites.add(r);
	}

	private void check() {
		
		if(image == null) {
			throw new NullPointerException("The image array cannot be null!");
		} 
		
		if(width < 0) {
			throw new IllegalStateException("Width is < 0");
		}
		
		if(height < 0) {
			throw new IllegalStateException("Height is < 0");
		}
		
		if(width * height < image.length) {
			throw new IndexOutOfBoundsException("Width * Height must be equals to the length of the image");
		}
		
	}

	private SplittedImageList sort(SplittedImageList sprites) {
		
		if(sprites.size() == 0) {
			return sprites;
		}

		SplittedImageList result = new SplittedImageList(sprites.size());

		Comparator<ImageBounds> topleft = (ImageBounds a, ImageBounds b) -> {
			return Integer.compare(a.getCenterX()+a.getCenterY(), b.getCenterX()+b.getCenterY()); 
		};

		Collections.sort(sprites, topleft);

		ImageBounds first = sprites.get(0);

		first.row = 0;
		first.column = 0;

		// Ordeno la primera columna

		List<ImageBounds> pivots = new SplittedImageList();

		pivots.add(first);

		for(int y = first.y+first.height;y < height;y++) {
			y = findRow(y, first.x, first.width, sprites, pivots);
		}

		sprites.removeAll(result);

		// Ahora tenemos la primera columna ordenada, con lo que podemos
		// buscar por 'rays' los de su misma coordenada y

		MutableInteger column = new MutableInteger();

		for(ImageBounds pivot : pivots) {
			result.add(pivot);
			column.value = 1;

			for(int x = pivot.x+pivot.width;x < width;x++) {
				x = findColumn(x, pivot.y, pivot.y+pivot.height, pivot.row,
						column, sprites, result);
			}
			sprites.removeAll(result);
		}
		
		return result;
	}

	private int findRow(int y, int from, int to, 
			List<ImageBounds> src, List<ImageBounds> dst) {

		for(int x = from;x < to;x++) {

			for(ImageBounds bounds : src) {

				if(bounds.contains(x, y)) {
					bounds.row = dst.size();
					bounds.column = 0;
					dst.add(bounds);
					return bounds.y + bounds.height;
				}

			}

		}

		return y;

	}

	private int findColumn(int x, int from, int to, int row, MutableInteger column,
			List<ImageBounds> src, List<ImageBounds> dst) {

		for(int y = from;y < to;y++) {

			for(ImageBounds bounds : src) {

				if(bounds.contains(x, y)) {
					bounds.column = column.value++;
					bounds.row = row;
					dst.add(bounds);
					return bounds.x + bounds.width;
				}

			}

		}

		return x;
	}


	private ImageBounds floodFill(int[] pixels, int x0, int y0, int width, int height) {

		ImageBounds frame = new ImageBounds(x0,y0,1,1);

		Deque<Point> queue = new LinkedList<>();

		queue.add(new Point(x0,y0));

		while(!queue.isEmpty()) {

			final Point p = queue.poll();

			final int x = p.x;
			final int y = p.y;

			if(x < 0 || x >= width || y < 0 || y >= height) {
				continue;
			}

			if(background.contains(pixels[x+y*width])) {
				continue;
			}

			pixels[x+y*width] = background.threshold;

			if(x < frame.x) {
				frame.x = x;
				frame.width++;
			} else if(x > frame.x + frame.width) {
				frame.width++;
			}

			if(y < frame.y) {
				frame.y = y;
				frame.height++;
			} else if(y > frame.y + frame.height) {
				frame.height++;
			}

			queue.add(new Point(x-1,y));
			queue.add(new Point(x+1,y));
			queue.add(new Point(x,y-1));
			queue.add(new Point(x,y+1));

		}

		return frame;
	}
	
	public void setImage(int[] image) {
		this.image = image;
	}

	public int[] getImage() {
		return image;
	}

	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}


	public int getParticleSize() {
		return particleSize;
	}


	public void setParticleSize(int particleSize) {
		this.particleSize = particleSize;
	}


	public int getParticleDistance() {
		return particleDistance;
	}
	
	public void setParticleDistance(int distance) {
		this.particleDistance = distance;
	}
	
	public BackgroundType getBackground() {
		return background;
	}


	public void setBackground(BackgroundType background) {
		this.background = background;
	}

	/**
	 * Class that checks if a given color value belongs to the background or not.
	 * 
	 * */
	public static abstract class BackgroundType {
		
		public static final BackgroundType WHITE = new SolidColorBackground(0xFFFFFFFF);
		
		public static final BackgroundType BLACK = new SolidColorBackground(0);
		
		public static final BackgroundType TRANSPARENT_0x33 = new BackgroundType(0x33) {
			@Override
			public boolean contains(int value) {
				return value >>> 24 <= threshold;
			}
		};
			
		protected final int threshold;	
			
		public BackgroundType(final int threshold) {
			this.threshold = threshold;
		}
		
		public abstract boolean contains(int value);
		
	}
	
	/**
	 * An implementation of {@link BackgroundType} for solid color backgrounds.
	 * 
	 * */
	public static class SolidColorBackground extends BackgroundType {
		
		public SolidColorBackground(final int color) {
			super(color);
		}

		@Override
		public boolean contains(int value) {
			return value == threshold;
		}
		
	}
	
	private static final class Point {
		
		int x;
		int y;
		
		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
	}
	
	private static final class MutableInteger {
		int value;
	}

}
