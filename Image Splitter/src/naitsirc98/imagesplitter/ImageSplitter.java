package naitsirc98.imagesplitter;

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ImageSplitter {

	private final int[] image;
	private final int width, height;
	private int particleSize = 100;
	private int particleDistance = 4;
	private BackgroundType background = BackgroundType.TRANSPARENT;

	public ImageSplitter(int[] image, int width, int height) {
		this.image = image;
		this.width = width;
		this.height = height;
	}
	
	public SplittedImageList split(int w, int h, int hPadding, int vPadding) {
		
		final int columns = width / (w+hPadding);
		final int rows = height / (h+vPadding);
		
		SplittedImageList sprites = new SplittedImageList();
		
		for(int y = 0;y < rows;y++) {
			
			for(int x = 0;x < columns;x++) {
				
				SplittedImage img = new SplittedImage(x*(w+hPadding), y*(h+vPadding), w, h);
				
				img.row = y;
				img.column = x;
				
				sprites.add(img);
				
			}
			
		}
		
		return sprites;
	}

	public SplittedImageList split(int rows, int columns) {
		return split(width/columns, height/rows, 0, 0);
	}

	public SplittedImageList split() {

		SplittedImageList sprites = new SplittedImageList();

		final int[] pixels = image.clone();

		int id = 0;

		for(int y = 0;y < height;y++) {
			for(int x = 0;x < width;x++) {

				// pixels[x+y*width]>>>24 > threshold
				if(!background.contains(pixels[x+y*width])) {

					SplittedImage r = floodFill(pixels,x,y,width,height);
					
					if(addIfNotParticle(r, sprites)) {
						r.id = id++;
					}

				}

			}
		}
		
		return sort(sprites);
	}
	
	private boolean addIfNotParticle(SplittedImage r, SplittedImageList sprites) {
		
		Iterator<SplittedImage> it = sprites.iterator();
		
		while(it.hasNext()) {
			
			SplittedImage b = it.next();
			
			// Si se cortan, vamos a comprobar si podemos unirlos o no
			if(b.intersects(r) || b.distance(r) <= particleDistance) {
				
				SplittedImage min = SplittedImage.min(b,r);
				SplittedImage max = SplittedImage.max(b,r);
				
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
						return false;
					}
					
				}
					
				
			}
			
		}
		
		return sprites.add(r);
		
	}


	private SplittedImageList sort(SplittedImageList sprites) {
		
		if(sprites.size() == 0) {
			return sprites;
		}

		SplittedImageList result = new SplittedImageList(sprites.size());

		Comparator<SplittedImage> topleft = (SplittedImage a, SplittedImage b) -> {
			return Integer.compare(a.getCenterX()+a.getCenterY(), b.getCenterX()+b.getCenterY()); 
		};

		Collections.sort(sprites, topleft);

		SplittedImage first = sprites.get(0);

		first.row = 0;
		first.column = 0;

		// Ordeno la primera columna

		List<SplittedImage> pivots = new SplittedImageList();

		pivots.add(first);

		for(int y = first.y+first.height;y < height;y++) {
			y = findRow(y, first.x, first.width, sprites, pivots);
		}

		sprites.removeAll(result);

		// Ahora tenemos la primera columna ordenada, con lo que podemos
		// buscar por 'rays' los de su misma coordenada y

		MutableInteger column = new MutableInteger();

		for(SplittedImage pivot : pivots) {
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
			List<SplittedImage> src, List<SplittedImage> dst) {

		for(int x = from;x < to;x++) {

			for(SplittedImage bounds : src) {

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
			List<SplittedImage> src, List<SplittedImage> dst) {

		for(int y = from;y < to;y++) {

			for(SplittedImage bounds : src) {

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


	private SplittedImage floodFill(int[] pixels, int x0, int y0, int width, int height) {

		SplittedImage frame = new SplittedImage(x0,y0,1,1);

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

	public int[] getImage() {
		return image;
	}


	public int getWidth() {
		return width;
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

	public static abstract class BackgroundType {
		
		public static final BackgroundType WHITE = new SolidColorBackground(0xFFFFFFFF);
		
		public static final BackgroundType BLACK = new SolidColorBackground(0);
		
		public static final BackgroundType TRANSPARENT = new BackgroundType(0x33) {
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
