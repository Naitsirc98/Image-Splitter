package naitsirc98.imagesplitter;

/**
 * An {@code ImageBounds} is an object that represents the data of a subimage relative to the original image.
 * 
 * <p>Instances of this class only hold data about the bounds and position of the subimage in the original image,
 * but not the subimage itself, so the programmer have to extract the desired subimage with the data provided by this
 * object</p>
 * 
 * <p>Objects of this class are totally modifiable and can be compared.</p>
 * 
 * */
public final class ImageBounds implements Comparable<ImageBounds> {
	
	public static ImageBounds min(ImageBounds a, ImageBounds b) {
		return a.compareTo(b) < 0 ? a : b;
	}	
	
	public static ImageBounds max(ImageBounds a, ImageBounds b) {
		return a.compareTo(b) > 0 ? a : b;
	}
	
	/**
	 * Combines two or more ImageBounds into one. Original instances are not modified.
	 * 
	 * @param bounds subimages to blend.
	 * 
	 * @return the result subimage
	 * 
	 * */
	public static ImageBounds blend(ImageBounds... bounds) {
		
		if(bounds == null || bounds.length == 0) {
			throw new IllegalArgumentException();
		}
		
		final ImageBounds result = bounds[0].clone();
		
		for(int i = 1;i < bounds.length;i++) {
			
			final ImageBounds b = bounds[i];
			
			if(!bounds[i-1].isConsecutive(b)) {
				throw new IllegalStateException("Images are not consecutive: "+bounds[i-1]+", "+b);
			}
			
			if(b.x < result.x) {
				result.x = b.x;
				result.width += result.x - b.x;
			} else if(b.x >= result.x) {
				result.width = Math.max(result.width, b.x+b.width-result.x);
			}
			
			if(b.y < result.y) {
				result.y = b.y;
				result.height += result.y - b.y;
			} else if(b.y >= result.y) {
				result.height = Math.max(result.height, b.y+b.height-result.y);
			}
			
			
		}
		
		return result;
	}
	
	int x, y;
	int width = 1, height = 1;
	int row = -1, column = -1;
	
	public ImageBounds() {
		x = y = 0;
	}
	
	public ImageBounds(int x, int y) {
		this(x, y, 1, 1);
	}
	
	public ImageBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Checks whether this two ImageBounds geometrically intersects or not.
	 * 
	 * @param other another subimage
	 * 
	 * @return true if intersects, false otherwise
	 * 
	 * */
	public boolean intersects(ImageBounds other) {
		
		if(other.width <= 0 || other.height <= 0 || width <= 0 || height <= 0) {
			return false;
		}
		
		final int ow = other.width + other.x;
		final int oh = other.height + other.y;
		final int w = width + x;
		final int h = height + y;
		
		return ((ow < other.x || ow > x) &&
				(oh < other.y || oh > y) &&
				       (w < x || w > other.x) &&
				       (h < y || h > other.y));
		
	}
	
	
	/**
	 * Checks whether this two ImageBounds has the given 2D point or not.
	 * 
	 * @param x coordinate x
	 * @param y coordinate y
	 * 
	 * @return true if contains the coordinates, false otherwise
	 */
	public boolean contains(int x, int y) {
		
		int w = width;
		int h = height;
		
		if((w | h) < 0) {
			return false;
		}
		
		if(x < this.x || y < this.y) {
			return false;
		}
		
		w += this.x;
		h += this.y;
		
		return ((w < this.x || w > x) && 
				(h < this.y || h > y));
		
	}
	
	/**
	 * Returns the distance between the two subimages.
	 * 
	 * @param other another subimage
	 * 
	 * @return the distance between them
	 * 
	 * */
	public int distance(ImageBounds other) {
		return (int) Math.sqrt(Math.pow(x-other.x, 2) + Math.pow(y-other.y,2));
	}
	
	/**
	 * Checks whether two subimages are consecutive or not. Two subimages are consecutive if they are next to each other,
	 * horizontally or vertically. Both subimages should be from the same original image.
	 * 
	 * @param other another image
	 * 
	 * @return true if they are consecutive, false otherwise
	 * 
	 * */
	public boolean isConsecutive(ImageBounds other) {
		
		final int dr = Math.abs(row - other.row);
		final int dc = Math.abs(column - other.column);
		
		return dr == 1 || dc == 1;
		
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public int getCenterX() {
		return (x + width) / 2;
	}
	
	public int getCenterY() {
		return (y + height) / 2;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getSize() {
		return width*height;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + width;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ImageBounds))
			return false;
		ImageBounds other = (ImageBounds) obj;
		if (height != other.height)
			return false;
		if (width != other.width)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ImageBounds [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
	}

	@Override
	public int compareTo(ImageBounds other) {
		return Integer.compare(getSize(), other.getSize());
	}
	
	@Override
	public ImageBounds clone() {
		final ImageBounds clone = new ImageBounds(x,y,width,height);
		clone.row = row;
		clone.column = column;
		return clone;
	}

}
